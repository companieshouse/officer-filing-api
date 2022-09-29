package uk.gov.companieshouse.officerfiling.api.error;

import java.util.Collections;
import java.util.List;
import org.springframework.validation.FieldError;

public class InvalidFilingException extends RuntimeException {
    private final List<FieldError> fieldErrors;

    public InvalidFilingException(final List<FieldError> fieldErrors) {

        this.fieldErrors = fieldErrors;
    }

    public List<FieldError> getFieldErrors() {
        return Collections.unmodifiableList(fieldErrors);
    }
}
