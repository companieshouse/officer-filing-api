package uk.gov.companieshouse.officerfiling.api.interceptor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;
import java.net.URI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.model.entity.Links;

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
    private static final String OTHER_FILING_ID = "wxyz";
    private static final OfficerFilingData offData = new OfficerFilingData(
            "etag",
            FILING_ID,
            Instant.parse("3022-09-13T00:00:00Z"));
    static final Instant now = Instant.parse("2022-09-13T00:00:00Z");
    private static final Links links = new Links(createUri("/transactions/" + TRANS_ID + "/officers/" + FILING_ID), createUri("status"));
    private static final Links other_links = new Links(createUri("/transactions/" + TRANS_ID + "/officers/" + OTHER_FILING_ID), createUri("status"));
    private static final OfficerFiling FILING = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData).links(links)
            .build();
    private static final OfficerFiling OTHER_FILING = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData).links(other_links)
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

    private static URI createUri(String uri) {
        try {
            return new URI(uri);
        } catch (Exception e) {
            return null;
        }
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
      
        var response = validTransactionInterceptor.preHandle(mockRequest, mockResponse, handler);

        assertThat(response, is(true));
    }

    @Test
    void requestURiContainsFilingSelfLinkReturnsFalse() {
        when(mockOfficerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(OTHER_FILING));
        
        var response = validTransactionInterceptor.preHandle(mockRequest, mockResponse, handler);

        assertThat(response, is(false));
        verify(logger, times (1))
                .errorRequest(mockRequest, "Filing resource does not match request");
        verify(mockResponse, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void requestWithNoFilingIdReturnsTrue() {
        ((HashMap<Object, Object>) mockRequest.getAttribute("")).put("filingResourceId", null);
        when(mockRequest.getAttribute(any())).thenReturn(pathVariablesMap);

        var response = validTransactionInterceptor.preHandle(mockRequest, mockResponse, handler);

        assertThat(response, is(true));
    }
}
