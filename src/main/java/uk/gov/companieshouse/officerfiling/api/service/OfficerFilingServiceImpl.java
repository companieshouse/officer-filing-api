package uk.gov.companieshouse.officerfiling.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerFilingServiceException;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.repository.OfficerFilingRepository;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;
import uk.gov.companieshouse.officerfiling.api.utils.MapHelper;

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
        logger.debugContext(transactionId, "Saving officer filing", new LogHelper.Builder(transactionId)
                .withFilingId(filing.getId())
                .build());
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
        logger.debugContext(transactionId, "Getting officer filing", new LogHelper.Builder(transactionId)
                .withFilingId(officerFilingId)
                .build());
        return repository.findById(officerFilingId);
    }

    /**
     * Merges the contents of an OfficerFiling patch into the original record.
     * @param original The base record
     * @param patch A record with updated values
     * @return The merged record
     */
    @Override
    public OfficerFiling mergeFilings(OfficerFiling original, OfficerFiling patch, Transaction transaction) {
        logger.debugContext(transaction.getId(), "Patching filings", new LogHelper.Builder(transaction)
                .withFilingId(original.getId())
                .build());
        HashMap<String,Object> fieldMap = new HashMap<>();
        OfficerFiling mergedFiling;
        // Get the current values of the original and patch filings, patch values will overwrite
        // Original values
        extractFields(original, fieldMap);
        extractFields(patch, fieldMap);
        // JavaTimeModule handles Instant serialisation
        var mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

        try {
            var updatedFilingJson = new ObjectMapper().writeValueAsString(fieldMap);
            mergedFiling = mapper.readerFor(OfficerFiling.class).readValue(updatedFilingJson);
        } catch (JsonProcessingException e) {
            throw new OfficerFilingServiceException("Failed to patch an officer filing for company "
                    + transaction.getCompanyNumber(), e);
        }
        return mergedFiling;
    }

    /**
     * Extracts the fields from an OfficerFiling object and adds them to the given map
     */
    private void extractFields(OfficerFiling filing, HashMap<String,Object> fieldMap){
        Map<String, Object> patchMap = MapHelper.convertObject(filing, PropertyNamingStrategies.LOWER_CAMEL_CASE);
        // Convert the data object to a map
        Map<String, Object> patchDataMap = MapHelper.convertObject(filing.getData(), PropertyNamingStrategies.LOWER_CAMEL_CASE);
        // Merge the patch map with the existing map, if it exists
        Map<String, Object> mergedDataMap = new HashMap<>();
        Map<String, Object> originalDataMap = (Map<String, Object>) fieldMap.get("data");
        if(originalDataMap != null){
            mergedDataMap.putAll(originalDataMap);
        }
        mergedDataMap.putAll(patchDataMap);
        fieldMap.putAll(patchMap);
        fieldMap.put("data", mergedDataMap);
        // Remove some extra entries here to avoid extra string comparisons during the merge
        // These will be added to the record on load and cause issues when converting from JSON
        fieldMap.remove("class");
        fieldMap.remove("links");
    }
}
