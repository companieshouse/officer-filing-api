package uk.gov.companieshouse.officerfiling.api.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.api.model.officers.OfficerRoleApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;

@Tag("web")
@WebMvcTest(controllers = StopScreenValidationControllerImpl.class)
class StopScreenValidationControllerImplIT {

    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = "12345678";
    private CompanyProfileApi companyProfileApi;

    @MockBean
    private OfficerFilingService officerFilingService;
    @MockBean
    private CompanyProfileService companyProfileService;

    @Mock
    private HttpServletRequest request;
    @Mock
    private Transaction transaction;
    private HttpHeaders httpHeaders;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", PASSTHROUGH_HEADER);

        companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyStatus("dissolved"); ;
        companyProfileApi.setDateOfCessation(null);
    }

    @Test
    void getCurrentOrFutureDissolvedWhenFoundThen200() throws Exception {
        var officer = new CompanyOfficerApi();
        officer.setName("DOE, John James");
        officer.setOfficerRole(OfficerRoleApi.CORPORATE_DIRECTOR);

        final var officers = Arrays.asList(officer, officer);

        when(companyProfileService.getCompanyProfile(anyString(), eq(COMPANY_NUMBER), eq(PASSTHROUGH_HEADER))).thenReturn(companyProfileApi);

        mockMvc.perform(get("/officer-filing/company/{companyNumber}/eligibility-check/past-future-dissolved", COMPANY_NUMBER)
                        .headers(httpHeaders).requestAttr("companyNumber", COMPANY_NUMBER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void getCurrentOrFutureDissolvedWhenCompanyProfileServiceNotFoundThen500() throws Exception {

        when(companyProfileService.getCompanyProfile(anyString(), eq(COMPANY_NUMBER), eq(PASSTHROUGH_HEADER)))
                .thenThrow(new CompanyProfileServiceException("Error Retrieving company profile"));

        mockMvc.perform(get("/officer-filing/company/{companyNumber}/eligibility-check/past-future-dissolved", COMPANY_NUMBER)
                        .headers(httpHeaders).requestAttr("companyNumber", COMPANY_NUMBER))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}
