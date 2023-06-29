package uk.gov.companieshouse.officerfiling.api.validation;

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
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides all validation that should be carried out when an officer is terminated. Fetches all data necessary to complete
 * the validation and generates a list of errors that can be sent back to the caller.
 */
public class OfficerTerminationValidator {

    private static final LocalDate MIN_RESIGNATION_DATE = LocalDate.of(2009, 10, 1);
    private static final List<String> ALLOWED_COMPANY_TYPES = List.of("private-unlimited", "ltd", "plc", "old-public-company", "private-limited-guarant-nsc-limited-exemption",
            "private-limited-guarant-nsc", "private-unlimited-nsc", "private-limited-shares-section-30-exemption");
    private static final List<String> ALLOWED_OFFICER_ROLES = List.of("director", "corporate-director", "nominee-director", "corporate-nominee-director");

    private final CompanyProfileService companyProfileService;
    private final CompanyAppointmentService companyAppointmentService;
    private final Logger logger;
    private final ApiEnumerations apiEnumerations;

    public OfficerTerminationValidator(final Logger logger,
                                       final CompanyProfileService companyProfileService,
                                       final CompanyAppointmentService companyAppointmentService,
                                       final ApiEnumerations apiEnumerations) {
        this.logger = logger;
        this.companyProfileService = companyProfileService;
        this.companyAppointmentService = companyAppointmentService;
        this.apiEnumerations = apiEnumerations;
    }

    /**
     * Main validation method to fetch the required data and validate the request. This should be the point of call when terminating an officer.
     * @param request The servlet request used in logging
     * @param dto Data Object containing details of the termination
     * @param transaction the transaction for this termination
     * @param passthroughHeader ERIC pass through header for authorisation
     * @return An object containing a list of any validation errors that have been raised
     */
    public ApiErrors validate(HttpServletRequest request, OfficerFilingDto dto, Transaction transaction, String passthroughHeader) {
            logger.debugContext(transaction.getId(), "Beginning officer termination validation", new LogHelper.Builder(transaction)
                .withRequest(request)
                .build());
        final List<ApiError> errorList = new ArrayList<>();

        // Validate required dto and transaction fields and fail early
        validateRequiredDtoFields(request, errorList, dto);
        validateRequiredTransactionFields(request, errorList, transaction);
        if (!errorList.isEmpty()) {
            return new ApiErrors(errorList);
        }

        // Retrieve data objects required for the validation process
        final Optional<AppointmentFullRecordAPI> companyAppointment = getOfficerAppointment(request, errorList, dto, transaction, passthroughHeader);
        final Optional<CompanyProfileApi> companyProfile = getCompanyProfile(request, errorList, transaction, passthroughHeader);
        if (companyAppointment.isEmpty() || companyProfile.isEmpty()) {
            return new ApiErrors(errorList);
        }

        // Perform validation
        validateSubmissionInformationInDate(request, dto, companyAppointment.get(), errorList);
        validateResignationDatePastOrPresent(request, errorList, dto, companyAppointment.get());
        validateMinResignationDate(request, errorList, dto);
        validateCompanyNotDissolved(request, errorList, companyProfile.get());
        validateTerminationDateAfterIncorporationDate(request, errorList, dto, companyProfile.get(), companyAppointment.get());
        validateTerminationDateAfterAppointmentDate(request, errorList, dto, companyAppointment.get());
        validateAllowedCompanyType(request, errorList, companyProfile.get());
        validateOfficerIsNotTerminated(request,errorList,companyAppointment.get());
        validateOfficerRole(request, errorList, companyAppointment.get());

        return new ApiErrors(errorList);
    }

