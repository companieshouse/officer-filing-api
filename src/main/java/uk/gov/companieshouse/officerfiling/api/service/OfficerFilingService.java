package uk.gov.companieshouse.officerfiling.api.service;

import java.util.Optional;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;

public interface OfficerFilingService {
    OfficerFiling save(OfficerFiling filing, String transactionId);

    Optional<OfficerFiling> get(String officerFilingId, String transactionId);

}
