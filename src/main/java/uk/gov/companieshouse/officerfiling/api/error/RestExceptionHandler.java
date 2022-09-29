package uk.gov.companieshouse.officerfiling.api.error;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.ContentCachingRequestWrapper;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger CH_LOGGER = LoggerFactory.getLogger("officer-filing-api");

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            final HttpMessageNotReadableException ex, final HttpHeaders headers,
            final HttpStatus status, final WebRequest request) {

        final ContentCachingRequestWrapper nativeRequest =
                (ContentCachingRequestWrapper) ((ServletWebRequest) request).getNativeRequest();
        final String requestBodyAsString = new String(nativeRequest.getContentAsByteArray());

        final var cause =
                ex.getMostSpecificCause().getMessage().replaceAll("Source: (:?[^;]*); ", "");
        final var bodyError = buildRequestBodyError(cause, "$", null);

        bodyError.addErrorValue("body", requestBodyAsString);

        return ResponseEntity.status(status).body(new ApiErrors(List.of(bodyError)));
    }

    @ExceptionHandler(InvalidFilingException.class)
    public ResponseEntity<ApiErrors> handleInvalidFilingException(final InvalidFilingException ex) {
        final var fieldErrors = ex.getFieldErrors();

        final var errorList = fieldErrors.stream()
                .map(e -> buildRequestBodyError(e.getDefaultMessage(), getJsonPath(e),
                        e.getRejectedValue()))
                .collect(Collectors.toList());
        final var errors = new ApiErrors(errorList);

        return ResponseEntity.badRequest().body(errors);
    }

    private static String getJsonPath(final FieldError e) {
        return Optional.ofNullable(e.getCodes())
                .stream()
                .flatMap(Arrays::stream)
                .skip(1)
                .findFirst()
                .map(s -> s.replaceAll("^[^.]*", "\\$"))
                .orElse(null);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleTheException(final Exception ex, final Object body,
            final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        CH_LOGGER.error("INTERNAL ERROR", ex);
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    private static ApiError buildRequestBodyError(final String message, final String jsonPath,
            final Object rejectedValue) {
        final var error = new ApiError(message, jsonPath, LocationType.REQUEST_BODY.getValue(),
                ErrorType.VALIDATION.getType());

        Optional.ofNullable(rejectedValue)
                .map(Object::toString)
                .filter(Predicate.not(String::isEmpty))
                .ifPresent(r -> error.addErrorValue("rejected", r));

        return error;
    }

}
