package uk.gov.companieshouse.officerfiling.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OfficerFilingApiApplicationTests {

    @Test
    void contextLoads() {
        assertThat("TODO", is(not(emptyString())));
    }

}
