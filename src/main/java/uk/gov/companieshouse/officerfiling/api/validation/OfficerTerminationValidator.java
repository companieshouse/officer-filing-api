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

/**
 * Provides all validation that should be carried out when an officer is terminated. Fetches all data necessary to complete
 * the validation and generates a list of errors that can be sent back to the caller.
 */
public class OfficerTerminationValidator {

    public static final LocalDate EARLIEST_POSSIBLE_DATE = LocalDate.of(2009, 10, 1);

    private final TransactionService transactionService;
    private final CompanyProfileService companyProfileService;
    private final CompanyAppointmentService companyAppointmentService;
    private final Logger logger;

    public OfficerTerminationValidator(final Logger logger, final TransactionService transactionService, final CompanyProfileService companyProfileService, final CompanyAppointmentService companyAppointmentService) {
        this.logger = logger;
        this.transactionService = transactionService;
        this.companyProfileService = companyProfileService;
        this.companyAppointmentService = companyAppointmentService;
    }

    /**
     * Main validation method to fetch the required data and validate the request. This should be the point of call when terminating an officer.
     * @param request The servlet request used in logging
     * @param dto Data Object containing details of the termination
     * @param transId Transaction ID linked to the transaction for this termination
     * @param passthroughHeader ERIC pass through header for authorisation
     * @return An object containing a list of any validation errors that have been raised
     */
    public ApiErrors validate(HttpServletRequest request, OfficerFilingDto dto, String transId, String passthroughHeader) {
        final var logMap = LogHelper.createLogMap(transId);
        logger.debugRequest(request, "POST", logMap);
        List<ApiError> errorList = new ArrayList<>();

        // Retrieve data objects required for the validation process
        final Transaction transaction = transactionService.getTransaction(transId, passthroughHeader);
        final AppointmentFullRecordAPI companyAppointment = validateOfficerIsIdentified(request, errorList, dto, transaction, passthroughHeader);
        final CompanyProfileApi companyProfile = companyProfileService.getCompanyProfile(transId, transaction.getCompanyNumber(), passthroughHeader);

        // Perform validation
        if (!(companyAppointment == null)) {
            validateMinResignationDate(request, errorList, dto);
            validateTerminationDateAfterIncorporationDate(request, errorList, dto, companyProfile, companyAppointment);
            validateTerminationDateAfterAppointmentDate(request, errorList, dto, companyAppointment);
            validateMinResignationDate(request, errorList, dto);
            validateTerminationDateAfterIncorporationDate(request, errorList, dto, companyProfile, companyAppointment);
            validateCompanyNotDissolved(request, errorList, companyProfile);
        }


        return new ApiErrors(errorList);
    }

    public AppointmentFullRecordAPI validateOfficerIsIdentified(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, Transaction transaction, String passthroughHeader) {
        try {
            final AppointmentFullRecordAPI companyAppointment = companyAppointmentService.getCompanyAppointment(transaction.getCompanyNumber(), dto.getReferenceAppointmentId(), passthroughHeader);
            return companyAppointment;
        }
        catch (CompanyAppointmentServiceException e){
            createValidationError(request, errorList,"Officer not found. Please confirm the details and resubmit");
        }
        return null;
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

    public void validateCompanyNotDissolved(HttpServletRequest request, List<ApiError> errorList, CompanyProfileApi companyProfile) {
        if (companyProfile.getDateOfCessation() != null){
            createValidationError(request, errorList, "You cannot remove a director from a company that's been dissolved");
        }
        if (Objects.equals(companyProfile.getCompanyStatus(), "dissolved")) {
            createValidationError(request, errorList, "You cannot remove a director from a company that's been dissolved or is about to be dissolved");
        }
    }

    public void validateTerminationDateAfterAppointmentDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI companyAppointment) {
        if (dto.getResignedOn().isBefore(companyAppointment.getAppointedOn())) {
            createValidationError(request, errorList, "Date director was removed must be on or after the date the director was appointed");
        }
    }

    private void createValidationError(HttpServletRequest request, List<ApiError> errorList, String errorMessage) {
        final var apiError = new ApiError(errorMessage, request.getRequestURI(),
                LocationType.JSON_PATH.getValue(), ErrorType.VALIDATION.getType());
        errorList.add(apiError);
    }

}