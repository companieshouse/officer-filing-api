package uk.gov.companieshouse.officerfiling.api.service;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.model.entity.ActiveOfficerDetails;

/**
 * Produces Filing Data format for consumption as JSON by filing-resource-handler external service.
 */
public interface OfficerService {

    /**
     * Create FilingApi data from a retrieved Officer Filing resource.
     *
     * @param companyNumber the company number
     * @param request the HTTP request
     * @return the Officers if found
     *
     * @throws OfficerServiceException if Officers not found or an error occurred
     */
    List<ActiveOfficerDetails> getListActiveDirectorDetails(HttpServletRequest request, String companyNumber) throws OfficerServiceException;
}
