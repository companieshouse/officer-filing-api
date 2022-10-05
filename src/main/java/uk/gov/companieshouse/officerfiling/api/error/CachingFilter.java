package uk.gov.companieshouse.officerfiling.api.error;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * Cache the request for future use; {@link RestExceptionHandler} can include the request body
 * content in any error response.
 */
@Component
public class CachingFilter implements Filter {
    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {
        final var contentCachingRequestWrapper =
                new ContentCachingRequestWrapper((HttpServletRequest) servletRequest);
        filterChain.doFilter(contentCachingRequestWrapper, servletResponse);
    }
}
