package uk.gov.companieshouse.officerfiling.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        logger.debugContext(transaction.getId(), "Patching filings", new LogHelper.Builder(transaction.getId())
                .withCompanyNumber(transaction.getCompanyNumber()).withFilingId(original.getId())
                .build());
        HashMap<String,String> fieldMap = new HashMap<>();
        OfficerFiling mergedFiling = null;
        // Get the current values of the original and patch filings, patch values will overwrite
        // Original values
        extractFields(original, fieldMap, transaction);
        extractFields(patch, fieldMap, transaction);
        // JavaTimeModule handles Instant serialisation
        var mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

        ObjectNode updatedFiling = mapper.createObjectNode();
        for(Map.Entry<String,String> entry : fieldMap.entrySet()){
            String field = entry.getKey();
            String value = entry.getValue();
            updatedFiling.put(field, value);
        }
        var updatedFilingJson = updatedFiling.toString();
        try {
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
    private void extractFields(OfficerFiling filing, HashMap<String,String> fieldMap, Transaction transaction){
        Class<OfficerFiling> yourClass = OfficerFiling.class;
        for (Method method : yourClass.getMethods()) {
            // Just call the getters
            String methodName = method.getName();
            if (methodName.startsWith("get")) {
                //Get the string representations of each field
                String fieldValue;
                try {
                    var fieldValueObject = method.invoke(filing, null);
                    if(fieldValueObject==null)
                        continue;
                    fieldValue = fieldValueObject.toString();
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new OfficerFilingServiceException("Failed to patch an officer filing for company "
                            + transaction.getCompanyNumber(), e);
                }
                //Get the name of the field
                var fieldName = methodName.substring(3);
                //Lower case the first character to match field name
                fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                //Add to our map
                fieldMap.put(fieldName, fieldValue);
            }
        }
        // Remove some extra entries here to avoid extra string comparisons during the merge
        // These will be added to the record on load and cause issues when converting from JSON
        fieldMap.remove("class");
        fieldMap.remove("links");
    }
}