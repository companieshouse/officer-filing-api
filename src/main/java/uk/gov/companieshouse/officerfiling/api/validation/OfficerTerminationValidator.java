package uk.gov.companieshouse.officerfiling.api.validation;

import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.error.ApiErrors;
import uk.gov.companieshouse.officerfiling.api.error.ErrorType;
import uk.gov.companieshouse.officerfiling.api.error.LocationType;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyAppointmentServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;
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

    private static final LocalDate EARLIEST_POSSIBLE_DATE = LocalDate.of(2009, 10, 1);
    private static final List<String> ALLOWED_COMPANY_TYPES = List.of("private-unlimited", "ltd", "plc", "old-public-company", "private-limited-guarant-nsc-limited-exemption",
            "private-limited-guarant-nsc", "private-unlimited-nsc", "private-limited-shares-section-30-exemption");
    private static final List<String> ALLOWED_OFFICER_ROLES = List.of("director", "corporate-director");

    private final TransactionService transactionService;
    private final CompanyProfileService companyProfileService;
    private final CompanyAppointmentService companyAppointmentService;
    private final Logger logger;

    public OfficerTerminationValidator(final Logger logger,
                                       final TransactionService transactionService,
                                       final CompanyProfileService companyProfileService,
                                       final CompanyAppointmentService companyAppointmentService) {
        this.logger = logger;
        this.transactionService = transactionService;
        this.companyProfileService = companyProfileService;
        this.companyAppointmentService = companyAppointmentService;
    }

    /**
     * Main validation method to fetch the required data and validate the request. This should be the point of call when terminating an officer.
     * @param request The servlet request used in logging
     * @param dto Data Object containing details of the termination
     * @param transaction the transaction for this termination
     * @param passthroughHeader ERIC pass through header for authorisation
     * @return An object containing a list of any validation errors that have been raised
     */
    public ApiErrors validate(HttpServletRequest request, OfficerFilingDto dto, Transaction transaction,
        String passthroughHeader) {
            logger.debugContext(transaction.getId(), "Beginning officer termination validation", new LogHelper.Builder(transaction)
                .withRequest(request)
                .build());
        List<ApiError> errorList = new ArrayList<>();

        // Retrieve data objects required for the validation process
        final Optional<AppointmentFullRecordAPI> companyAppointment = getOfficerAppointment(request, errorList, dto, transaction, passthroughHeader);
        final Optional<CompanyProfileApi> companyProfile = getCompanyProfile(request, errorList, transaction, passthroughHeader);

        if (companyAppointment.isEmpty()) {
            return new ApiErrors(errorList);
        }
        if (companyProfile.isEmpty()) {
            return new ApiErrors(errorList);
        }
        // Perform validation
        validateSubmissionInformationInDate(request, dto, companyAppointment.get(), errorList);
        validateMinResignationDate(request, errorList, dto);
        validateSubmissionInformationInDate(request, dto, companyAppointment.get(), errorList);
        validateCompanyNotDissolved(request, errorList, companyProfile.get());
        validateTerminationDateAfterIncorporationDate(request, errorList, dto, companyProfile.get(), companyAppointment.get());
        validateTerminationDateAfterAppointmentDate(request, errorList, dto, companyAppointment.get());
        validateAllowedCompanyType(request, errorList, companyProfile.get());
        validateOfficerIsNotTerminated(request,errorList,companyAppointment.get());
        validateOfficerRole(request, errorList, companyAppointment.get());

        return new ApiErrors(errorList);
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
            createValidationError(request, errorList, "Officer not found. Please confirm the details and resubmit");
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
            createValidationError(request, errorList, "Company not found. Please confirm the details and resubmit");
        }
        return Optional.empty();
    }

    public void validateSubmissionInformationInDate(HttpServletRequest request, OfficerFilingDto dto, AppointmentFullRecordAPI companyAppointment, List<ApiError> errorList) {
        // If submission information is not out of date, the ETAG retrieved from the Company Appointments API and the ETAG passed from the request will match
        String companyAppointmentEtag = companyAppointment.getEtag();
        String requestEtag = dto.getReferenceEtag();

        if(!Objects.equals(requestEtag, companyAppointmentEtag)) {
            createValidationError(request, errorList,"The Officers information is out of date. Please start the process again and make a new submission");
        }
    }

    public void validateMinResignationDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        // Earliest ever possible date that a director can have been removed that is valid on the CH system is the 1st of october 2009.
        if(dto.getResignedOn().isBefore(EARLIEST_POSSIBLE_DATE)) {
            createValidationError(request, errorList, "You have entered a date too far in the past. Please check the date and resubmit");
        }
    }

    public void validateTerminationDateAfterIncorporationDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, CompanyProfileApi companyProfile, AppointmentFullRecordAPI companyAppointment) {
        if (dto.getResignedOn().isBefore(companyProfile.getDateOfCreation())) {
            createValidationError(request, errorList, companyAppointment.getName() + " has not been found");
        }
    }

    /**
     * Check to ensure a request isn't being filed for an officer who has already resigned.
     * Used for Validation rules D19_9A/D19_9
     * @param request
     * @param errorList
     * @param companyAppointment
     */
    public void validateOfficerIsNotTerminated(HttpServletRequest request, List<ApiError> errorList, AppointmentFullRecordAPI companyAppointment){
        if(companyAppointment.getResignedOn() != null){
            createValidationError(request, errorList, "An application to remove " +
                    companyAppointment.getName() + " has already been submitted");
        }
    }

    public void validateCompanyNotDissolved(HttpServletRequest request, List<ApiError> errorList, CompanyProfileApi companyProfile) {
        if (Objects.equals(companyProfile.getCompanyStatus(), "dissolved")) {
            createValidationError(request, errorList, "You cannot remove an officer from a company that has been dissolved");
        }
        else if (companyProfile.getDateOfCessation() != null){
            createValidationError(request, errorList, "You cannot remove an officer from a company that is about to be dissolved");
        }
    }

    public Optional<LocalDate> getAppointmentDate(AppointmentFullRecordAPI companyAppointment) {
        if (companyAppointment.getIsPre1992Appointment() != null) {
            return Optional.ofNullable(
                    companyAppointment.getIsPre1992Appointment() ? companyAppointment.getAppointedBefore() : companyAppointment.getAppointedOn());
        }
        return Optional.empty();
    }

    public void validateTerminationDateAfterAppointmentDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI companyAppointment) {
        var companyAppointmentDate = getAppointmentDate(companyAppointment);
        if (companyAppointmentDate.isPresent()) {
            final var appointmentDate = companyAppointmentDate.get();
            if (dto.getResignedOn().isBefore(appointmentDate)) {
                createValidationError(request, errorList, "Date director was removed must be on or after the date the director was appointed");
            }
        }
    }

    public void validateAllowedCompanyType(HttpServletRequest request, List<ApiError> errorList, CompanyProfileApi companyProfile) {
        if (!ALLOWED_COMPANY_TYPES.contains(companyProfile.getType())) {
            createValidationError(request, errorList, String.format("You cannot remove an officer from a %s using this service", companyProfile.getType()));
        }
    }

    public void validateOfficerRole(HttpServletRequest request, List<ApiError> errorList, AppointmentFullRecordAPI companyAppointment) {
        if (!ALLOWED_OFFICER_ROLES.contains(companyAppointment.getOfficerRole())) {
            createValidationError(request, errorList, "You can only remove directors");
        }
    }

    private void createServiceError (HttpServletRequest request, List<ApiError> errorList) {
        final var apiError = new ApiError("The service is down. Try again later", request.getRequestURI(),
            LocationType.JSON_PATH.getValue(), ErrorType.SERVICE.getType());
        errorList.add(apiError);
    }

    private void createValidationError(HttpServletRequest request, List<ApiError> errorList, String errorMessage) {
        final var apiError = new ApiError(errorMessage, request.getRequestURI(),
                LocationType.JSON_PATH.getValue(), ErrorType.VALIDATION.getType());
        errorList.add(apiError);
    }
}
