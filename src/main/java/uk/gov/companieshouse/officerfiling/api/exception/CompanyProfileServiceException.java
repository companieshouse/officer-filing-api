package uk.gov.companieshouse.officerfiling.api.exception;

/**
 * Company profile not found or external query failed.
 */
public class CompanyProfileServiceException extends RuntimeException{

    public CompanyProfileServiceException(final String s, final Exception e) {
        super(s, e);
    }
}
