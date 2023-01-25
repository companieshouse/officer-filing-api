package uk.gov.companieshouse.officerfiling.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.OpenTransactionInterceptor;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;

@Configuration
@ComponentScan("uk.gov.companieshouse.api.interceptor")
public class InterceptorConfig implements WebMvcConfigurer {

    private static final String TRANSACTIONS = "/transactions/**";

    @Autowired
    private TransactionInterceptor transactionInterceptor;
    //@Autowired
    //private OpenTransactionInterceptor openTransactionInterceptor;

    /**
     * Setup the interceptors to run against endpoints when the endpoints are called
     * Interceptors are executed in the order they are added to the registry
     * @param registry The spring interceptor registry
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        addTransactionInterceptor(registry);
        //addOpenTransactionInterceptor(registry);
    }

    /**
     * Interceptor to get transaction and put in request for endpoints that require a transaction
     * @param registry The spring interceptor registry
     */
    private void addTransactionInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(transactionInterceptor)
                .addPathPatterns(TRANSACTIONS);
    }

//    private void addOpenTransactionInterceptor(InterceptorRegistry registry) {
//        registry.addInterceptor(openTransactionInterceptor)
//            .addPathPatterns(TRANSACTIONS);
//    }
}
