package uk.gov.companieshouse.officerfiling.api.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.officerfiling.api.interceptor.TokenPermissionInterceptor;

@Configuration
@ComponentScan("uk.gov.companieshouse.officerfiling.api.interceptor")
public class InterceptorConfig implements WebMvcConfigurer {

    static final String OFFICERS_ENDPOINT = "/transactions/{transId}/officers";
    static final String OFFICERS_FILING_ENDPOINT = "/transactions/{transId}/officers/{filingResourceId}";

    static final String PRIVATE_OFFICERS_FILING_ENDPOINT = "/private/transactions/{transId}/officers/{filingResourceId}/filings";


    /**
     * Setup the interceptors to run against endpoints when the endpoints are called
     * Interceptors are executed in the order they are added to the registry
     * @param registry The spring interceptor registry
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        TokenPermissionInterceptor tokenPermissionInterceptor = new TokenPermissionInterceptor();
        registry.addInterceptor(tokenPermissionInterceptor).addPathPatterns(OFFICERS_FILING_ENDPOINT, OFFICERS_ENDPOINT, PRIVATE_OFFICERS_FILING_ENDPOINT);
    }
}
