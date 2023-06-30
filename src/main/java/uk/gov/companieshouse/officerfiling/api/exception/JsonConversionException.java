package uk.gov.companieshouse.officerfiling.api.exception;

public class JsonConversionException extends RuntimeException {

  public JsonConversionException() {
  }

  public JsonConversionException(final String message) {
    super(message);
  }
}
