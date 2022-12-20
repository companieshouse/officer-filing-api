package uk.gov.companieshouse.officerfiling.api.exception;

/**
 * Appointment not found or external query failed.
 */
public class CompanyAppointmentServiceException extends RuntimeException{

    public CompanyAppointmentServiceException(final String s, final Exception e) {
        super(s, e);
    }
}
