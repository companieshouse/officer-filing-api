package uk.gov.companieshouse.officerfiling.api.validation;

import org.apache.commons.lang.StringUtils;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;
import uk.gov.companieshouse.officerfiling.api.error.ApiErrors;
import uk.gov.companieshouse.officerfiling.api.error.ErrorType;
import uk.gov.companieshouse.officerfiling.api.error.LocationType;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyAppointmentServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Provides all validation that should be carried out when an officer is terminated. Fetches all data necessary to complete
 * the validation and generates a list of errors that can be sent back to the caller.
 */
public abstract class OfficerValidator {

    public static final LocalDate MIN_RESIGNATION_DATE = LocalDate.of(2009, 10, 1);
    public static final List<String> ALLOWED_COMPANY_TYPES = List.of("private-unlimited", "ltd", "plc", "private-limited-guarant-nsc-limited-exemption",
            "private-limited-guarant-nsc", "private-unlimited-nsc", "private-limited-shares-section-30-exemption");
    public static final List<String> ALLOWED_OFFICER_ROLES = List.of("director", "corporate-director", "nominee-director", "corporate-nominee-director");
    private static final String REG_EXP_FOR_VALID_CHARACTERS = "^[-,.:; 0-9A-Z&@$£¥€'\"«»?!/\\\\()\\[\\]{}<>*=#%+ÀÁÂÃÄÅĀĂĄÆǼÇĆĈĊČÞĎÐÈÉÊËĒĔĖĘĚĜĞĠĢĤĦÌÍÎÏĨĪĬĮİĴĶĹĻĽĿŁÑŃŅŇŊÒÓÔÕÖØŌŎŐǾŒŔŖŘŚŜŞŠŢŤŦÙÚÛÜŨŪŬŮŰŲŴẀẂẄỲÝŶŸŹŻŽa-zſƒǺàáâãäåāăąæǽçćĉċčþďðèéêëēĕėęěĝģğġĥħìíîïĩīĭįĵķĺļľŀłñńņňŋòóôõöøōŏőǿœŕŗřśŝşšţťŧùúûüũūŭůűųŵẁẃẅỳýŷÿźżž]*$";
    private static final String REG_EXP_FOR_UK_POSTCODE = "^[A-Z]{1,2}[0-9][A-Z0-9]? ?[0-9][A-Z]{2}$";
    private static final String REG_EXP_FOR_NAME = "^[ÀÁÂÃÄÅĀĂĄÆǼÇĆĈĊČÞĎÐÈÉÊËĒĔĖĘĚĜĞĠĢĤĦÌÍÎÏĨĪĬĮİĴĶĹĻĽĿŁÑŃŅŇŊÒÓÔÕÖØŌŎŐǾŒŔŖŘŚŜŞŠŢŤŦÙÚÛÜŨŪŬŮŰŲŴẀẂẄỲÝŶŸŹŻŽ'A-Za-zſƒǺàáâãäåāăąæǽçćĉċčþďðèéêëēĕėęěĝģğġĥħìíîïĩīĭįĵķĺļľŀłñńņňŋòóôõöøōŏőǿœŕŗřśŝşšţťŧùúûüũūŭůűųŵẁẃẅỳýŷÿźżž -]*$";
    private static final String REG_EXP_FOR_FORMER_NAMES = "^[ÀÁÂÃÄÅĀĂĄÆǼÇĆĈĊČÞĎÐÈÉÊËĒĔĖĘĚĜĞĠĢĤĦÌÍÎÏĨĪĬĮİĴĶĹĻĽĿŁÑŃŅŇŊÒÓÔÕÖØŌŎŐǾŒŔŖŘŚŜŞŠŢŤŦÙÚÛÜŨŪŬŮŰŲŴẀẂẄỲÝŶŸŹŻŽ'A-Za-zſƒǺàáâãäåāăąæǽçćĉċčþďðèéêëēĕėęěĝģğġĥħìíîïĩīĭįĵķĺļľŀłñńņňŋòóôõöøōŏőǿœŕŗřśŝşšţťŧùúûüũūŭůűųŵẁẃẅỳýŷÿźżž, -]*$";
    private static final String DISSOLVED = "dissolved";

