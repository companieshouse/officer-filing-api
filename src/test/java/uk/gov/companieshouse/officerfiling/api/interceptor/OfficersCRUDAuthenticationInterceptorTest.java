package uk.gov.companieshouse.officerfiling.api.interceptor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.HandlerMapping;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.api.util.security.TokenPermissions;
import uk.gov.companieshouse.api.util.security.Permission.Value;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import static uk.gov.companieshouse.officerfiling.api.utils.Constants.ERIC_REQUEST_ID_KEY;

@ExtendWith(MockitoExtension.class)
class OfficersCRUDAuthenticationInterceptorTest {

    @Mock
    private TransactionService mockTransactionService;
    @Mock
    private Logger logger;
    @Mock
    private Object handler;
    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;

    private OfficersCRUDAuthenticationInterceptor officersCRUDAuthenticationInterceptor;

    private static final String TRANS_ID = "12345";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String REQUEST_ID = "requestId";

    static final Instant now = Instant.parse("2022-09-13T00:00:00Z");

    private static final String PERMISSIONS = "company_number=" + COMPANY_NUMBER; 

    HashMap<String, String> pathVariablesMap;

    private static Transaction mockTransaction = new Transaction();

    private static TokenPermissions mockAllTokenPermissions = new TokenPermissions() {
        public boolean hasPermission(Permission.Key var1, String var2) {
            return true;
        }
    };

    private TokenPermissions withoutPermission(String missingPermission) {
        return new TokenPermissions() {
            public boolean hasPermission(Permission.Key var1, String var2) {
                if (missingPermission.equals(var2)) {
                    return false;
                }
                return true;
            }
        };
    }

    @BeforeEach
    void setUp() {
        officersCRUDAuthenticationInterceptor = new OfficersCRUDAuthenticationInterceptor(logger, mockTransactionService);
        when(mockRequest.getHeader(ERIC_REQUEST_ID_KEY)).thenReturn(REQUEST_ID);
        mockTransaction.setCompanyNumber(COMPANY_NUMBER);
    }

