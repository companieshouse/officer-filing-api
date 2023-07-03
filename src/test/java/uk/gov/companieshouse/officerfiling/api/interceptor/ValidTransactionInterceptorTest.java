package uk.gov.companieshouse.officerfiling.api.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidTransactionInterceptorTest {

    @Mock
    private OfficerFilingService mockOfficerFilingService;
    @Mock
    private Logger logger;
    @Mock
    private Object handler;
    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;

    private ValidTransactionInterceptor validTransactionInterceptor;

    private static final String TRANS_ID = "12345";
    private static final String FILING_ID = "abcde";
    private static final OfficerFilingData offData = new OfficerFilingData(
            "etag",
            FILING_ID,
            Instant.parse("3022-09-13T00:00:00Z"));
    static final Instant now = Instant.parse("2022-09-13T00:00:00Z");
    private static final OfficerFiling FILING = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
            .build();


    HashMap<String, String> pathVariablesMap;

    @BeforeEach
    void setUp() {
        validTransactionInterceptor = new ValidTransactionInterceptor(logger, mockOfficerFilingService);
        pathVariablesMap = new HashMap<>();
        pathVariablesMap.put("transactionId", "12345");
        pathVariablesMap.put("filingResourceId", "abcde");
        when(mockRequest.getAttribute(any())).thenReturn(pathVariablesMap);
    }

    @Test
    void getOfficerFilingReturnsEmptyOptional() {
        when(mockOfficerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.empty());

        var response = validTransactionInterceptor.preHandle(mockRequest, mockResponse, handler);

        assertThat(response, is(false));
        verify(logger, times (1)).errorRequest(mockRequest, "Filing resource not found");
        verify(mockResponse, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void requestURiContainsFilingSelfLinkReturnsTrue() {
        when(mockOfficerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(FILING));
        when(mockOfficerFilingService.requestUriContainsFilingSelfLink(any(), any())).thenReturn(true);

        var response = validTransactionInterceptor.preHandle(mockRequest, mockResponse, handler);

        assertThat(response, is(true));
    }

    @Test
    void requestURiContainsFilingSelfLinkReturnsFalse() {
        when(mockOfficerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(FILING));
        when(mockOfficerFilingService.requestUriContainsFilingSelfLink(any(), any())).thenReturn(false);

        var response = validTransactionInterceptor.preHandle(mockRequest, mockResponse, handler);

        assertThat(response, is(false));
        verify(logger, times (1))
                .errorRequest(mockRequest, "Filing resource does not match request");
        verify(mockResponse, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}
