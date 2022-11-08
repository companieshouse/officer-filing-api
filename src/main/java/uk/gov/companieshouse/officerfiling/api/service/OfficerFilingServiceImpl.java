package uk.gov.companieshouse.officerfiling.api.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.repository.OfficerFilingRepository;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

/**
 * Store/retrieve Officer Filing entities using the persistence layer.
 */
@Service
public class OfficerFilingServiceImpl implements OfficerFilingService {
    private final OfficerFilingRepository repository;
    private final Logger logger;

    public OfficerFilingServiceImpl(final OfficerFilingRepository repository, Logger logger) {
        this.repository = repository;
        this.logger = logger;
    }

    /**
     * Store an OfficerFiling entity in persistence layer.
     *
     * @param filing        the OfficerFiling entity to store
     * @param transactionId the associated Transaction ID
     * @return the stored entity
     */
    @Override
    public OfficerFiling save(final OfficerFiling filing, final String transactionId) {
        final var logMap = LogHelper.createLogMap(transactionId, filing.getId());

        logger.debugContext(transactionId, "saving officer filing", logMap);

        return repository.save(filing);
    }

    /**
     * Retrieve a stored OfficerFiling entity by Filing ID.
     *
     * @param officerFilingId the Filing ID
     * @param transactionId   the associated Transaction ID
     * @return the stored entity if found
     */
    @Override
    public Optional<OfficerFiling> get(String officerFilingId, String transactionId) {
        final var logMap = LogHelper.createLogMap(transactionId, officerFilingId);

        logger.debugContext(transactionId, "getting officer filing", logMap);

        return repository.findById(officerFilingId);
    }

}
