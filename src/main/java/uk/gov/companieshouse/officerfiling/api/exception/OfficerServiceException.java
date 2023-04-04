package uk.gov.companieshouse.officerfiling.api.exception;

/**
 * Company profile not found or external query failed.
 */
public class OfficerServiceException extends RuntimeException{

    public OfficerServiceException(final String message) {
        super(message);
    }

    public OfficerServiceException(final String s, final Exception e) {
        super(s, e);
    }
}
