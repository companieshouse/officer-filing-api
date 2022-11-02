package uk.gov.companieshouse.officerfiling.api.utils;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LogHelper {

    public Map<String, Object> createLogMap(String transactionId, String filingId) {
        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("transaction_id", transactionId);
        logMap.put("filing_id", filingId);
        return logMap;
    }
}
