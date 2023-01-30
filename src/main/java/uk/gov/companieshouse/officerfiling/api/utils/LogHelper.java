package uk.gov.companieshouse.officerfiling.api.utils;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.util.LogContext;
import uk.gov.companieshouse.logging.util.LogContextProperties;

import javax.servlet.http.HttpServletRequest;

@Component
public final class LogHelper {

    private enum Key {
        OFFSET,
        TRANSACTION_ID,
        FILING_ID,
        COMPANY_NUMBER,
        COMPANY_NAME;
    }

    private LogHelper() {
        // intentionally blank
    }

    /**
     * Create a mutable property map required by CH logging methods. Transaction ID is a required field and all others
     * are optional.
     */
    public static class Builder {
        private final Map<String, Object> logMap = new HashMap<>();

        /**
         * Initialise the Builder with just the Transaction ID
         */
        public Builder(String transactionId) {
            addToLogMap(Key.TRANSACTION_ID, transactionId);
        }

        /**
         * Initialise the Builder with the Transaction ID and the Company Name and Number
         */
        public Builder(Transaction transaction) {
            addToLogMap(Key.TRANSACTION_ID, transaction.getId());
            addToLogMap(Key.COMPANY_NAME, transaction.getCompanyName());
            addToLogMap(Key.COMPANY_NUMBER, transaction.getCompanyNumber());
        }

        /**
         * Set default logging fields from the request
         */
        public Builder withRequest(HttpServletRequest request) {
            addToLogMap(LogContextProperties.REQUEST_PATH_KEY, request.getRequestURI());
            addToLogMap(LogContextProperties.USER_ID_KEY, request.getRemoteUser());
            addToLogMap(LogContextProperties.REQUEST_METHOD_KEY, request.getMethod());
            return this;
        }

        public Builder withMessage(String message) {
            addToLogMap(LogContextProperties.LOG_MSG_KEY, message);
            return this;
        }

        public Builder withStart(String start) {
            addToLogMap(LogContextProperties.START_TIME_KEY, start);
            return this;
        }

        public Builder withEnd(String end) {
            addToLogMap(LogContextProperties.END_TIME_KEY, end);
            return this;
        }

        public Builder withDuration(String duration) {
            addToLogMap(LogContextProperties.RESPONSE_TIME_KEY, duration);
            return this;
        }

        public Builder withStatus(String status) {
            addToLogMap(LogContextProperties.STATUS_CODE_KEY, status);
            return this;
        }

        public Builder withMethod(String method) {
            addToLogMap(LogContextProperties.REQUEST_METHOD_KEY, method);
            return this;
        }

        public Builder withPath(String path) {
            addToLogMap(LogContextProperties.REQUEST_PATH_KEY, path);
            return this;
        }

        public Builder withOffset(String offset) {
            addToLogMap(Key.OFFSET, offset);
            return this;
        }

        public Builder withIdentity(String identity) {
            addToLogMap(LogContextProperties.USER_ID_KEY, identity);
            return this;
        }

        public Builder withFilingId(String filingId) {
            addToLogMap(Key.FILING_ID, filingId);
            return this;
        }

        public Builder withCompanyNumber(String companyNumber) {
            addToLogMap(Key.COMPANY_NUMBER, companyNumber);
            return this;
        }

        public Builder withCompanyName(String companyName) {
            addToLogMap(Key.COMPANY_NAME, companyName);
            return this;
        }

        public Map<String, Object> build() {
            return logMap;
        }
        
        private void addToLogMap(Key key, String field) {
            if (field == null || field.isBlank()) {
                return;
            }
            logMap.put(key.name(), field);
        }

        private void addToLogMap(LogContextProperties key, String field) {
            if (field == null || field.isBlank()) {
                return;
            }
            logMap.put(key.value(), field);
        }
    }

}