package uk.gov.companieshouse.officerfiling.api.exception;

public class ServiceUnavailableException extends RuntimeException {

  public ServiceUnavailableException() {
  }

  public ServiceUnavailableException(final String message) {
    super(message);
  }
}
