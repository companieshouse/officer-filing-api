package uk.gov.companieshouse.officerfiling.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Configuration class for CH logging.
 */
@Configuration
@PropertySource("classpath:logger.properties")
public class LoggingConfig {

    @Value("${logger.namespace}")
    private String loggerNamespace;

    /**
     * Creates a CH logger bean with specified namespace for use throughout this application.
     *
     * @return the {@link LoggerFactory} for the specified namespace
     */
    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(loggerNamespace);
    }
}
