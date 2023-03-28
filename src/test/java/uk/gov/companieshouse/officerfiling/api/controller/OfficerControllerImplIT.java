package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import uk.gov.companieshouse.api.interceptor.OpenTransactionInterceptor;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.client.OracleQueryClient;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.model.entity.ActiveOfficerDetails;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

@Tag("web")
@WebMvcTest(controllers = OfficerControllerImpl.class)
class OfficerControllerImplIT {
    private static final String TRANS_ID = "4f56fdf78b357bfc";
    private static final String FILING_ID = "632c8e65105b1b4a9f0d1f5e";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String USER ="user";
    private static final String KEY ="key";
    private static final String KEY_ROLE ="*";
    @MockBean
    private OfficerFilingService officerFilingService;
    @MockBean
    private LogHelper logHelper;
    @MockBean
    private Logger logger;
    @MockBean
    private TransactionInterceptor transactionInterceptor;
    @MockBean
    private OpenTransactionInterceptor openTransactionInterceptor;
    @Mock
    private OfficerService officerService;
    @MockBean
    private OracleQueryClient oracleQueryClient;
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
        httpHeaders.add("ERIC-Identity", USER);
        httpHeaders.add("ERIC-Identity-Type", KEY);
        httpHeaders.add("ERIC-Authorised-Key-Roles", KEY_ROLE);
        httpHeaders.add("ERIC-Authorised-Token-Permissions", "company_officers=readprotected,delete");
        when(transactionInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(openTransactionInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(transaction.getStatus()).thenReturn(TransactionStatus.OPEN);
    }

    @Test
    void getListOfActiveDirectorsDetailsDetailsWhenFoundTthen200() throws Exception {
        var officer = new ActiveOfficerDetails();
        officer.setForeName1("John");
        officer.setSurname("DOE");
        officer.setRole("Director");

        final var officers = Arrays.asList(officer, officer);

        when(oracleQueryClient.getActiveOfficersDetails(COMPANY_NUMBER)).thenReturn(officers);

        mockMvc.perform(get("/transactions/{transactionId}/officers/{filingResourceId}/active-officers-details", TRANS_ID, FILING_ID)
            .headers(httpHeaders).requestAttr("transaction", transaction))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getListOfActiveDirectorsDetailsDetailsWhenNotFoundThen500() throws Exception {

        when(oracleQueryClient.getActiveOfficersDetails(COMPANY_NUMBER)).thenThrow(new OfficerServiceException("Error retrieving Officers"));

        mockMvc.perform(get("/transactions/{transactionId}/officers/{filingResourceId}/active-officers-details", TRANS_ID, FILING_ID)
                .headers(httpHeaders).requestAttr("transaction", transaction))
            .andDo(print())
            .andExpect(status().isInternalServerError());
    }
}