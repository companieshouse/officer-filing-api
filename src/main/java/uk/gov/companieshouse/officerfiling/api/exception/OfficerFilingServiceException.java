package uk.gov.companieshouse.officerfiling.api.exception;

/**
 * Officer Filing processing exception.
 */
public class OfficerFilingServiceException extends RuntimeException {

    public OfficerFilingServiceException(final String s, final Exception e) {
        super(s, e);
    }
}
