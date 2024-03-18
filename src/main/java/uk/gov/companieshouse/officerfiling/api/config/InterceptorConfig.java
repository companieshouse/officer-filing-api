package uk.gov.companieshouse.officerfiling.api.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.ClosedTransactionInterceptor;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.OpenTransactionInterceptor;
import uk.gov.companieshouse.api.interceptor.TokenPermissionsInterceptor;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.api.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.officerfiling.api.interceptor.OfficersCRUDAuthenticationInterceptor;
import uk.gov.companieshouse.officerfiling.api.interceptor.RequestLoggingInterceptor;
import uk.gov.companieshouse.officerfiling.api.interceptor.ValidTransactionInterceptor;

@Configuration
@ComponentScan(basePackages = {"uk.gov.companieshouse.api", "uk.gov.companieshouse.officerfiling.api"})
public class InterceptorConfig implements WebMvcConfigurer {

    private static final String TRANSACTIONS = "/transactions/**";
    private static final String PRIVATE = "/private/**";
    private static final String[] TRANSACTIONS_LIST = {TRANSACTIONS, PRIVATE};

    private static final String GET_VALIDATION = "/**/validation_status";
    private static final String FILINGS = "/transactions/*/officers/*";
    private static final String OFFICERS = "/officer-filing/**";
    
    /**
     * Setup the interceptors to run against endpoints when the endpoints are called
     * Interceptors are executed in the order they are added to the registry
     * @param registry The spring interceptor registry
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        addRequestLoggingInterceptor(registry);
        addTransactionInterceptor(registry);
        addOpenTransactionInterceptor(registry);
        addTokenPermissionInterceptor(registry);
        addInternalUserInterceptor(registry);
        addClosedTransactionInterceptor(registry);
        addValidTransactionInterceptor(registry);
        addUserAuthenticationInterceptor(registry);
    }

    private void addRequestLoggingInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor());
    }

    private void addValidTransactionInterceptor(InterceptorRegistry registry){
        registry.addInterceptor(validTransactionInterceptor())
                .addPathPatterns(new String[] { GET_VALIDATION, FILINGS });
    }

    /**
     * Interceptor to get transaction and put in request for endpoints that require a transaction
     * @param registry The spring interceptor registry
     */
    private void addTransactionInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(transactionInterceptor())
            .addPathPatterns(TRANSACTIONS_LIST);
    }

    private void addOpenTransactionInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(openTransactionInterceptor())
            .addPathPatterns(TRANSACTIONS_LIST);
    }

    private void addTokenPermissionInterceptor(InterceptorRegistry registry) {
        registry.addInterceptor(tokenPermissionsInterceptor());
        //Just check the non private endpoints. Private endpoints use API keys rather than OAuth2
        registry.addInterceptor(officersCRUDAuthenticationInterceptor())
            .addPathPatterns(TRANSACTIONS);
    }

    private void addInternalUserInterceptor(InterceptorRegistry registry){
        registry.addInterceptor(internalUserInterceptor())
                .addPathPatterns(PRIVATE);
    }

    private void addClosedTransactionInterceptor(InterceptorRegistry registry){
        registry.addInterceptor(closedTransactionInterceptor())
                .addPathPatterns(PRIVATE);
    }

    private void addUserAuthenticationInterceptor(InterceptorRegistry registry){
        var externalMethods = new ArrayList<String>();
        externalMethods.add("GET");
        var otherAllowedIdentityTypes = new ArrayList<String>();
        otherAllowedIdentityTypes.add("oauth2");
        InternalUserInterceptor internalUserInterceptor = internalUserInterceptor();
        UserAuthenticationInterceptor userAuthenticationInterceptor = new UserAuthenticationInterceptor(externalMethods, otherAllowedIdentityTypes, internalUserInterceptor);
        registry.addInterceptor(userAuthenticationInterceptor)
                .addPathPatterns(OFFICERS);
    }

    @Bean
    public TransactionInterceptor transactionInterceptor() {
        return new TransactionInterceptor();
    }

    @Bean
    public OpenTransactionInterceptor openTransactionInterceptor() {
        return new OpenTransactionInterceptor();
    }

    public RequestLoggingInterceptor requestLoggingInterceptor() {
        return new RequestLoggingInterceptor();
    }

    @Bean
    public OfficersCRUDAuthenticationInterceptor officersCRUDAuthenticationInterceptor() {
        return new OfficersCRUDAuthenticationInterceptor();
    }

    public TokenPermissionsInterceptor tokenPermissionsInterceptor() {
        return new TokenPermissionsInterceptor();
    }

    public InternalUserInterceptor internalUserInterceptor(){
        return new InternalUserInterceptor();
    }

    public ClosedTransactionInterceptor closedTransactionInterceptor(){
        return new ClosedTransactionInterceptor();
    }

    @Bean
    public ValidTransactionInterceptor validTransactionInterceptor() {
        return new ValidTransactionInterceptor();
    }
}
