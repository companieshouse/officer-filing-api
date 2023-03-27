package uk.gov.companieshouse.officerfiling.api.config;

import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Main application configuration class.
 */
@Configuration
public class AppConfig {
    public AppConfig() {
        // required no-arg constructor
    }

    /**
     * Obtains a clock that returns the current instant, converting to date and time using the
     * UTC time-zone. Singleton bean provides consistent UTC timestamps.
     *
     * @return a Clock that uses the best available system clock in the UTC zone, not null
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
    @Bean
    public Supplier<LocalDate> dateNow() {
        return LocalDate::now;
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
