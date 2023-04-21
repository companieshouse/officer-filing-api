package uk.gov.companieshouse.officerfiling.api.config.enumerations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.companieshouse.officerfiling.api.enumerations.YamlPropertySourceFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource(value = "classpath:api-enumerations/constants.yml", factory = YamlPropertySourceFactory.class)
public class ConstantsEnumerationsConfig {

    @Bean("companyType")
    @ConfigurationProperties(prefix = "company-type")
    public Map<String, String> companyType() {
        return new HashMap<>();
    }
}