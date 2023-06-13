package uk.gov.companieshouse.officerfiling.api.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ValidTransactionInterceptorTest {

    @Mock
    private OfficerFilingService mockOfficerFilingService;
    @Mock
    private Object handler;
    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private HttpSession requestSession;

    private ValidTransactionInterceptor validTransactionInterceptor;

    private final static String TEST_REQUEST_PATH = "/";
    private static final String TRANS_ID = "12345";
    private static final String FILING_ID = "abcde";

    HashMap<String, String> pathVariablesMap = new HashMap<>();

    @BeforeEach
    void setUp() {
        when(mockRequest.getSession()).thenReturn(requestSession);
        when(mockRequest.getRequestURI()).thenReturn(TEST_REQUEST_PATH);
        when(mockRequest.getAttribute(any())).thenReturn(pathVariablesMap);
        validTransactionInterceptor = new ValidTransactionInterceptor();
//        loggerFactory.getLogger()
    }

//    @Test
//    void getOfficerFilingReturnsEmptyOptional() {
//
//        when(mockOfficerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.empty());
//
//        var response = validTransactionInterceptor.preHandle(mockRequest, mockResponse, handler);
//        verify(mockRequest).getSession();
//
//        assertThat(response, is(false));
//        verify(mockResponse, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
//    }

//    @Test
//    void requestURiContainsFilingSelfLinkReturnsTrue() {
//        when(officerFilingService.requestUriContainsFilingSelfLink(any(), any())).thenReturn(true);
//
//    }
//
//    @Test
//    void requestURiContainsFilingSelfLinkReturnsFalse() {
//        when(officerFilingService.requestUriContainsFilingSelfLink(any(), any())).thenReturn(false);
//
//    }
}
