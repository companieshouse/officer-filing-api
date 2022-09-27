package uk.gov.companieshouse.officerfiling.api.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;

@Tag("web")
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
        final var body = "{" + TM01_FRAGMENT + "}";
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceOfficerId("id")
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").doesNotExist());
        verify(filingMapper).map(dto);
    }

}