package uk.gov.companieshouse.officerfiling.api.enumerations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.companieshouse.officerfiling.api.config.enumerations.ConstantsEnumerationsConfig;

@Tag("web")
@WebMvcTest
@ContextConfiguration(classes = ConstantsEnumerationsConfig.class)
class ConstantsConfigTest {

    @Autowired
    @Qualifier(value = "companyType")
    private Map<String, String> companyType;

    @Test
    void companyType() {
        assertThat(companyType.get("ltd"),
                is("Private limited company"));
    }
}