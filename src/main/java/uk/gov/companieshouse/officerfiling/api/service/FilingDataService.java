package uk.gov.companieshouse.officerfiling.api.service;

import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;

public interface FilingDataService {

    FilingApi generateOfficerFiling(String transactionId, String filingId);
}
