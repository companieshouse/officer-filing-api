package uk.gov.companieshouse.officerfiling.api.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
    }

    public ResourceNotFoundException(final String message) {
        super(message);
    }
}
