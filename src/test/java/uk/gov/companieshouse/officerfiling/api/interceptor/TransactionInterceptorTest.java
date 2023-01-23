package uk.gov.companieshouse.officerfiling.api.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.TransactionServiceException;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentServiceImpl;
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
    @InjectMocks
    private TransactionInterceptor transactionInterceptor;
    private Transaction transaction;
    private MockHttpServletResponse mockHttpServletResponse;
    private Object mockHandler;
    private HashMap<String, String> pathParams;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setId(TRANS_ID);
        pathParams = new HashMap<>();
        pathParams.put("transId", TRANS_ID);

        mockHttpServletResponse = new MockHttpServletResponse();
        mockHandler = new Object();
    }

    @Test
    void testPreHandleIsSuccessful() throws Exception {
        transaction.setStatus(TransactionStatus.OPEN);
        when(transactionService.getTransaction(eq(TRANS_ID), eq(PASSTHROUGH_HEADER))).thenReturn(transaction);
        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParams);
        when(mockHttpServletRequest.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);

        assertTrue(transactionInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        verify(mockHttpServletRequest, times(1)).setAttribute("transaction", transaction);
    }

    //Write test for when transaction is closed

    @Test
    void testPreHandleIsUnsuccessfulWhenServiceExceptionCaught() throws Exception {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        var pathParams = new HashMap<String, String>();
        pathParams.put("transId", TRANS_ID);

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParams);
        when(mockHttpServletRequest.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenThrow(
            TransactionServiceException.class);

        assertFalse(transactionInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,  mockHttpServletResponse.getStatus());
    }
}
