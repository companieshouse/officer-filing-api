package uk.gov.companieshouse.officerfiling.api.validation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;
import uk.gov.companieshouse.officerfiling.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.officerfiling.api.exception.TransactionServiceException;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

/**
 * Provides all validation that should be carried out when an officer is terminated. Fetches all data necessary to complete
 * the validation and generates a list of errors that can be sent back to the caller.
 */
public interface OfficerValidator {

    /**
     * Main validation method to fetch the required data and validate the request. This should be the point of call when terminating an officer.
     * @param request The servlet request used in logging
     * @param dto Data Object containing details of the termination
     * @param transaction the transaction for this termination
     * @param passthroughHeader ERIC pass through header for authorisation
     * @return An object containing a list of any validation errors that have been raised
     */
    public ApiErrors validate(HttpServletRequest request, OfficerFilingDto dto, Transaction transaction, String passthroughHeader);

    public void validateRequiredDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto);

    public void validateRequiredTransactionFields(HttpServletRequest request, List<ApiError> errorList, Transaction transaction);

    public Optional<AppointmentFullRecordAPI> getOfficerAppointment(HttpServletRequest request,
        List<ApiError> errorList, OfficerFilingDto dto, Transaction transaction, String passthroughHeader);

    public Optional<CompanyProfileApi> getCompanyProfile(HttpServletRequest request,
        List<ApiError> errorList, Transaction transaction, String passthroughHeader);

    public void validateCompanyNotDissolved(HttpServletRequest request, List<ApiError> errorList, CompanyProfileApi companyProfile);

    public void validateAllowedCompanyType(HttpServletRequest request, List<ApiError> errorList, CompanyProfileApi companyProfile);

    public void validateOfficerRole(HttpServletRequest request, List<ApiError> errorList, AppointmentFullRecordAPI companyAppointment);

    String getDirectorName(AppointmentFullRecordAPI appointment);

    void createServiceError (HttpServletRequest request, List<ApiError> errorList);

    void createValidationError(HttpServletRequest request, List<ApiError> errorList, String errorMessage);
}
