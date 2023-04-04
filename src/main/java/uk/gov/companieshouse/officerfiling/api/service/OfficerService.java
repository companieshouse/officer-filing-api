package uk.gov.companieshouse.officerfiling.api.service;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.ServiceUnavailableException;

public interface OfficerService {

    /**
     * Query the officers service for a given company.
     *
     * @param transactionId the ID of the related transaction
     * @param companyNumber the Company Number
     * @param ericPassThroughHeader includes authorisation details
     * @return the company profile if found
     * @throws OfficerServiceException if not found or an error occurred
     * @throws ServiceUnavailableException if public API is unavailable
     */
    List<CompanyOfficerApi> getListOfActiveDirectorsDetails(final HttpServletRequest request, final String transactionId,
        final String companyNumber, final String ericPassThroughHeader)
            throws OfficerServiceException, ServiceUnavailableException;

}
