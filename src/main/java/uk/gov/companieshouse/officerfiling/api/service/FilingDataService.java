package uk.gov.companieshouse.officerfiling.api.service;

import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;

/**
 * Produces Filing Data format for consumption as JSON by filing-resource-handler external service.
 */
public interface FilingDataService {

    /**
     * Create FilingApi data from a retrieved Officer Filing resource.
     *
     * @param transactionId the Transaction ID
     * @param filingId      the Officer Filing ID
     * @return the FilingApi resource
     */
    FilingApi generateOfficerFiling(String transactionId, String filingId, String ericPassThroughHeader);
}