    private final Logger logger;
    private final String inputAllowedNationalities;
    private final CompanyProfileService companyProfileService;
    private CompanyAppointmentService companyAppointmentService;
    protected ApiEnumerations apiEnumerations;

    protected OfficerValidator(final Logger logger, final CompanyProfileService companyProfileService,
                               final CompanyAppointmentService companyAppointmentService, final String inputAllowedNationalities,
                               final ApiEnumerations apiEnumerations) {
        this.logger = logger;
        this.companyProfileService = companyProfileService;
        this.companyAppointmentService = companyAppointmentService;
        this.inputAllowedNationalities = inputAllowedNationalities;
        this.apiEnumerations = apiEnumerations;
    }

    protected OfficerValidator(final Logger logger,
                               final CompanyProfileService companyProfileService,
                               final String inputAllowedNationalities,
                               final ApiEnumerations apiEnumerations) {
        this.logger = logger;
        this.companyProfileService = companyProfileService;
        this.inputAllowedNationalities = inputAllowedNationalities;
        this.apiEnumerations = apiEnumerations;
    }

    /**
     * Superclass for the Main validation method to fetch the required data and validate the request.
     * This should not be called but rather the sub-classes should be used instead.to be the point of call when terminating an officer.
     *
     * @param request           The servlet request used in logging
     * @param dto               Data Object containing details of the termination
     * @param transaction       the transaction for this termination
     * @param passthroughHeader ERIC pass through header for authorisation
     * @return An object containing a list of any validation errors that have been raised
     */
    public ApiErrors validate(HttpServletRequest request, OfficerFilingDto dto, Transaction transaction, String passthroughHeader) {
        final List<ApiError> errorList = new ArrayList<>();
        return new ApiErrors(errorList);
    }

    protected void validateRequiredTransactionFields(HttpServletRequest request, List<ApiError> errorList, Transaction transaction) {
        if (transaction.getCompanyNumber() == null || transaction.getCompanyNumber().isBlank()) {
            createValidationError(request, errorList, "The company number cannot be null or blank");
        }
    }