    @Test
    void nullTransactionIdReturnsFalse () {
        pathVariablesMap = new HashMap<>();
        when(mockRequest.getAttribute(any())).thenReturn(pathVariablesMap);

        var response = officersCRUDAuthenticationInterceptor.preHandle(mockRequest, mockResponse, handler);

        assertThat(response, is(false));
        verify(logger, times (1)).errorContext(eq(REQUEST_ID), eq("OfficersCRUDAuthenticationInterceptor unauthorised - no transaction identifier found"), isNull(), any());
        verify(mockResponse, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void noCompanyNumberInTransactionReturnsFalse () {
        pathVariablesMap = new HashMap<>();
        pathVariablesMap.put("transactionId", TRANS_ID);
        when(mockRequest.getAttribute(any())).thenReturn(pathVariablesMap);
        when(mockRequest.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(null);

        var response = officersCRUDAuthenticationInterceptor.preHandle(mockRequest, mockResponse, handler);

        assertThat(response, is(false));
        verify(logger, times (1)).errorContext(eq(REQUEST_ID), eq("OfficersCRUDAuthenticationInterceptor unauthorised - no company number in transaction"), isNull(), any());
        verify(mockResponse, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void noCompanyNumberInScopeReturnsFalse () {
        pathVariablesMap = new HashMap<>();
        pathVariablesMap.put("transactionId", TRANS_ID);
        when(mockRequest.getAttribute(any())).thenReturn(pathVariablesMap);
        when(mockRequest.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(null);
        when(mockTransactionService.getTransaction(eq(TRANS_ID), any())).thenReturn(mockTransaction);

        var response = officersCRUDAuthenticationInterceptor.preHandle(mockRequest, mockResponse, handler);

        assertThat(response, is(false));
        verify(logger, times (1)).errorContext(eq(REQUEST_ID), eq("OfficersCRUDAuthenticationInterceptor unauthorised - no company number in scope"), isNull(), any());
        verify(mockResponse, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void differentCompanyNumberInScopeReturnsFalse () {
        pathVariablesMap = new HashMap<>();
        pathVariablesMap.put("transactionId", TRANS_ID);
        when(mockRequest.getAttribute(any())).thenReturn(pathVariablesMap);
        when(mockRequest.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(null);
        when(mockRequest.getHeader(OfficersCRUDAuthenticationInterceptor.ERIC_AUTHORISED_TOKEN_PERMISSIONS)).thenReturn(PERMISSIONS);
        when(mockTransactionService.getTransaction(eq(TRANS_ID), any())).thenReturn(mockTransaction);
        mockTransaction.setCompanyNumber("87654321");

        var response = officersCRUDAuthenticationInterceptor.preHandle(mockRequest, mockResponse, handler);

        assertThat(response, is(false));
        verify(logger, times (1)).errorContext(eq(REQUEST_ID), eq("OfficersCRUDAuthenticationInterceptor unauthorised - company number in transaction does not match company number in scope"), isNull(), any());
    }

    @Test
    void companyNumberInScopeMatchNoTokenPermissions () {
        pathVariablesMap = new HashMap<>();
        pathVariablesMap.put("transactionId", TRANS_ID);
        when(mockRequest.getAttribute(any())).thenReturn(pathVariablesMap);
        when(mockRequest.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(null);
        when(mockRequest.getHeader(OfficersCRUDAuthenticationInterceptor.ERIC_AUTHORISED_TOKEN_PERMISSIONS)).thenReturn(PERMISSIONS);
        when(mockTransactionService.getTransaction(eq(TRANS_ID), any())).thenReturn(mockTransaction);

        assertThrows(IllegalStateException.class, () -> officersCRUDAuthenticationInterceptor.preHandle(mockRequest, mockResponse, handler));
    }

    @Test
    void companyNumberInScopeMatchAndNoDeleteTokenPermissions () {
       testNoPermission(Value.DELETE);
    }

    @Test
    void companyNumberInScopeMatchAndNoCreateTokenPermissions () {
       testNoPermission(Value.CREATE);
    }

    @Test
    void companyNumberInScopeMatchAndNoUpdateTokenPermissions () {
       testNoPermission(Value.UPDATE);
    }

    @Test
    void companyNumberInScopeMatchAndNoReadProtectedTokenPermissions () {
       testNoPermission(Value.READ_PROTECTED);
    }

    private void testNoPermission(String missingPermission) {
        pathVariablesMap = new HashMap<>();
        pathVariablesMap.put("transactionId", TRANS_ID);
        when(mockRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathVariablesMap);
        when(mockRequest.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(null);
        when(mockRequest.getHeader(OfficersCRUDAuthenticationInterceptor.ERIC_AUTHORISED_TOKEN_PERMISSIONS)).thenReturn(PERMISSIONS);
        when(mockTransactionService.getTransaction(eq(TRANS_ID), any())).thenReturn(mockTransaction);
        when(mockRequest.getAttribute("token_permissions")).thenReturn(withoutPermission(missingPermission));

        var response = officersCRUDAuthenticationInterceptor.preHandle(mockRequest, mockResponse, handler);

        assertThat(response, is(false));
        verify(logger, times (1)).errorContext(eq(REQUEST_ID), eq("OfficersCRUDAuthenticationInterceptor unauthorised"), isNull(), any());
    }

    @Test
    void companyNumberInScopeMatchAndAllTokenPermissions () {
        pathVariablesMap = new HashMap<>();
        pathVariablesMap.put("transactionId", TRANS_ID);
        when(mockRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathVariablesMap);
        when(mockRequest.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(null);
        when(mockRequest.getHeader(OfficersCRUDAuthenticationInterceptor.ERIC_AUTHORISED_TOKEN_PERMISSIONS)).thenReturn(PERMISSIONS);
        when(mockTransactionService.getTransaction(eq(TRANS_ID), any())).thenReturn(mockTransaction);
        when(mockRequest.getAttribute("token_permissions")).thenReturn(mockAllTokenPermissions);

        var response = officersCRUDAuthenticationInterceptor.preHandle(mockRequest, mockResponse, handler);

        assertThat(response, is(true));
    }

    @Test
    void invalidERICTokenPermissions() {
        var officerCRUDAuthenticationInterceptor = new OfficersCRUDAuthenticationInterceptor();
        when(mockRequest.getHeader(OfficersCRUDAuthenticationInterceptor.ERIC_AUTHORISED_TOKEN_PERMISSIONS)).thenReturn("HSU&@AZ123xj*");
        mockRequest.getHeader(ERIC_REQUEST_ID_KEY);
        assertNull(officerCRUDAuthenticationInterceptor.getCompanyNumberInScope(mockRequest));
    }
}
