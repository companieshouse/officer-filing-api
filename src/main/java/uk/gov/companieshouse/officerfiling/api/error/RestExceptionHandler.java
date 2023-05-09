package uk.gov.companieshouse.officerfiling.api.error;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.exception.TransactionServiceException;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Handle exceptions caused by client REST requests, propagated from Spring or the service
 * controllers.
 * <ul>
 *     <li>JSON payload not readable/malformed</li>
 *     <li>{@link InvalidFilingException}</li>
 *     <li>{@link ResourceNotFoundException}</li>
 *     <li>{@link TransactionServiceException}</li>
 *     <li>other {@link RuntimeException}</li>
 *     <li>other internal exceptions</li>
 * </ul>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private final Logger chLogger;
    public static final String CAUSE = "cause";

    public RestExceptionHandler(final Logger logger) {
        this.chLogger = logger;
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            final HttpMessageNotReadableException ex, final HttpHeaders headers,
            final HttpStatus status, final WebRequest request) {
        final var cause = ex.getCause();
        final var baseMessage = "JSON parse error: ";
        final ApiError error;

        if (cause instanceof JsonProcessingException) {
            final var jpe = (JsonProcessingException) cause;
            final var msg = baseMessage + cause.getMessage();
            final var location = jpe.getLocation();
            var jsonPath = "$";

            if (cause instanceof MismatchedInputException) {
                final var fieldNameOpt = ((MismatchedInputException) cause).getPath()
                        .stream()
                        .findFirst()
                        .map(JsonMappingException.Reference::getFieldName);
                jsonPath += fieldNameOpt.map(f -> ".." + f).orElse("");
            }

            error = buildRequestBodyError(msg, jsonPath, null);
            addLocationInfo(error, location);
        }
        else {
            error = buildRequestBodyError(ex.getMostSpecificCause().getMessage(), "$", null);

        }
        logError(request, "Message not readable", ex);
        return ResponseEntity.badRequest().body(new ApiErrors(List.of(error)));
    }

    @ExceptionHandler(InvalidFilingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiErrors handleInvalidFilingException(final InvalidFilingException ex,
            WebRequest request) {
        final var fieldErrors = ex.getFieldErrors();

        final var errorList = fieldErrors.stream()
                .map(e -> buildRequestBodyError(e.getDefaultMessage(), getJsonPath(e),
                        e.getRejectedValue()))
                .collect(Collectors.toList());

        logError(request, "Invalid filing data", ex, errorList);
        return new ApiErrors(errorList);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
    public ResponseEntity<Void> handleResourceNotFoundException(final ResourceNotFoundException ex,
            final WebRequest request) {
        logError(request, "Resource not found", ex);
        return ResponseEntity.notFound()
                .build();
    }

    @ExceptionHandler(TransactionServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiErrors handleTransactionServiceException(final TransactionServiceException ex,
            final WebRequest request) {
        final var error = new ApiError(ex.getMessage(), getRequestURI(request),
                LocationType.RESOURCE.getValue(), ErrorType.SERVICE.getType());
        Optional.ofNullable(ex.getCause())
                .ifPresent(c -> error.addErrorValue(CAUSE, c.getMessage()));

        final var errorList = List.of(error);
        logError(request, "Transaction service error", ex, errorList);
        return new ApiErrors(errorList);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(final Exception ex, final Object body,
            final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        logError(request, "INTERNAL ERROR", ex);
        final var error = new ApiError(ex.getMessage(), getRequestURI(request),
                LocationType.RESOURCE.getValue(), ErrorType.SERVICE.getType());
        Optional.ofNullable(ex.getCause())
                .ifPresent(c -> error.addErrorValue(CAUSE, c.getMessage()));

        final var errorList = List.of(error);
        logError(request, "Internal error", ex, errorList);
        return super.handleExceptionInternal(ex, new ApiErrors(errorList), headers, status,
                request);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiErrors handleAllUncaughtException(final RuntimeException ex,
            final WebRequest request) {
        logError(request, "Unknown error occurred", ex);
        final var error = new ApiError(ex.getMessage(), getRequestURI(request),
                LocationType.RESOURCE.getValue(), ErrorType.SERVICE.getType());
        Optional.ofNullable(ex.getCause())
                .ifPresent(c -> error.addErrorValue(CAUSE, c.getMessage()));

        final var errorList = List.of(error);
        logError(request, "Unknown error", ex, errorList);
        return new ApiErrors(errorList);
    }

    private static void addLocationInfo(final ApiError error, final JsonLocation location) {
        error.addErrorValue("offset", location.offsetDescription());
        error.addErrorValue("line", String.valueOf(location.getLineNr()));
        error.addErrorValue("column", String.valueOf(location.getColumnNr()));
    }

    private static String getJsonPath(final FieldError e) {
        return Optional.ofNullable(e.getCodes())
                .stream()
                .flatMap(Arrays::stream)
                .skip(1)
                .findFirst()
                .map(s -> s.replaceAll("^[^.]*", "\\$"))
                .map(s -> s.replaceAll("([A-Z0-9]+)", "_$1").toLowerCase())
                .orElse(null);
    }

    private static String getRequestURI(final WebRequest request) {
        // resolveReference("request") preferred over getRequest() because the latter method is
        // final and cannot be stubbed with Mockito
        return Optional.ofNullable((HttpServletRequest) request.resolveReference("request"))
                .map(HttpServletRequest::getRequestURI)
                .orElse(null);
    }

    private static ApiError buildRequestBodyError(final String message, final String jsonPath,
            final Object rejectedValue) {
        final var error = new ApiError(message, jsonPath, LocationType.JSON_PATH.getValue(),
                ErrorType.VALIDATION.getType());

        Optional.ofNullable(rejectedValue)
                .map(Object::toString)
                .filter(Predicate.not(String::isEmpty))
                .ifPresent(r -> error.addErrorValue("rejected", r));

        return error;
    }

    private void logError(WebRequest request, String msg, Exception ex) {
        logError(request, msg, ex, null);
    }

    private void logError(WebRequest request, String msg, Exception ex, @Nullable List<ApiError> apiErrorList) {
        final var servletRequest = ((ServletWebRequest) request).getRequest();

        if (apiErrorList != null && !apiErrorList.isEmpty()) {
            msg += apiErrorList.stream()
                    .map(apiError -> " - " + apiError.getErrorValues())
                    .collect(Collectors.joining());
        }

        chLogger.errorRequest(servletRequest, ex, new LogHelper.Builder("") // transaction ID not available here for context
                .withMessage(msg)
                .withRequest(servletRequest)
                .build());
    }

}