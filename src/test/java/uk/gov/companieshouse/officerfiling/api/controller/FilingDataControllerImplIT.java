package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.interceptor.OpenTransactionInterceptor;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.service.FilingDataService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;

@Tag("web")
@WebMvcTest(controllers = FilingDataControllerImpl.class)
class FilingDataControllerImplIT {
    private static final String TRANS_ID = "4f56fdf78b357bfc";
    private static final String FILING_ID = "632c8e65105b1b4a9f0d1f5e";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String REF_APPOINTMENT_ID = "12345";
    private static final String REF_ETAG = "6789";
    private static final String RESIGNED_ON = "2022-10-05";
    private static final String USER ="user";
    private static final String KEY ="key";
    private static final String KEY_ROLE ="*";
    @MockBean
    private FilingDataService filingDataService;
    @MockBean
    private OfficerFilingService officerFilingService;
    @MockBean
    private TransactionInterceptor transactionInterceptor;
    @MockBean
    private OpenTransactionInterceptor openTransactionInterceptor;
    @MockBean
    private Logger logger;

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
        when(transactionInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(openTransactionInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void getFilingsWhenFound() throws Exception {
        final var filingApi = new FilingApi();
        filingApi.setKind("officer-filing#termination");
        final Map<String, Object> dataMap =
                Map.of("referenceEtag", REF_ETAG, "referenceAppointmentId", REF_APPOINTMENT_ID, "resignedOn", RESIGNED_ON);
        filingApi.setData(dataMap);
        when(filingDataService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(filingApi);
        Transaction transaction = new Transaction();
        transaction.setStatus(TransactionStatus.CLOSED);

        mockMvc.perform(get("/private/transactions/{id}/officers/{filingId}/filings", TRANS_ID, FILING_ID)
            .headers(httpHeaders).requestAttr("transaction", transaction))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].data", is(dataMap)))
            .andExpect(jsonPath("$[0].kind", is("officer-filing#termination")));
    }

    @Test
    void getFilingsWhenNotFound() throws Exception {
        when(filingDataService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER)).thenThrow(new ResourceNotFoundException("for Not Found scenario"));
        Transaction transaction = new Transaction();
        transaction.setStatus(TransactionStatus.CLOSED);

        mockMvc.perform(
                        get("/private/transactions/{id}/officers/{filingId}/filings", TRANS_ID,
                                FILING_ID).headers(httpHeaders)
                                .requestAttr("transaction", transaction))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(status().reason(is("Resource not found")))
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void getFilingsTransactionOpen() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setStatus(TransactionStatus.OPEN);

        mockMvc.perform(get("/private/transactions/{id}/officers/{filingId}/filings", TRANS_ID, FILING_ID)
                        .headers(httpHeaders).requestAttr("transaction", transaction))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getFilingsNonKey() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setStatus(TransactionStatus.CLOSED);
        httpHeaders.set("ERIC-Authorised-Key-Roles", "");

        mockMvc.perform(get("/private/transactions/{id}/officers/{filingId}/filings", TRANS_ID, FILING_ID)
                        .headers(httpHeaders).requestAttr("transaction", transaction))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}