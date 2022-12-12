package uk.gov.companieshouse.officerfiling.api.exception;

/**
 * Appointment not found or external query failed.
 */
public class CompanyAppoinmentServiceException extends RuntimeException{

    public CompanyAppoinmentServiceException(final String s, final Exception e) {
        super(s, e);
    }
}
