package uk.gov.companieshouse.officerfiling.api.interceptor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.util.LogContextProperties;
import org.springframework.web.servlet.ModelAndView;

@ExtendWith(MockitoExtension.class)
class RequestLoggingInterceptorTest {
  private final static String TEST_REQUEST_PATH = "/";

  @Mock
  Object handler;
  @Mock
  ModelAndView modelAndView;
  @Mock
  HttpServletRequest mockRequest;
  @Mock
  HttpServletResponse mockResponse;
  @Mock
  HttpSession requestSession;
  private RequestLoggingInterceptor requestLoggingInterceptor;

  @BeforeEach
  void setUp() {
    when(mockRequest.getSession()).thenReturn(requestSession);
    when(mockRequest.getRequestURI()).thenReturn(TEST_REQUEST_PATH);
    requestLoggingInterceptor = new RequestLoggingInterceptor();
  }

  @Test
  void verifyRequestLoggingPreHandle() {
    var response = requestLoggingInterceptor.preHandle(mockRequest, mockResponse, handler);
    verify(mockRequest, times(1)).getSession();
    verify(requestSession, times(1)).setAttribute(anyString(), anyLong());
    assertThat(response, is(true));
  }

  @Test
  void verifyRequestLoggingPostHandle() {
    long startTime = System.currentTimeMillis();
    when(mockRequest.getSession().getAttribute(LogContextProperties.START_TIME_KEY.value()))
        .thenReturn(startTime);
    requestLoggingInterceptor.postHandle(mockRequest, mockResponse, handler, modelAndView);
    verify(mockResponse, times(1)).getStatus();
  }

}
