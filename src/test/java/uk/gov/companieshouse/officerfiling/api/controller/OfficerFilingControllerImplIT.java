package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;

@Tag("web")
@WebMvcTest(controllers = OfficerFilingControllerImpl.class)
class OfficerFilingControllerImplIT {
    private static final String TRANS_ID = "4f56fdf78b357bfc";
    private static final String FILING_ID = "632c8e65105b1b4a9f0d1f5e";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String TM01_FRAGMENT = "\"reference_etag\": \"etag\","
            + "\"reference_appointment_id\": \"id\","
            + "\"resigned_on\": \"2022-09-13\"";
    public static final String MALFORMED_JSON_QUOTED = "\"\"";
    private static final Instant FIRST_INSTANT = Instant.parse("2022-10-15T09:44:08.108Z");

    @MockBean
    private TransactionService transactionService;
    @MockBean
    private OfficerFilingService officerFilingService;
    @MockBean
    private OfficerFilingMapper filingMapper;
    @MockBean
    private Clock clock;
    @MockBean
    private Logger logger;

    private HttpHeaders httpHeaders;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", PASSTHROUGH_HEADER);
    }

    @Test
    void createFilingWhenTM01PayloadOKThenResponse201() throws Exception {
        final var body = "{" + TM01_FRAGMENT + "}";
        final var transaction = new Transaction();
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId("id")
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();
        final var filing = OfficerFiling.builder()
                .referenceEtag("etag")
                .referenceAppointmentId("id")
                .resignedOn(Instant.parse("2022-09-13T00:00:00Z"))
                .build();
        final var locationUri = UriComponentsBuilder.fromPath("/")
                .pathSegment("transactions", TRANS_ID, "officers", FILING_ID)
                .build();

        transaction.setId(TRANS_ID);

        when(filingMapper.map(dto)).thenReturn(filing);
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(
                transaction);
        when(officerFilingService.save(any(OfficerFiling.class), eq(TRANS_ID))).thenReturn(
                        OfficerFiling.builder(filing).id(FILING_ID)
                                .build()) // copy of 'filing' with id=FILING_ID
                .thenAnswer(i -> OfficerFiling.builder(i.getArgument(0))
                        .build()); // copy of first argument
        when(clock.instant()).thenReturn(FIRST_INSTANT);

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", locationUri.toUriString()))
                .andExpect(jsonPath("$").doesNotExist());
        verify(filingMapper).map(dto);
    }

    @Test
    void createFilingWhenRequestBodyMalformedThenResponse400() throws Exception {
        final var expectedError = createExpectedError(
                "JSON parse error: Cannot coerce empty String (\\\"\\\") to `uk.gov"
                        + ".companieshouse.officerfiling.api.model.dto.OfficerFilingDto$Builder` "
                        + "value (but could if coercion was enabled using `CoercionConfig`)\\n at"
                        + " [Source: (org.springframework.util.StreamUtils$NonClosingInputStream)"
                        + "; line: 1, column: 1]", "$", 1, 1);

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(MALFORMED_JSON_QUOTED)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]",
                        allOf(hasEntry("location", expectedError.getLocation()),
                                hasEntry("location_type", expectedError.getLocationType()),
                                hasEntry("type", expectedError.getType()))))
                .andExpect(jsonPath("$.errors[0].error", containsString(
                        "Cannot coerce empty String")))
                .andExpect(jsonPath("$.errors[0].error_values",
                        is(Map.of("offset", "line: 1, column: 1", "line", "1", "column", "1"))));
    }

    @Test
    void createFilingWhenDateUnparseableThenResponse400() throws Exception {
        final var body = "{" + TM01_FRAGMENT.replace("2022-09-13", "ABC") + "}";
        final var expectedError = createExpectedError(
                "JSON parse error:", "$..resigned_on", 1, 75);

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]",
                        allOf(hasEntry("location", expectedError.getLocation()),
                                hasEntry("location_type", expectedError.getLocationType()),
                                hasEntry("type", expectedError.getType()))))
                .andExpect(jsonPath("$.errors[0].error", containsString(
                        "JSON parse error: Cannot deserialize value of type `java.time.LocalDate`"
                                + " from String \"ABC\"")))
                .andExpect(jsonPath("$.errors[0].error_values",
                        is(Map.of("offset", "line: 1, column: 75", "line", "1", "column", "75"))));
    }

    @Test
    void createFilingWhenResignedOnInvalidThenResponse400() throws Exception {
        final var body = "{" + TM01_FRAGMENT.replace("2022-09-13", "3000-09-13") + "}";
        final var expectedError = createExpectedError(
                "JSON parse error:", "$.resigned_on", 1, 75);

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]",
                        allOf(hasEntry("location", expectedError.getLocation()),
                                hasEntry("location_type", expectedError.getLocationType()),
                                hasEntry("type", expectedError.getType()))))
                .andExpect(jsonPath("$.errors[0].error", containsString(
                        "must be a date in the past or in the present")))
                .andExpect(jsonPath("$.errors[0].error_values",
                        is(Map.of("rejected", "3000-09-13"))));
    }

    @Test
    void createFilingWhenResignedOnBlankThenResponse400() throws Exception {
        final var body = "{" + TM01_FRAGMENT.replace("2022-09-13", "") + "}";
        final var expectedError = createExpectedError(
                "JSON parse error:", "$.resigned_on", 1, 75);

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]",
                        allOf(hasEntry("location", expectedError.getLocation()),
                                hasEntry("location_type", expectedError.getLocationType()),
                                hasEntry("type", expectedError.getType()))))
                .andExpect(jsonPath("$.errors[0].error", containsString("must not be null")))
                .andExpect(jsonPath("$.errors[0].error_values", is(nullValue())));
    }

    @Test
    void createFilingWhenRequestBodyIncompleteThenResponse400() throws Exception {
        final var expectedError = createExpectedError(
                "JSON parse error: Unexpected end-of-input: expected close marker for Object "
                        + "(start marker at [Source: (org.springframework.util"
                        + ".StreamUtils$NonClosingInputStream); line: 1, column: 1])\n"
                        + " at [Source: (org.springframework.util"
                        + ".StreamUtils$NonClosingInputStream); line: 1, column: 87]", "$", 1, 87);

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content("{" + TM01_FRAGMENT)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]",
                        allOf(hasEntry("location", expectedError.getLocation()),
                                hasEntry("location_type", expectedError.getLocationType()),
                                hasEntry("type", expectedError.getType()))))
                .andExpect(jsonPath("$.errors[0].error", containsString(
                        "JSON parse error: Unexpected end-of-input: expected close marker for "
                                + "Object")))
                .andExpect(jsonPath("$.errors[0].error_values",
                        is(Map.of("offset", "line: 1, column: 87", "line", "1", "column", "87"))));
    }

    @Test
    void getFilingForReviewThenResponse200() throws Exception {
        final var dto = OfficerFilingDto.builder()
            .referenceEtag("etag")
            .referenceAppointmentId("id")
            .resignedOn(LocalDate.of(2022, 9, 13))
            .build();
        final var filing = OfficerFiling.builder()
            .referenceEtag("etag")
            .referenceAppointmentId("id")
            .resignedOn(Instant.parse("2022-09-13T00:00:00Z"))
            .build();

        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));

        when(filingMapper.map(filing)).thenReturn(dto);

        mockMvc.perform(get("/transactions/{id}/officers/{filingId}", TRANS_ID, FILING_ID)
            .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reference_etag", is("etag")))
            .andExpect(jsonPath("$.reference_appointment_id", is("id")))
            .andExpect(jsonPath("$.resigned_on", is("2022-09-13")));
    }

    @Test
    void getFilingForReviewNotFoundThenResponse404() throws Exception {

        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/transactions/{id}/officers/{filingId}", TRANS_ID, FILING_ID)
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    private ApiError createExpectedError(final String msg, final String location, final int line,
            final int column) {
        final var expectedError = new ApiError(msg, location, "json-path", "ch:validation");

        expectedError.addErrorValue("offset", String.format("line: %d, column: %d", line, column));
        expectedError.addErrorValue("line", String.valueOf(line));
        expectedError.addErrorValue("column", String.valueOf(column));

        return expectedError;
    }

}