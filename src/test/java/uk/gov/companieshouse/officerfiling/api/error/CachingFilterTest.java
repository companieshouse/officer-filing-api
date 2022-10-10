package uk.gov.companieshouse.officerfiling.api.error;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

@ExtendWith(MockitoExtension.class)
class CachingFilterTest {
    private CachingFilter testFilter;

    @Mock
    private ServletResponse servletResponse;
    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        testFilter = new CachingFilter();
    }

    @Test
    @DisplayName("doFilter(): verify chained call uses correct type")
        // without mockito-inline: only able to verify ContentCachingRequestWrapper type is used
        // in chained call
    void doFilterNonInline() throws ServletException, IOException {
        final var servletRequest = new MockHttpServletRequest();

        testFilter.doFilter(servletRequest, servletResponse, filterChain);

        verify(filterChain).doFilter(isA(ContentCachingRequestWrapper.class), eq(servletResponse));
    }

    @Test
    @DisplayName("doFilter(): verify chained call uses correct instance")
        // with mockito-inline: able to verify constructed ContentCachingRequestWrapper instance
        // is used in chained call
    void doFilter() throws ServletException, IOException {
        final var servletRequest = new MockHttpServletRequest();

        try (final var requestWrapper = mockConstruction(ContentCachingRequestWrapper.class)) {
            testFilter.doFilter(servletRequest, servletResponse, filterChain);

            verify(filterChain).doFilter(requestWrapper.constructed().get(0), servletResponse);
        }

    }
}