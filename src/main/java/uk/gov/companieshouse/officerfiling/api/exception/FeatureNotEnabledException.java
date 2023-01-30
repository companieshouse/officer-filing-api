package uk.gov.companieshouse.officerfiling.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
/**
 * Feature not currently enabled.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class FeatureNotEnabledException extends RuntimeException {

}
