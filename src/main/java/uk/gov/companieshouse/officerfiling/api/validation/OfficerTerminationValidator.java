package uk.gov.companieshouse.officerfiling.api.validation;

import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;
import uk.gov.companieshouse.officerfiling.api.error.ApiErrors;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides all validation that should be carried out when an officer is terminated. Fetches all data necessary to complete
 * the validation and generates a list of errors that can be sent back to the caller.
 */
public class OfficerTerminationValidator extends OfficerValidator {

    private final Logger logger;

    public OfficerTerminationValidator(final Logger logger,
                                       final CompanyProfileService companyProfileService,
                                       final CompanyAppointmentService companyAppointmentService,
                                       final String inputAllowedNationalities,
                                       final ApiEnumerations apiEnumerations) {
        super(logger, companyProfileService, companyAppointmentService, inputAllowedNationalities, apiEnumerations);
        this.logger = logger;
    }

    /**
     * Main validation method to fetch the required data and validate the request. This should be the point of call when terminating an officer.
     *
     * @param request           The servlet request used in logging
     * @param dto               Data Object containing details of the termination
     * @param transaction       the transaction for this termination
     * @param passthroughHeader ERIC pass through header for authorisation
     * @return An object containing a list of any validation errors that have been raised
     */
    @Override
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
        validateOfficerIsNotTerminated(request, errorList, companyAppointment.get());
        validateOfficerRole(request, errorList, companyAppointment.get());

        return new ApiErrors(errorList);
    }

    public void validateRequiredDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        // check for blank officer id, eTag and termination date
        if (dto.getReferenceAppointmentId() == null || dto.getReferenceAppointmentId().isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.OFFICER_ID_BLANK));
        }

        validateEtagPresent(request, dto, errorList);

        if (dto.getResignedOn() == null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_MISSING, getDirectorName(null)));
        }
    }

    public void validateResignationDatePastOrPresent(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI companyAppointment) {
        if (dto.getResignedOn().isAfter(LocalDate.now())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_IN_PAST, getDirectorName(companyAppointment)));
        }
    }

    public void validateMinResignationDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        // Earliest ever possible date that a director can have been removed that is valid on the CH system is the 1st of october 2009.
        if (dto.getResignedOn().isBefore(MIN_RESIGNATION_DATE)) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_2009));
        }
    }

    public void validateTerminationDateAfterIncorporationDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, CompanyProfileApi companyProfile, AppointmentFullRecordAPI companyAppointment) {
        if (companyProfile.getDateOfCreation() == null) {
            logger.errorRequest(request, "null data was found in the Company Profile API within the Date Of Creation field");
            return;
        }
        if (dto.getResignedOn().isBefore(companyProfile.getDateOfCreation())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_INCORPORATION_DATE));
        }
    }

    /**
     * Check to ensure a request isn't being filed for an officer who has already resigned.
     * Used for Validation rules D19_9A/D19_9
     */
    public void validateOfficerIsNotTerminated(HttpServletRequest request, List<ApiError> errorList, AppointmentFullRecordAPI companyAppointment) {
        if (companyAppointment.getResignedOn() != null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DIRECTOR_ALREADY_REMOVED, getDirectorName(companyAppointment)));
        }
    }

    public void validateTerminationDateAfterAppointmentDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI companyAppointment) {
        var companyAppointmentDate = getAppointmentDate(request, companyAppointment);
        if (companyAppointmentDate.isPresent() && dto.getResignedOn().isBefore(companyAppointmentDate.get())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_APPOINTMENT_DATE, getDirectorName(companyAppointment)));
        }
    }

    @Override
    public void validateAllowedCompanyType(HttpServletRequest request, List<ApiError> errorList, CompanyProfileApi companyProfile) {
        if (companyProfile.getType() == null) {
            logger.errorRequest(request, "null data was found in the Company Profile API within the Company Type field");
            return;
        }
        if (!ALLOWED_COMPANY_TYPES.contains(companyProfile.getType())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.COMPANY_TYPE_NOT_PERMITTED, apiEnumerations.getCompanyType(companyProfile.getType())));
        }
    }

    @Override
    public void validateOfficerRole(HttpServletRequest request, List<ApiError> errorList, AppointmentFullRecordAPI companyAppointment) {
        if (companyAppointment.getOfficerRole() == null) {
            logger.errorRequest(request, "null data was found in the Company Appointment API within the Officer Role field");
            return;
        }
        if (!ALLOWED_OFFICER_ROLES.contains(companyAppointment.getOfficerRole())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.OFFICER_ROLE));
        }
    }


}
