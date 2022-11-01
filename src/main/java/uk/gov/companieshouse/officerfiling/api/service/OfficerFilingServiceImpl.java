package uk.gov.companieshouse.officerfiling.api.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.repository.OfficerFilingRepository;

@Service
public class OfficerFilingServiceImpl implements OfficerFilingService {
    private final OfficerFilingRepository repository;
    private final Logger logger;

    public OfficerFilingServiceImpl(final OfficerFilingRepository repository, Logger logger) {
        this.repository = repository;
        this.logger = logger;
    }

    @Override
    public OfficerFiling save(final OfficerFiling filing, final String transactionId) {
        final var logMap = createLogMap(transactionId, filing.getId());
        logger.debugContext(transactionId, "saving officer filing", logMap);
        return repository.save(filing);
    }

    @Override
    public Optional<OfficerFiling> get(String officerFilingId, String transactionId) {
        final var logMap = createLogMap(transactionId, officerFilingId);
        logger.debugContext(transactionId, "getting officer filing", logMap);
        return repository.findById(officerFilingId);
    }

    private static Map<String, Object> createLogMap(String transactionId, String filingId) {
        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("transaction_id", transactionId);
        logMap.put("filing_id", filingId);
        return logMap;
    }
}