    public void validateRequiredDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        // check for blank officer id, eTag and termination date
        if (dto.getOfficerFilingData().getReferenceAppointmentId() == null || dto.getOfficerFilingData().getReferenceAppointmentId().isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.OFFICER_ID_BLANK));
        }

        if (dto.getOfficerFilingData().getReferenceEtag() == null || dto.getOfficerFilingData().getReferenceEtag().isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.ETAG_BLANK));
        }

        if (dto.getOfficerFilingData().getResignedOn() == null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_MISSING, getDirectorName(null)));
        }
    }

    public void validateRequiredTransactionFields(HttpServletRequest request, List<ApiError> errorList, Transaction transaction) {
        if (transaction.getCompanyNumber() == null || transaction.getCompanyNumber().isBlank()) {
            createValidationError(request, errorList, "The company number cannot be null or blank");
        }
    }

    public Optional<AppointmentFullRecordAPI> getOfficerAppointment(HttpServletRequest request,
        List<ApiError> errorList, OfficerFilingDto dto, Transaction transaction, String passthroughHeader) {
        try {
            return Optional.ofNullable(
                    companyAppointmentService.getCompanyAppointment(transaction.getId(), transaction.getCompanyNumber(),
                            dto.getOfficerFilingData().getReferenceAppointmentId(), passthroughHeader));
        } catch (ServiceUnavailableException e) {
            createServiceError(request, errorList);
        } catch (CompanyAppointmentServiceException e) {
            // We do not have the directors name in this scenario for the error message
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DIRECTOR_NOT_FOUND, getDirectorName(null)));
        }
        return Optional.empty();
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

    public void validateSubmissionInformationInDate(HttpServletRequest request, OfficerFilingDto dto, AppointmentFullRecordAPI companyAppointment, List<ApiError> errorList) {
        if (companyAppointment.getEtag() == null) {
            logger.errorRequest(request, "null data was found in the Company Appointment API within the etag field");
            return;
        }
        // If submission information is not out-of-date, the ETAG retrieved from the Company Appointments API and the ETAG passed from the request will match
        if(!Objects.equals(dto.getOfficerFilingData().getReferenceEtag(), companyAppointment.getEtag())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.ETAG_INVALID));
        }
    }

    public void validateResignationDatePastOrPresent(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI companyAppointment) {
        if (dto.getOfficerFilingData().getResignedOn().isAfter(LocalDate.now())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_IN_PAST, getDirectorName(companyAppointment)));
        }
    }

    public void validateMinResignationDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        // Earliest ever possible date that a director can have been removed that is valid on the CH system is the 1st of october 2009.
        if(dto.getOfficerFilingData().getResignedOn().isBefore(MIN_RESIGNATION_DATE)) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_2009));
        }
    }

    public void validateTerminationDateAfterIncorporationDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, CompanyProfileApi companyProfile, AppointmentFullRecordAPI companyAppointment) {
        if (companyProfile.getDateOfCreation() == null) {
            logger.errorRequest(request, "null data was found in the Company Profile API within the Date Of Creation field");
            return;
        }
        if (dto.getOfficerFilingData().getResignedOn().isBefore(companyProfile.getDateOfCreation())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_INCORPORATION_DATE));
        }
    }

    /**
     * Check to ensure a request isn't being filed for an officer who has already resigned.
     * Used for Validation rules D19_9A/D19_9
     */
    public void validateOfficerIsNotTerminated(HttpServletRequest request, List<ApiError> errorList, AppointmentFullRecordAPI companyAppointment){
        if(companyAppointment.getResignedOn() != null){
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DIRECTOR_ALREADY_REMOVED, getDirectorName(companyAppointment)));
        }
    }

    public void validateCompanyNotDissolved(HttpServletRequest request, List<ApiError> errorList, CompanyProfileApi companyProfile) {
        if (companyProfile.getCompanyStatus() == null) {
            logger.errorRequest(request, "null data was found in the Company Profile API within the Company Status field");
            return;
        }
        if (Objects.equals(companyProfile.getCompanyStatus(), "dissolved") || companyProfile.getDateOfCessation() != null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.COMPANY_DISSOLVED));
        }
    }

    public void validateTerminationDateAfterAppointmentDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI companyAppointment) {
        var companyAppointmentDate = getAppointmentDate(request, companyAppointment);
        if (companyAppointmentDate.isPresent() && dto.getOfficerFilingData().getResignedOn().isBefore(companyAppointmentDate.get())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_APPOINTMENT_DATE, getDirectorName(companyAppointment)));
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

    private Optional<LocalDate> getAppointmentDate(HttpServletRequest request, AppointmentFullRecordAPI companyAppointment) {
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

    private String getDirectorName(AppointmentFullRecordAPI appointment) {
        if (appointment != null && appointment.getForename() != null && appointment.getSurname() != null) {
            return appointment.getForename() + " " + appointment.getSurname();
        }
        return "Director";
    }

    private void createServiceError (HttpServletRequest request, List<ApiError> errorList) {
        final var apiError = new ApiError(apiEnumerations.getValidation(ValidationEnum.SERVICE_UNAVAILABLE), request.getRequestURI(),
            LocationType.JSON_PATH.getValue(), ErrorType.SERVICE.getType());
        errorList.add(apiError);
    }

    private void createValidationError(HttpServletRequest request, List<ApiError> errorList, String errorMessage) {
        final var apiError = new ApiError(errorMessage, request.getRequestURI(),
                LocationType.JSON_PATH.getValue(), ErrorType.VALIDATION.getType());
        errorList.add(apiError);
    }
}
