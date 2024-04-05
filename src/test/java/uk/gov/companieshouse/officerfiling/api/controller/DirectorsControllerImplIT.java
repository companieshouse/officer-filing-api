package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;
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
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.transaction.TransactionsResourceHandler;
import uk.gov.companieshouse.api.handler.transaction.request.TransactionsGet;
import uk.gov.companieshouse.api.interceptor.OpenTransactionInterceptor;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.api.model.officers.OfficerRoleApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerService;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

@Tag("web")
@WebMvcTest(controllers = DirectorsControllerImpl.class)
class DirectorsControllerImplIT {
    private static final String TRANS_ID = "4f56fdf78b357bfc";
    private static final String SUBMISSION_ID = "645d1188c794645afe15f5cc";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String USER ="user";
    private static final String KEY ="key";
    private static final String KEY_ROLE ="*";
    Instant resignedOn = Instant.parse("2021-12-03T10:15:30.00Z");
    @MockBean
    private OfficerFilingService officerFilingService;
    @MockBean
    private CompanyAppointmentService companyAppointmentService;
    @MockBean
    private LogHelper logHelper;
    @MockBean
    private Logger logger;
    @MockBean
    private TransactionInterceptor transactionInterceptor;
    @MockBean
    private OpenTransactionInterceptor openTransactionInterceptor;
    @MockBean
    private OfficerService officerService;
    @Mock
    private HttpServletRequest request;
    private Transaction transaction;

    @Mock
    private TransactionService transactionService;
    @Mock
    Optional<OfficerFiling> officerFilingOptional;
    @Mock
    AppointmentFullRecordAPI appointmentFullRecordAPI;
    @Mock
    OfficerFiling officerFiling;
    @Mock
    OfficerServiceException serviceException;
    @MockBean
    private ApiClientService apiClientService;
    @Mock
    private ApiClient apiClientMock;
    @Mock
    private TransactionsResourceHandler transactionResourceHandlerMock;
    @Mock
    private TransactionsGet transactionGetMock;
    @Mock
    private ApiResponse<Transaction> apiResponse;
    private HttpHeaders httpHeaders;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws IOException, URIValidationException {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", PASSTHROUGH_HEADER);
        httpHeaders.add("ERIC-Identity", USER);
        httpHeaders.add("ERIC-Identity-Type", KEY);
        httpHeaders.add("ERIC-Authorised-Key-Roles", KEY_ROLE);
        httpHeaders.add("ERIC-Authorised-Token-Permissions", "company_number="+COMPANY_NUMBER+" company_officers=readprotected,delete,create,update");

        var offData = new OfficerFilingData(
                "etag",
                null,
                resignedOn);

        transaction = new Transaction();
        transaction.setCompanyNumber(COMPANY_NUMBER);
        transaction.setId(TRANS_ID);
        transaction.setStatus(TransactionStatus.OPEN);

        when(transactionInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(openTransactionInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(officerFilingService.get(SUBMISSION_ID, TRANS_ID)).thenReturn(officerFilingOptional);
        when(officerFilingOptional.isPresent()).thenReturn(true);
        when(officerFilingOptional.get()).thenReturn(officerFiling);
        when(officerFiling.getData()).thenReturn(offData);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, null, PASSTHROUGH_HEADER)).thenReturn(appointmentFullRecordAPI);

        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClientMock);
        when(apiClientMock.transactions()).thenReturn(transactionResourceHandlerMock);
        when(transactionResourceHandlerMock.get(anyString())).thenReturn(transactionGetMock);
        when(transactionGetMock.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(transaction);
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
    }

    @Test
    void getListOfActiveDirectorsDetailsWhenFoundThen200() throws Exception {
        var officer = new CompanyOfficerApi();
        officer.setName("DOE, John James");
        officer.setOfficerRole(OfficerRoleApi.CORPORATE_DIRECTOR);

        final var officers = Arrays.asList(officer, officer);

        when(officerService.getListOfActiveDirectorsDetails(any(HttpServletRequest.class), eq(TRANS_ID), eq(COMPANY_NUMBER), eq(PASSTHROUGH_HEADER))).thenReturn(officers);

        mockMvc.perform(get("/transactions/{transactionId}/officers/active-directors-details", TRANS_ID)
                .headers(httpHeaders).requestAttr("transaction", transaction))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getListOfActiveDirectorsDetailsDetailsWhenNotFoundThen500() throws Exception {

        when(officerService.getListOfActiveDirectorsDetails(any(HttpServletRequest.class), eq(TRANS_ID), eq(COMPANY_NUMBER), eq(PASSTHROUGH_HEADER)))
            .thenThrow(serviceException);
        when(serviceException.getCause()).thenReturn(serviceException);
        when(serviceException.getMessage()).thenReturn("Internal error");

        mockMvc.perform(get("/transactions/{transactionId}/officers/active-directors-details", TRANS_ID)
                .headers(httpHeaders).requestAttr("transaction", transaction))
            .andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @Test
    void getRemoveCheckAnswersDirectorDetailsWhenFoundThen200() throws Exception {
        mockMvc.perform(get("/transactions/{transactionId}/officers/{filingId}/tm01-check-answers-directors-details", TRANS_ID, SUBMISSION_ID)
                        .headers(httpHeaders).requestAttr("transaction", transaction))
                .andDo(print())
                .andExpect(status().isOk());
        verify(appointmentFullRecordAPI, times(1)).setResignedOn(
                LocalDate.ofInstant(resignedOn,
                ZoneId.systemDefault()));
    }

    @Test
    void getRemoveCheckAnswersDirectorDetailsWhenNotFoundThen500() throws Exception {

        when(officerFilingOptional.isPresent()).thenReturn(false);
        mockMvc.perform(get("/transactions/{transactionId}/officers/{filingId}/tm01-check-answers-directors-details", TRANS_ID, SUBMISSION_ID)
                        .headers(httpHeaders).requestAttr("transaction", transaction))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getRemoveCheckAnswersDirectorDetailsWhenResignedOnNotFoundThen500() throws Exception {
        var officerDataNoResignationDate = new OfficerFilingData(
                "etag",
                null,
                null);
        when(officerFiling.getData()).thenReturn(officerDataNoResignationDate);

        mockMvc.perform(get("/transactions/{transactionId}/officers/{filingId}/tm01-check-answers-directors-details", TRANS_ID, SUBMISSION_ID)
                        .headers(httpHeaders).requestAttr("transaction", transaction))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}