package uk.gov.companieshouse.officerfiling.api.error;

/**
 * Validation error types.
 */
public enum ErrorType {

    SERVICE("ch:service"),
    VALIDATION("ch:validation");

    private final String type;

    ErrorType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
