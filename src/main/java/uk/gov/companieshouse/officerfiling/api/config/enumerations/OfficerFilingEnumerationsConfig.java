package uk.gov.companieshouse.officerfiling.api.config.enumerations;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.companieshouse.officerfiling.api.enumerations.YamlPropertySourceFactory;

@Configuration
@PropertySource(value = "classpath:api-enumerations/officer_filing.yml", factory = YamlPropertySourceFactory.class)
public class OfficerFilingEnumerationsConfig {

    @Bean("validation")
    @ConfigurationProperties(prefix = "validation")
    public Map<String, String> validation() {
        return new HashMap<>();
    }
}
