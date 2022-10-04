package uk.gov.companieshouse.officerfiling.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.officerfiling.api.controller.OfficerFilingController;

@Tag("app")
@SpringBootTest
class OfficerFilingApiApplicationTests {
    @Autowired
    private OfficerFilingController officerFilingController;

    @Test
    void contextLoads() {
        assertThat(officerFilingController, is(not(nullValue())));
    }

}
