package uk.gov.companieshouse.officerfiling.api.service;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.model.entity.ActiveOfficerDetails;

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
    List<ActiveOfficerDetails> getListActiveDirectorsDetails(HttpServletRequest request, String companyNumber) throws OfficerServiceException;
}
