package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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
            + "\"reference_officer_id\": \"id\","
            + "\"resigned_on\": \"2022-09-13\"";
    public static final String MALFORMED_JSON_QUOTED = "\"{\"";
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
                .referenceOfficerId("id")
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();
        final var filing = OfficerFiling.builder()
                .referenceEtag("etag")
                .referenceOfficerId("id")
                .resignedOn(Instant.parse("2022-09-13T00:00:00Z"))
                .build();
        final var locationUri = UriComponentsBuilder.fromPath("/")
                .pathSegment("transactions", TRANS_ID, "officers", FILING_ID)
                .build();

        transaction.setId(TRANS_ID);

        when(filingMapper.map(dto)).thenReturn(filing);
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(
                transaction);
        when(officerFilingService.save(any(OfficerFiling.class))).thenReturn(
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
        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(MALFORMED_JSON_QUOTED)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().json(
                        "{errors=[{\"error\":\"JSON parse error: [line: 1, column: 1]\","
                                + "\"error_values\":{\"rejected\":\"{\"},"
                                + "\"location\":\"$\","
                                + "\"location_type\":\"json-path\",\"type\":\"ch:validation\"}]}"));
    }

    @Test
    void createFilingWhenRequestBodyIncompleteThenResponse400() throws Exception {
        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content("{" + TM01_FRAGMENT)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().json(
                        "{\"errors\":[{\"error\":\"JSON parse error: [line: 1, column: 83]\","
                                + "\"error_values\":{\"rejected\":\"{\\\"reference_etag\\\": "
                                + "\\\"etag\\\",\\\"reference_officer_id\\\": \\\"id\\\","
                                + "\\\"resigned_on\\\": \\\"2022-09-13\"},\"location\":\"$\","
                                + "\"location_type\":\"json-path\",\"type\":\"ch:validation\"}]}"));
    }

    @Test
    void getFilingForReviewThenResponse200() throws Exception {
        final var dto = OfficerFilingDto.builder()
            .referenceEtag("etag")
            .referenceOfficerId("id")
            .resignedOn(LocalDate.of(2022, 9, 13))
            .build();
        final var filing = OfficerFiling.builder()
            .referenceEtag("etag")
            .referenceOfficerId("id")
            .resignedOn(Instant.parse("2022-09-13T00:00:00Z"))
            .build();

        when(officerFilingService.get(FILING_ID)).thenReturn(Optional.of(filing));

        when(filingMapper.map(filing)).thenReturn(dto);

        mockMvc.perform(get("/transactions/{id}/officers/{filingId}", TRANS_ID, FILING_ID)
            .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reference_etag", is("etag")))
            .andExpect(jsonPath("$.reference_officer_id", is("id")))
            .andExpect(jsonPath("$.resigned_on", is("2022-09-13")));
    }

    @Test
    void getFilingForReviewNotFoundThenResponse404() throws Exception {

        when(officerFilingService.get(FILING_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/transactions/{id}/officers/{filingId}", TRANS_ID, FILING_ID)
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
}