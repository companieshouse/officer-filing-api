package uk.gov.companieshouse.officerfiling.api.error;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.officerfiling.api.exception.TransactionServiceException;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {
    public static final String MALFORMED_JSON_QUOTED = "\"{\"";
    private RestExceptionHandler testExceptionHandler;

    @Mock
    private HttpHeaders headers;
    @Mock
    private ServletWebRequest request;
    @Mock
    private ContentCachingRequestWrapper requestWrapper;

    private MockHttpServletRequest servletRequest;

    @BeforeEach
    void setUp() {
        testExceptionHandler = new RestExceptionHandler();
        servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/path/to/resource");
    }

    @Test
    void handleHttpMessageNotReadableWithRedactedMessage() {
        final var message = new MockHttpInputMessage(MALFORMED_JSON_QUOTED.getBytes());
        final var exceptionWithNamedClass = new HttpMessageNotReadableException(
                "Unexpected end-of-input: "
                        + "expected close marker for Object (start marker at [Source: (org"
                        + ".springframework.util.StreamUtils$NonClosingInputStream); line: 1, "
                        + "column: 1])"
                        + "\\n at [Source: (org.springframework.util"
                        + ".StreamUtils$NonClosingInputStream); "
                        + "line: 1, column: 2]", message);

        when(request.getNativeRequest()).thenReturn(requestWrapper);
        when(requestWrapper.getContentAsByteArray()).thenReturn(MALFORMED_JSON_QUOTED.getBytes());

        final var response =
                testExceptionHandler.handleHttpMessageNotReadable(exceptionWithNamedClass, headers,
                        HttpStatus.BAD_REQUEST, request);
        final var apiErrors = (ApiErrors) response.getBody();
        final var expectedError = new ApiError("test", "$", "json-path", "ch:validation");
        final var actualError = apiErrors.getErrors().iterator().next();

        expectedError.addErrorValue("body", MALFORMED_JSON_QUOTED);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(apiErrors.getErrors(), hasSize(1));
        assertThat(actualError, is(samePropertyValuesAs(expectedError, "error")));
        assertThat(actualError.getError(), not(containsString(
                "Source: (org.springframework.util.StreamUtils$NonClosingInputStream);")));
    }

    @Test
    void handleInvalidFilingException() {
        final var fieldError = new FieldError("object", "field", "error");
        final String[] codes = {"code1", "code2.name", "code3"};
        final var fieldErrorWithRejectedValue =
                new FieldError("object", "field", "rejectedValue", false, codes, null,
                        "errorWithRejectedValue");
        final var exception =
                new InvalidFilingException(List.of(fieldError, fieldErrorWithRejectedValue));

        final var apiErrors = testExceptionHandler.handleInvalidFilingException(exception);

        final var expectedError = new ApiError("error", null, "json-path", "ch:validation");
        final var expectedErrorWithRejectedValue =
                new ApiError("errorWithRejectedValue", "$.name", "json-path", "ch:validation");

        expectedErrorWithRejectedValue.addErrorValue("rejected", "rejectedValue");

        assertThat(apiErrors.getErrors(), contains(expectedError, expectedErrorWithRejectedValue));
    }

    @ParameterizedTest(name = "[{index}]: cause={0}")
    @NullSource
    @MethodSource("causeProvider")
    void handleTransactionServiceException(final Exception cause) {
        final var exception = new TransactionServiceException("test", cause);

        when(request.resolveReference("request")).thenReturn(servletRequest);

        final var apiErrors =
                testExceptionHandler.handleTransactionServiceException(exception, request);

        final var expectedError =
                new ApiError("test", "/path/to/resource", "resource", "ch:service");

        if (cause != null) {
            expectedError.addErrorValue("cause", cause.getMessage());
        }
        assertThat(apiErrors.getErrors(), contains(expectedError));
    }

    @Test
    void handleExceptionInternal() {
        final var exception = new NullPointerException("test");
        final Object body = Integer.valueOf(0);

        when(request.resolveReference("request")).thenReturn(servletRequest);

        final var response =
                testExceptionHandler.handleExceptionInternal(exception, body, new HttpHeaders(),
                        HttpStatus.INTERNAL_SERVER_ERROR, request);

        final var apiErrors = (ApiErrors) response.getBody();
        final var expectedError =
                new ApiError("test", "/path/to/resource", "resource", "ch:service");

        assertThat(apiErrors.getErrors(), contains(expectedError));
    }

    @ParameterizedTest(name = "[{index}]: cause={0}")
    @NullSource
    @MethodSource("causeProvider")
    void handleAllUncaughtException(final Exception cause) {
        final var exception = new RuntimeException("test", cause);

        when(request.resolveReference("request")).thenReturn(servletRequest);

        final var apiErrors = testExceptionHandler.handleAllUncaughtException(exception, request);

        final var expectedError =
                new ApiError("test", "/path/to/resource", "resource", "ch:service");

        if (cause != null) {
            expectedError.addErrorValue("cause", cause.getMessage());
        }
        assertThat(apiErrors.getErrors(), contains(expectedError));
    }

    private static Stream<Arguments> causeProvider() {
        return Stream.of(Arguments.of(new ArithmeticException("DIV/0")));
    }

}