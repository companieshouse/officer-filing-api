package uk.gov.companieshouse.officerfiling.api.error;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.util.List;
import java.util.Objects;
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
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.exception.TransactionServiceException;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {
    public static final String MALFORMED_JSON_QUOTED = "\"{\"";
    private static final String TM01_FRAGMENT = "{\"reference_etag\": \"etag\","
            + "\"reference_officer_id\": \"id\","
            + "\"resigned_on\": \"2022-09-13\"}";

    private RestExceptionHandler testExceptionHandler;

    @Mock
    private HttpHeaders headers;
    @Mock
    private ServletWebRequest request;
    @Mock
    private MismatchedInputException mismatchedInputException;
    @Mock
    private JsonParseException jsonParseException;

    private MockHttpServletRequest servletRequest;

    @Mock
    private JsonMappingException.Reference mappingReference;

    @BeforeEach
    void setUp() {
        testExceptionHandler = new RestExceptionHandler();
        servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/path/to/resource");
    }

    @Test
    void handleHttpMessageNotReadableWhenJsonMalformed() {
        final var message = new MockHttpInputMessage(MALFORMED_JSON_QUOTED.getBytes());
        final var exceptionMessage = new HttpMessageNotReadableException("Unexpected end-of-input: "
                + "expected close marker for Object (start marker at [Source: (org"
                + ".springframework.util.StreamUtils$NonClosingInputStream); line: 1, "
                + "column: 1])"
                + "\\n at [Source: (org.springframework.util"
                + ".StreamUtils$NonClosingInputStream); "
                + "line: 1, column: 2]", message);

        final var response =
                testExceptionHandler.handleHttpMessageNotReadable(exceptionMessage, headers,
                        HttpStatus.BAD_REQUEST, request);
        final var apiErrors = (ApiErrors) response.getBody();
        final var expectedError = new ApiError("test", "$", "json-path", "ch:validation");
        final var actualError = Objects.requireNonNull(apiErrors).getErrors().iterator().next();

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(actualError.getErrorValues(), is(nullValue()));
        assertThat(actualError, is(samePropertyValuesAs(expectedError, "error")));
        assertThat(actualError.getError(),
                containsString("Unexpected end-of-input: expected close marker"));
    }

    @Test
    void handleHttpMessageNotReadableWhenMismatchedInputException() {
        final var msg =
                "Cannot deserialize value of type `java.time.LocalDate` from String \"ABC\": "
                        + "Failed to deserialize java.time.LocalDate: (java.time.format"
                        + ".DateTimeParseException) Text 'ABC' could not be parsed at index 0";
        final var message = new MockHttpInputMessage("{]".getBytes());

        when(mismatchedInputException.getMessage()).thenReturn(msg);
        when(mismatchedInputException.getLocation()).thenReturn(new JsonLocation(null, 100, 3, 7));
        when(mismatchedInputException.getPath()).thenReturn(List.of(mappingReference));
        when(mappingReference.getFieldName()).thenReturn("resigned_on");

        final var exceptionMessage =
                new HttpMessageNotReadableException(msg, mismatchedInputException, message);
        final var response =
                testExceptionHandler.handleHttpMessageNotReadable(exceptionMessage, headers,
                        HttpStatus.BAD_REQUEST, request);
        final var apiErrors = (ApiErrors) response.getBody();
        final var expectedError =
                new ApiError("JSON parse error: " + msg, "$..resigned_on", "json-path",
                        "ch:validation");
        expectedError.addErrorValue("offset", "line: 3, column: 7");
        expectedError.addErrorValue("line", "3");
        expectedError.addErrorValue("column", "7");
        final var actualError = Objects.requireNonNull(apiErrors).getErrors().iterator().next();

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(actualError, is(samePropertyValuesAs(expectedError)));
        assertThat(actualError.getError(), containsString("Text 'ABC' could not be parsed"));
    }

    @Test
    void handleHttpMessageNotReadableWhenJsonParseException() {
        final var msg = "JsonParseException";
        final var message =
                new MockHttpInputMessage(TM01_FRAGMENT.replaceAll("2022", "ABC").getBytes());

        when(jsonParseException.getMessage()).thenReturn(msg);
        when(jsonParseException.getLocation()).thenReturn(new JsonLocation(null, 100, 3, 7));
//        when(jsonParseException.getPath()).thenReturn(List.of(mappingReference));
//        when(mappingReference.getFieldName()).thenReturn("resigned_on");

        final var exceptionMessage =
                new HttpMessageNotReadableException(msg, jsonParseException, message);

        final var response =
                testExceptionHandler.handleHttpMessageNotReadable(exceptionMessage, headers,
                        HttpStatus.BAD_REQUEST, request);

        final var apiErrors = (ApiErrors) response.getBody();
        final var expectedError =
                new ApiError("JSON parse error: " + msg, "$", "json-path", "ch:validation");
        expectedError.addErrorValue("offset", "line: 3, column: 7");
        expectedError.addErrorValue("line", "3");
        expectedError.addErrorValue("column", "7");
        final var actualError = Objects.requireNonNull(apiErrors).getErrors().iterator().next();

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(actualError, is(samePropertyValuesAs(expectedError)));
        assertThat(actualError.getError(), containsString("JSON parse error: JsonParseException"));
    }

    @Test
    void handleResourceNotFoundException() {
        final var exception = new ResourceNotFoundException("test resource missing");

        final var response =
                testExceptionHandler.handleResourceNotFoundException(exception);

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(response.hasBody(), is(false));
    }

    @Test
    void handleInvalidFilingException() {
        final var fieldError = new FieldError("object", "field", "error");
        final var codes = new String[]{"code1", "code2.addressLine1", "code3"};
        final var fieldErrorWithRejectedValue =
                new FieldError("object", "field", "rejectedValue", false, codes, null,
                        "errorWithRejectedValue");
        final var exception =
                new InvalidFilingException(List.of(fieldError, fieldErrorWithRejectedValue));

        final var apiErrors = testExceptionHandler.handleInvalidFilingException(exception);

        final var expectedError = new ApiError("error", null, "json-path", "ch:validation");
        final var expectedErrorWithRejectedValue =
                new ApiError("errorWithRejectedValue", "$.address_line_1", "json-path",
                        "ch:validation");

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
        final Object body = 0;

        when(request.resolveReference("request")).thenReturn(servletRequest);

        final var response =
                testExceptionHandler.handleExceptionInternal(exception, body, new HttpHeaders(),
                        HttpStatus.INTERNAL_SERVER_ERROR, request);

        final var apiErrors = (ApiErrors) response.getBody();
        final var expectedError =
                new ApiError("test", "/path/to/resource", "resource", "ch:service");

        assertThat(apiErrors, is(notNullValue()));
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