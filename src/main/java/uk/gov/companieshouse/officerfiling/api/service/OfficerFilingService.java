package uk.gov.companieshouse.officerfiling.api.service;

import java.util.Optional;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;

import javax.servlet.http.HttpServletRequest;

/**
 * Store/retrieve Officer Filing entities using the persistence layer.
 */
public interface OfficerFilingService {
    OfficerFiling save(OfficerFiling filing, String transactionId);

    Optional<OfficerFiling> get(String officerFilingId, String transactionId);

    OfficerFiling mergeFilings(OfficerFiling original, OfficerFiling patch, Transaction transaction);

    boolean requestMatchesResourceSelf(HttpServletRequest request, OfficerFiling filing);
}
