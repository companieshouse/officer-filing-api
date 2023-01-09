package uk.gov.companieshouse.officerfiling.api.controller;

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
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

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

@Tag("web")
@WebMvcTest(controllers = OfficerFilingControllerImpl.class)
class OfficerFilingControllerImplIT {
    private static final String TRANS_ID = "4f56fdf78b357bfc";
    private static final String FILING_ID = "632c8e65105b1b4a9f0d1f5e";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String TM01_FRAGMENT = "\"reference_etag\": \"etag\","
            + "\"reference_appointment_id\": \"" + FILING_ID + "\","
            + "\"resigned_on\": \"2022-09-13\"";
    public static final String MALFORMED_JSON_QUOTED = "\"\"";
    private static final Instant FIRST_INSTANT = Instant.parse("2022-10-15T09:44:08.108Z");
    private static final String COMPANY_NUMBER = "123456";
    public static final LocalDate INCORPORATION_DATE = LocalDate.of(2010, Month.OCTOBER, 20);
    public static final String DIRECTOR_NAME = "Director name";
    private static final String ETAG = "etag";

    @MockBean
    private TransactionService transactionService;
    @MockBean
    private OfficerFilingService officerFilingService;
    @MockBean
    private OfficerFilingMapper filingMapper;
    @MockBean
    private CompanyProfileService companyProfileService;
    @MockBean
    private CompanyAppointmentService companyAppointmentService;
    @MockBean
    private Clock clock;
    @MockBean
    private Logger logger;

    private HttpHeaders httpHeaders;
    private Transaction transaction;
    private CompanyProfileApi companyProfileApi;
    private AppointmentFullRecordAPI companyAppointment;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", PASSTHROUGH_HEADER);

        transaction = new Transaction();
        transaction.setCompanyNumber(COMPANY_NUMBER);
        transaction.setId(TRANS_ID);
        companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setDateOfCreation(INCORPORATION_DATE);
        companyAppointment = new AppointmentFullRecordAPI();
        companyAppointment.setName(DIRECTOR_NAME);
        companyAppointment.setEtag(ETAG);
    }

    @Test
    void createFilingWhenTM01PayloadOKThenResponse201() throws Exception {
        final var body = "{" + TM01_FRAGMENT + "}";
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();
        final var filing = OfficerFiling.builder()
                .referenceEtag("etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(Instant.parse("2022-09-13T00:00:00Z"))
                .build();
        final var locationUri = UriComponentsBuilder.fromPath("/")
                .pathSegment("transactions", TRANS_ID, "officers", FILING_ID)
                .build();

        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointmentService.getCompanyAppointment(COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);
        when(filingMapper.map(dto)).thenReturn(filing);
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
        response400BaseTest("ABC");
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
                        is(Map.of("offset", "line: 1, column: 109", "line", "1", "column", "109"))));
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

    @Test
    void createFilingWhenResignedOnDateTodayOKThenResponse201() throws Exception {
        final Instant resignedToday = Instant.now();
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(Date.from(resignedToday));
        final var body = "{" + TM01_FRAGMENT.replace("2022-09-13", formattedDate) + "}";
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.now())
                .build();
        final var filing = OfficerFiling.builder()
                .referenceEtag("etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(resignedToday)
                .build();
        final var locationUri = UriComponentsBuilder.fromPath("/")
                .pathSegment("transactions", TRANS_ID, "officers", FILING_ID)
                .build();

        when(filingMapper.map(dto)).thenReturn(filing);
        when(officerFilingService.save(any(OfficerFiling.class), eq(TRANS_ID))).thenReturn(
                        OfficerFiling.builder(filing).id(FILING_ID)
                                .build()) // copy of 'filing' with id=FILING_ID
                .thenAnswer(i -> OfficerFiling.builder(i.getArgument(0))
                        .build()); // copy of first argument
        when(clock.instant()).thenReturn(FIRST_INSTANT);
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointmentService.getCompanyAppointment(COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

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
    void createFilingWhenDateIncorrectFormatThenResponse400() throws Exception {
        response400BaseTest("2022-09-131");
    }

    @Test
    void createFilingWhenDateUsingAmericanDateFormatThenResponse400() throws Exception {
        response400BaseTest("2022-13-09");
    }

    @Test
    void createFilingWhenResignedOnDate300yearsAgoThenResponse400() throws Exception {
        companyProfileApi.setDateOfCreation(LocalDate.of(1721, Month.OCTOBER, 20));
        when(transactionService.getTransaction(any(String.class), any(String.class))).thenReturn(transaction);
        when(companyProfileService.getCompanyProfile(any(String.class), any(String.class), any(String.class))).thenReturn(companyProfileApi);
        when(companyAppointmentService.getCompanyAppointment(any(String.class), any(String.class), any(String.class))).thenReturn(companyAppointment);

        final var body = "{" + TM01_FRAGMENT.replace("2022-09-13", "1722-09-13") + "}";
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
                        allOf(hasEntry("location_type", expectedError.getLocationType()),
                                hasEntry("type", expectedError.getType()))))
                .andExpect(jsonPath("$.errors[0].error", containsString("You have entered a date too far in the past. Please check the date and resubmit")))
                .andExpect(jsonPath("$.errors[0].error_values", is(nullValue())));
    }

    @Test
    void createFilingWhenDateContainsTwoCharacterYearThenResponse400() throws Exception {
        response400BaseTest("22-09-13");
    }
    @Test
    void createFilingWhenDateContainsSpecialcharactersThenResponse400() throws Exception {
        response400BaseTest("2022-!@-%^");
    }

    @Test
    void createFilingWhenDateContainsMonthAsNameThenResponse400() throws Exception {
        String formattedDate = new SimpleDateFormat("yyyy-MMM-dd").format(Date.from(Instant.now()));
        response400BaseTest(formattedDate);
    }

    @Test
    void createFilingWhenDateFormatIsddMMyyyyThenResponse400() throws Exception {
        String formattedDate = new SimpleDateFormat("dd-MM-yyyy").format(Date.from(Instant.now()));
        response400BaseTest(formattedDate);

    }

    private void response400BaseTest(String replacementString) throws Exception {
        final var body = "{" + TM01_FRAGMENT.replace("2022-09-13", replacementString) + "}";
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
                                + " from String \"" + replacementString + "\"")))
                .andExpect(jsonPath("$.errors[0].error_values",
                        is(Map.of("offset", "line: 1, column: 97", "line", "1", "column", "97"))));
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