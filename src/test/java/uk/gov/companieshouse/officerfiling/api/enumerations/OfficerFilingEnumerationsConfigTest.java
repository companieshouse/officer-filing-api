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
import uk.gov.companieshouse.officerfiling.api.config.enumerations.OfficerFilingEnumerationsConfig;

@Tag("web")
@WebMvcTest
@ContextConfiguration(classes = OfficerFilingEnumerationsConfig.class)
class OfficerFilingEnumerationsConfigTest {
    @Autowired
    @Qualifier(value = "validation")
    private Map<String, String> validation;

    @Test
    void officerFiling() {
        assertThat(validation.get("etag-invalid"),
                is("The Director’s information was updated before you sent this submission. You will need to start again"));
    }
}
