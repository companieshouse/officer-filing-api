package uk.gov.companieshouse.officerfiling.api.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.transaction.TransactionsResourceHandler;
import uk.gov.companieshouse.api.handler.transaction.request.TransactionsGet;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.TransactionServiceException;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;

@ExtendWith(MockitoExtension.class)
class TransactionInterceptorTest {

    private static final String TRANS_ID = "12345-54321-76666";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    @Mock
    private Logger logger;
    @Mock
    private TransactionService transactionService;
    @Mock
    private HttpServletRequest mockHttpServletRequest;
    @Mock
    private ApiClientService apiClientServiceMock;
    @Mock
    private ApiClient apiClientMock;
    @Mock
    private TransactionsResourceHandler transactionResourceHandlerMock;
    @Mock
    private TransactionsGet transactionGetMock;
    @Mock
    private ApiResponse<Transaction> apiResponse;
    @InjectMocks
    private TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
    private Transaction transaction;
    private MockHttpServletResponse mockHttpServletResponse;
    private Object mockHandler;
    private HashMap<String, String> pathParams;

    @BeforeEach
    void setUp() throws IOException, URIValidationException {
        transaction = new Transaction();
        transaction.setId(TRANS_ID);
        pathParams = new HashMap<>();
        pathParams.put("transId", TRANS_ID);

        mockHttpServletResponse = new MockHttpServletResponse();
        mockHandler = new Object();

        when(apiClientServiceMock.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClientMock);
        when(apiClientMock.transactions()).thenReturn(transactionResourceHandlerMock);
        when(transactionResourceHandlerMock.get(anyString())).thenReturn(transactionGetMock);
        when(transactionGetMock.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(transaction);
    }

    @Test
    void testPreHandleIsSuccessful() throws IOException, URIValidationException {
        transaction.setStatus(TransactionStatus.OPEN);

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParams);
        when(mockHttpServletRequest.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);

        assertTrue(transactionInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        verify(mockHttpServletRequest).setAttribute("transaction", transaction);
    }

    @Test
    void testPreHandleIsUnsuccessfulWhenTransactionClosed() {
        transaction.setStatus(TransactionStatus.CLOSED);

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParams);
        when(mockHttpServletRequest.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);

        assertFalse(transactionInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST,  mockHttpServletResponse.getStatus());
        assertEquals("This transaction is not open", mockHttpServletResponse.getErrorMessage());
    }

    @Test
    void testPreHandleIsUnsuccessfulWhenServiceExceptionCaught() {
        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParams);
        when(mockHttpServletRequest.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenThrow(
            TransactionServiceException.class);

        assertFalse(transactionInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,  mockHttpServletResponse.getStatus());
    }
}
