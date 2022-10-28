package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;

@Tag("web")
@WebMvcTest(controllers = OfficerFilingControllerImpl.class)
class OfficerFilingControllerImplValidationIT {
    private static final String TRANS_ID = "4f56fdf78b357bfc";
    private static final String FILING_ID = "632c8e65105b1b4a9f0d1f5e";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String TM01_FRAGMENT = "\"reference_etag\": \"etag_value\","
            + "\"reference_appointment_id\": \"id_value\","
            + "\"resigned_on\": \"2022-09-13\"";

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
    void createFilingWhenReferenceEtagBlankThenResponse400() throws Exception {
        final var body = "{" + TM01_FRAGMENT.replace("etag_value", "") + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].location", is("$.reference_etag")))
                .andExpect(jsonPath("$.errors[0].error_values", is(nullValue())))
                .andExpect(jsonPath("$.errors[0].error", is("must not be blank")));
    }

    @Test
    void createFilingWhenReferenceReferenceAppointmentIdBlankThenResponse400() throws Exception {
        final var body = "{" + TM01_FRAGMENT.replace("id_value", "") + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                .contentType("application/json")
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
            .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
            .andExpect(jsonPath("$.errors[0].location", is("$.reference_appointment_id")))
            .andExpect(jsonPath("$.errors[0].error_values", is(nullValue())))
            .andExpect(jsonPath("$.errors[0].error", is("must not be blank")));
    }

    @Test
    void createFilingWhenReferenceResignedOnBlankThenResponse400() throws Exception {
        final var body = "{" + TM01_FRAGMENT.replace("2022-09-13", "") + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                .contentType("application/json")
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
            .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
            .andExpect(jsonPath("$.errors[0].location", is("$.resigned_on")))
            .andExpect(jsonPath("$.errors[0].error_values", is(nullValue())))
            .andExpect(jsonPath("$.errors[0].error", is("must not be null")));
    }

    @Test
    void createFilingWhenReferenceResignedOnInFutureThenResponse400() throws Exception {
        final var tomorrow = LocalDate.now().plusDays(1);
        final var body = "{"
                + TM01_FRAGMENT.replace("2022-09-13", tomorrow.toString())
                + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].location", is("$.resigned_on")))
                .andExpect(jsonPath("$.errors[0].error_values",
                        is(Map.of("rejected", tomorrow.toString()))))
                .andExpect(jsonPath("$.errors[0].error",
                        is("must be a date in the past or in the present")));
    }

}