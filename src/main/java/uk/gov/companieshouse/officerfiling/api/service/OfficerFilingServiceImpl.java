package uk.gov.companieshouse.officerfiling.api.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.repository.OfficerFilingRepository;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

@Service
public class OfficerFilingServiceImpl implements OfficerFilingService {
    private final OfficerFilingRepository repository;
    private final Logger logger;
    private final LogHelper logHelper;

    public OfficerFilingServiceImpl(final OfficerFilingRepository repository, Logger logger, LogHelper logHelper) {
        this.repository = repository;
        this.logger = logger;
        this.logHelper = logHelper;
    }

    @Override
    public OfficerFiling save(final OfficerFiling filing, final String transactionId) {
        final var logMap = logHelper.createLogMap(transactionId, filing.getId());
        logger.debugContext(transactionId, "saving officer filing", logMap);
        return repository.save(filing);
    }

    @Override
    public Optional<OfficerFiling> get(String officerFilingId, String transactionId) {
        final var logMap = logHelper.createLogMap(transactionId, officerFilingId);
        logger.debugContext(transactionId, "getting officer filing", logMap);
        return repository.findById(officerFilingId);
    }

}