    public Optional<CompanyProfileApi> getCompanyProfile(HttpServletRequest request,
                                                         List<ApiError> errorList, Transaction transaction, String passthroughHeader) {
        try {
            return Optional.ofNullable(
                    companyProfileService.getCompanyProfile(transaction.getId(), transaction.getCompanyNumber(),
                            passthroughHeader));
        } catch (ServiceUnavailableException e) {
            createServiceError(request, errorList);
        } catch (CompanyProfileServiceException e) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CANNOT_FIND_COMPANY));
        }
        return Optional.empty();
    }

    protected void validateCompanyNotDissolved(HttpServletRequest request, List<ApiError> errorList, CompanyProfileApi companyProfile) {
        if (companyProfile.getCompanyStatus() == null) {
            logger.errorRequest(request, "null data was found in the Company Profile API within the Company Status field");
            return;
        }
        if (Objects.equals(companyProfile.getCompanyStatus(), DISSOLVED) || companyProfile.getDateOfCessation() != null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.COMPANY_DISSOLVED));
        }
    }

    public void validateAllowedCompanyType(HttpServletRequest request, List<ApiError> errorList, CompanyProfileApi companyProfile) {
        if (companyProfile.getType() == null) {
            logger.errorRequest(request, "null data was found in the Company Profile API within the Company Type field");
            return;
        }
        if (!ALLOWED_COMPANY_TYPES.contains(companyProfile.getType())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.COMPANY_TYPE_NOT_PERMITTED, apiEnumerations.getCompanyType(companyProfile.getType())));
        }
    }

    public void validateOfficerRole(HttpServletRequest request, List<ApiError> errorList, AppointmentFullRecordAPI companyAppointment) {
        if (companyAppointment.getOfficerRole() == null) {
            logger.errorRequest(request, "null data was found in the Company Appointment API within the Officer Role field");
            return;
        }
        if (!ALLOWED_OFFICER_ROLES.contains(companyAppointment.getOfficerRole())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.OFFICER_ROLE));
        }
    }

    public String getDirectorName(AppointmentFullRecordAPI appointment) {
        if (appointment != null && appointment.getForename() != null && appointment.getSurname() != null) {
            return appointment.getForename() + " " + appointment.getSurname();
        }
        return "Director";
    }

    public Optional<AppointmentFullRecordAPI> getOfficerAppointment(HttpServletRequest request,
                                                                    List<ApiError> errorList, OfficerFilingDto dto, Transaction transaction, String passthroughHeader) {
        try {
            return Optional.ofNullable(
                    companyAppointmentService.getCompanyAppointment(transaction.getId(), transaction.getCompanyNumber(),
                            dto.getReferenceAppointmentId(), passthroughHeader));
        } catch (ServiceUnavailableException e) {
            createServiceError(request, errorList);
        } catch (CompanyAppointmentServiceException e) {
            // We do not have the directors name in this scenario for the error message
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DIRECTOR_NOT_FOUND, getDirectorName(null)));
        }
        return Optional.empty();
    }

    public void createServiceError(HttpServletRequest request, List<ApiError> errorList) {
        final var apiError = new ApiError(apiEnumerations.getValidation(ValidationEnum.SERVICE_UNAVAILABLE), request.getRequestURI(),
                LocationType.JSON_PATH.getValue(), ErrorType.SERVICE.getType());
        errorList.add(apiError);
    }

    public void createValidationError(HttpServletRequest request, List<ApiError> errorList, String errorMessage) {
        final var apiError = new ApiError(errorMessage, request.getRequestURI(),
                LocationType.JSON_PATH.getValue(), ErrorType.VALIDATION.getType());
        errorList.add(apiError);
    }

    public boolean validateDtoFieldLength(String field, int maxLength) {
        return field.length() <= maxLength;
    }

    public boolean validateFormerNamesLength(String formerNames) {
        return formerNames.length() <= 160;
    }

    public static boolean isValidCharacters(String field) {
        var pattern = Pattern.compile(REG_EXP_FOR_VALID_CHARACTERS);
        var matcher = pattern.matcher(field);
        return matcher.matches();
    }

    public static boolean isValidNameCharacters(String field) {
        var pattern = Pattern.compile(REG_EXP_FOR_NAME);
        var matcher = pattern.matcher(field);
        return matcher.matches();
    }

    public static boolean isValidFormerNameCharacters(String field) {
        var pattern = Pattern.compile(REG_EXP_FOR_FORMER_NAMES);
        var matcher = pattern.matcher(field);
        return matcher.matches();
    }

    public static boolean isValidCharactersForUkPostcode(String field) {
        if (field == null) return false;
        var pattern = Pattern.compile(REG_EXP_FOR_UK_POSTCODE);
        var matcher = pattern.matcher(field.toUpperCase().trim());
        return matcher.matches();
    }

    public List<String> getAllowedNationalities(String inputAllowedNationalities) {

        String[] nationalityArray = inputAllowedNationalities.split(",");

        return Arrays.asList(nationalityArray);
    }

    public boolean isValidNationalityFromAllowedList(String nationality, String inputAllowedNationalities) {
        return getAllowedNationalities(inputAllowedNationalities).stream().anyMatch(x -> x.equalsIgnoreCase(nationality));
    }

    public Optional<LocalDate> getAppointmentDate(HttpServletRequest request, AppointmentFullRecordAPI companyAppointment) {
        var isPre1992 = companyAppointment.getIsPre1992Appointment();
        if (isPre1992 == null) {
            logger.errorRequest(request, "null data was found in the Company Appointment API within the Pre-1992 Appointment field");
            return Optional.empty();
        }
        // If pre-1992 then set as appointedBefore field
        if (isPre1992) {
            return Optional.ofNullable(companyAppointment.getAppointedBefore()).or(() -> {
                logger.errorRequest(request, "null data was found in the Company Appointment API within the Appointed Before field");
                return Optional.empty();
            });
        }
        // Else set as appointedOn field
        return Optional.ofNullable(companyAppointment.getAppointedOn()).or(() -> {
            logger.errorRequest(request, "null data was found in the Company Appointment API within the Appointed On field");
            return Optional.empty();
        });
    }

    protected void validateTitle(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getTitle() != null) {
            if (!validateDtoFieldLength(dto.getTitle(), 50)) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.TITLE_LENGTH));
            }
            if (!isValidNameCharacters(dto.getTitle())) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.TITLE_CHARACTERS));
            }
        }
    }

    protected void validateFirstName(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getFirstName() == null || dto.getFirstName().isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_BLANK));
        } else {
            if (!validateDtoFieldLength(dto.getFirstName(), 50)) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_LENGTH));
            }
            if (!isValidNameCharacters(dto.getFirstName())) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_CHARACTERS));
            }
        }
    }

    protected void validateLastName(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getLastName() == null || dto.getLastName().isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.LAST_NAME_BLANK));
        } else {
            if (!validateDtoFieldLength(dto.getLastName(), 160)) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.LAST_NAME_LENGTH));
            }
            if (!isValidNameCharacters(dto.getLastName())) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.LAST_NAME_CHARACTERS));
            }
        }
    }

    protected void validateMiddleNames(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getMiddleNames() != null) {
            if (!validateDtoFieldLength(dto.getMiddleNames(), 50)) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.MIDDLE_NAME_LENGTH));
            }
            if (!isValidNameCharacters(dto.getMiddleNames())) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.MIDDLE_NAME_CHARACTERS));
            }
        }
    }

    protected void validateNationality1(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getNationality1() == null || dto.getNationality1().isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.NATIONALITY_BLANK));
        } else {
            if (!isValidNationalityFromAllowedList(dto.getNationality1(), inputAllowedNationalities)) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.INVALID_NATIONALITY));
            }
        }
    }

    protected void validateNationality2(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (!StringUtils.isEmpty(dto.getNationality2())) {
            if (dto.getNationality2().equalsIgnoreCase(dto.getNationality1())) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DUPLICATE_NATIONALITY2));
            }
            if (!isValidNationalityFromAllowedList(dto.getNationality2(), inputAllowedNationalities)) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.INVALID_NATIONALITY));
            }
        }
    }

    protected void validateNationality3(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (!StringUtils.isEmpty(dto.getNationality3())) {

            if (dto.getNationality3().equalsIgnoreCase(dto.getNationality1())) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DUPLICATE_NATIONALITY3));
            }
            if (dto.getNationality3().equalsIgnoreCase(dto.getNationality2())) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DUPLICATE_NATIONALITY3));
            }
            if (!isValidNationalityFromAllowedList(dto.getNationality3(), inputAllowedNationalities)) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.INVALID_NATIONALITY));
            }
        }
    }

    protected void validateNationalityLength(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
       var nationatities = Arrays.asList(dto.getNationality1(), dto.getNationality2(), dto.getNationality3())
            .stream()
            .filter(Objects::nonNull)
            .map(s -> s.replace(",", ""))
            .map(String::trim)
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.joining(","));
       
        if (!validateDtoFieldLength(nationatities, 50)) {
            String errorMessage;
            if (StringUtils.isEmpty(dto.getNationality2()) && StringUtils.isEmpty(dto.getNationality3())) {
                errorMessage = apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH);
            } else {
                if (StringUtils.isEmpty(dto.getNationality2()) || StringUtils.isEmpty(dto.getNationality3())) {
                    errorMessage = apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH49);
                } else {
                    errorMessage = apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH48);
                }
            }
            createValidationError(request, errorList, errorMessage);
        }
    }

    protected void validateOccupation(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getOccupation() != null) {
            if (!validateDtoFieldLength(dto.getOccupation(), 100)) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.OCCUPATION_LENGTH));
            }
            if (!isValidCharacters(dto.getOccupation())) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.OCCUPATION_CHARACTERS));
            }
        }
    }

    public void validateSubmissionInformationInDate(HttpServletRequest request, OfficerFilingDto dto, AppointmentFullRecordAPI companyAppointment, List<ApiError> errorList) {
        if (companyAppointment.getEtag() == null) {
            logger.errorRequest(request, "null data was found in the Company Appointment API within the etag field");
            return;
        }
        if(dto.getReferenceEtag() == null || dto.getReferenceEtag().isBlank()){
            // Caught by mandatory field checks
            return;
        }
        // If submission information is not out-of-date, the ETAG retrieved from the Company Appointments API and the ETAG passed from the request will match
        if (!Objects.equals(dto.getReferenceEtag(), companyAppointment.getEtag())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.ETAG_INVALID));
        }
    }

    public void validateEtagPresent(HttpServletRequest request, OfficerFilingDto dto, List<ApiError> errorList){
        if (dto.getReferenceEtag() == null || dto.getReferenceEtag().isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.ETAG_BLANK));
        }
    }

}
