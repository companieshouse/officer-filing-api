package uk.gov.companieshouse.officerfiling.api.service;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.ServiceUnavailableException;

/**
 * Produces a list of Directors from a specified company.
 */
public interface OfficerService {

    /**
     * Retrieves list of active Directors
     *
     * @param companyNumber the company number
     * @param request the HTTP request
     * @return the Officers if found
     *
     * @throws OfficerServiceException if Officers not found or an error occurred
     */
    List<CompanyOfficerApi> getListOfActiveDirectorsDetails(final HttpServletRequest request, final String transactionId,
        final String companyNumber, final String ericPassThroughHeader)
            throws OfficerServiceException, ServiceUnavailableException;

}
