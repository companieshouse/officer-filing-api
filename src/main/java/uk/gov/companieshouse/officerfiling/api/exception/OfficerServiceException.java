package uk.gov.companieshouse.officerfiling.api.exception;

public class OfficerServiceException extends Exception {
    public OfficerServiceException(String message) {
        super(message);
    }
    public OfficerServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
