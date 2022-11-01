package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
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
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

@Tag("web")
@WebMvcTest(controllers = ValidationStatusControllerImpl.class)
class ValidationStatusControllerImplIT {
    private static final String TRANS_ID = "4f56fdf78b357bfc";
    private static final String FILING_ID = "632c8e65105b1b4a9f0d1f5e";
    private static final String PASSTHROUGH_HEADER = "passthrough";

    @MockBean
    private OfficerFilingService officerFilingService;
    @MockBean
    private HttpServletRequest request;
    @MockBean
    private Logger logger;
    @MockBean
    private LogHelper logHelper;

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
        final var filing = OfficerFiling.builder().build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));

        mockMvc.perform(get("/private/transactions/{id}/officers/{filingId}/validation_status", TRANS_ID, FILING_ID, request)
            .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.is_valid", is(true)));
    }

    @Test
    void validationStatusWhenNotFound() throws Exception {
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/private/transactions/{id}/officers/{filingId}/validation_status", TRANS_ID, FILING_ID, request)
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotExist());
    }
}