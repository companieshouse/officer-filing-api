package uk.gov.companieshouse.officerfiling.api.validation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
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
import uk.gov.companieshouse.officerfiling.api.model.dto.FormerNameDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;

/**
 * Provides all validation that should be carried out when an officer is terminated. Fetches all data necessary to complete
 * the validation and generates a list of errors that can be sent back to the caller.
 */
public class OfficerValidator {

    public static final LocalDate MIN_RESIGNATION_DATE = LocalDate.of(2009, 10, 1);
    public static final List<String> ALLOWED_COMPANY_TYPES = List.of("private-unlimited", "ltd", "plc", "private-limited-guarant-nsc-limited-exemption",
            "private-limited-guarant-nsc", "private-unlimited-nsc", "private-limited-shares-section-30-exemption");
    public static final List<String> ALLOWED_OFFICER_ROLES = List.of("director", "corporate-director", "nominee-director", "corporate-nominee-director");
    private static final String REG_EXP_FOR_INVALID_CHARACTERS = "^[-,.:; 0-9A-Z&@$£¥€'\"«»?!/\\\\()\\[\\]{}<>*=#%+ÀÁÂÃÄÅĀĂĄÆǼÇĆĈĊČÞĎÐÈÉÊËĒĔĖĘĚĜĞĠĢĤĦÌÍÎÏĨĪĬĮİĴĶĹĻĽĿŁÑŃŅŇŊÒÓÔÕÖØŌŎŐǾŒŔŖŘŚŜŞŠŢŤŦÙÚÛÜŨŪŬŮŰŲŴẀẂẄỲÝŶŸŹŻŽa-zſƒǺàáâãäåāăąæǽçćĉċčþďðèéêëēĕėęěĝģğġĥħìíîïĩīĭįĵķĺļľŀłñńņňŋòóôõöøōŏőǿœŕŗřśŝşšţťŧùúûüũūŭůűųŵẁẃẅỳýŷÿźżž]*$";
    private Logger logger;

    public ApiEnumerations getApiEnumerations() {
        return apiEnumerations;
    }

    public void setApiEnumerations(
            ApiEnumerations apiEnumerations) {
        this.apiEnumerations = apiEnumerations;
    }

    private ApiEnumerations apiEnumerations;

    private final CompanyProfileService companyProfileService;

    private CompanyAppointmentService companyAppointmentService;

    public OfficerValidator(final Logger logger, final CompanyProfileService companyProfileService,
        final CompanyAppointmentService companyAppointmentService,
        final ApiEnumerations apiEnumerations) {
            this.logger = logger;
            this.companyProfileService = companyProfileService;
            this.companyAppointmentService = companyAppointmentService;
            this.apiEnumerations = apiEnumerations;
        }

    public OfficerValidator(final Logger logger,
            final CompanyProfileService companyProfileService,
            final ApiEnumerations apiEnumerations) {
        this.logger = logger;
        this.companyProfileService = companyProfileService;
        this.apiEnumerations = apiEnumerations;
    }

    /**
     * Superclass for the Main validation method to fetch the required data and validate the request.
     * This should not be called but rather the sub-classes should be used instead.to be the point of call when terminating an officer.
     * @param request The servlet request used in logging
     * @param dto Data Object containing details of the termination
     * @param transaction the transaction for this termination
     * @param passthroughHeader ERIC pass through header for authorisation
     * @return An object containing a list of any validation errors that have been raised
     */
    public ApiErrors validate(HttpServletRequest request, OfficerFilingDto dto, Transaction transaction, String passthroughHeader){
        final List<ApiError> errorList = new ArrayList<>();
        return new ApiErrors(errorList);
    }

    public void validateRequiredDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
    // specific to the sub-classes so no need for code in the super class.
    }

    public void validateOptionalDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        // specific to the sub-classes so no need for code in the super class.
    }

    public void validateRequiredTransactionFields(HttpServletRequest request, List<ApiError> errorList, Transaction transaction){
    // specific to the sub-classes so no need for code in the super class.
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

    public void validateCompanyNotDissolved(HttpServletRequest request, List<ApiError> errorList, CompanyProfileApi companyProfile){
        if (companyProfile.getCompanyStatus() == null) {
            logger.errorRequest(request, "null data was found in the Company Profile API within the Company Status field");
            return;
        }
        if (Objects.equals(companyProfile.getCompanyStatus(), "dissolved") || companyProfile.getDateOfCessation() != null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.COMPANY_DISSOLVED));
        }
    }

    public void validateAllowedCompanyType(HttpServletRequest request, List<ApiError> errorList, CompanyProfileApi companyProfile){
        if (companyProfile.getType() == null) {
            logger.errorRequest(request, "null data was found in the Company Profile API within the Company Type field");
            return;
        }
        if (!ALLOWED_COMPANY_TYPES.contains(companyProfile.getType())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.COMPANY_TYPE_NOT_PERMITTED, apiEnumerations.getCompanyType(companyProfile.getType())));
        }
    }

    public void validateOfficerRole(HttpServletRequest request, List<ApiError> errorList, AppointmentFullRecordAPI companyAppointment){
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

    public boolean validateDtoFieldLength(String field, int maxLength){
        if(field.length() > maxLength){
            return false;
        }
        return true;
    }

    public boolean validateFormerNamesLength(List<FormerNameDto> formerNames){
        var length = 0;
        for(var formerName : formerNames){
            length = length + formerName.getForenames().length() + formerName.getSurname().length();
        }
        if(length > 160){
            return false;
        }
        return true;
    }


    public static boolean isValidCharacters(String field) {
        var pattern = Pattern.compile(REG_EXP_FOR_INVALID_CHARACTERS);
        var matcher = pattern.matcher(field);
        if (!matcher.matches()) {
            return false;
        }
        return true;
    }


}
