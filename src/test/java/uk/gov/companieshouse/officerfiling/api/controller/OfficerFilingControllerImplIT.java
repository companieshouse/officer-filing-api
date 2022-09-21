package uk.gov.companieshouse.officerfiling.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = OfficerFilingControllerImpl.class)
class OfficerFilingControllerImplIT {
    private static final String TRANS_ID = "4f56fdf78b357bfc";
    private static final String TM01_FRAGMENT = "\"reference_etag\": \"etag\","
            + "\"reference_officer_id\": \"id\","
            + "\"resigned_on\": \"2022-09-13\"";

    @MockBean
    private OfficerFilingService officerFilingService;
    @MockBean
    private OfficerFilingMapper filingMapper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
    }

    @Test
    void createFilingWhenTM01PayloadOKThenResponse201() throws Exception {
        String request = "{" + TM01_FRAGMENT + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(request)
                        .contentType("application/json"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").doesNotExist());
    }

}