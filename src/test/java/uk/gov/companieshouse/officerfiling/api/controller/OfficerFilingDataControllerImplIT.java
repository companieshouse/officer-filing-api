package uk.gov.companieshouse.officerfiling.api.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("web")
@WebMvcTest(controllers = OfficerFilingDataController.class)
class OfficerFilingDataControllerImplIT {
    private static final String TRANS_ID = "4f56fdf78b357bfc";
    private static final String FILING_ID = "632c8e65105b1b4a9f0d1f5e";
    private static final String PASSTHROUGH_HEADER = "passthrough";

    @MockBean
    private OfficerFilingService officerFilingService;

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
    void validationStatusWhenFound() throws Exception {
        final var filing = OfficerFiling.builder().name("test").kind("kind").build();
        when(officerFilingService.getFilingsData(FILING_ID)).thenReturn(List.of(filing));

        mockMvc.perform(get("/private/transactions/{id}/officers/{filingId}/filings", TRANS_ID, FILING_ID)
            .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("test")))
            .andExpect(jsonPath("$[0].keys()", containsInAnyOrder("name","kind")));
    }

    @Test
    void validationStatusWhenNotFound() throws Exception {
        when(officerFilingService.getFilingsData(FILING_ID)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/private/transactions/{id}/officers/{filingId}/filing", TRANS_ID, FILING_ID)
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotExist());
    }
}