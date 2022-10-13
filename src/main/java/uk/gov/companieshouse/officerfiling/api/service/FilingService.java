package uk.gov.companieshouse.officerfiling.api.service;

import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;

public interface FilingService {

    public FilingApi generateOfficerFiling(String filingId);
}
