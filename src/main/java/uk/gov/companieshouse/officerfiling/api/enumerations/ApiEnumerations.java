package uk.gov.companieshouse.officerfiling.api.enumerations;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Used as an interface with the api-enumerations submodule. A map is populated for each yml file within api-enumerations.
 */
@Component
public class ApiEnumerations {

    private final Map<String, String> validation;
    private final Map<String, String> companyType;

    @Autowired
    public ApiEnumerations(@Qualifier("validation") Map<String, String> validation, @Qualifier("companyType") Map<String, String> companyType) {
        this.validation = validation;
        this.companyType = companyType;
    }

    public String getValidation(ValidationEnum validationEnum, String... customElements) {
        final String message = validation.get(validationEnum.getKey());
        if (customElements.length > 0) {
            return overwriteCustomElements(message, customElements);
        }
        return message;
    }

    public String getCompanyType(String companyTypeKey) {
        return companyType.get(companyTypeKey);
    }

    private String overwriteCustomElements(String message, String... customElements) {
        for (String customElement : customElements) {
            message = message.replaceFirst("<[\\w-]*>", customElement);
        }
        return message;
    }
}
