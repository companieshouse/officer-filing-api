package uk.gov.companieshouse.officerfiling.api.exception;

public class TransactionServiceException extends RuntimeException {

    public TransactionServiceException(final String s, final Exception e) {
        super(s, e);
    }
}
