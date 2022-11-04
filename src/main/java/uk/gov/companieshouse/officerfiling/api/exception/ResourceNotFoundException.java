package uk.gov.companieshouse.officerfiling.api.exception;

/**
 * Officer Filing resource not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
    }

    public ResourceNotFoundException(final String message) {
        super(message);
    }
}
