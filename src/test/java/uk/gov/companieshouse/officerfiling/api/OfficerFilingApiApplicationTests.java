package uk.gov.companieshouse.officerfiling.api;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.api.interceptor.OpenTransactionInterceptor;
import uk.gov.companieshouse.api.interceptor.TransactionInterceptor;
import uk.gov.companieshouse.officerfiling.api.controller.OfficerFilingController;
import uk.gov.companieshouse.officerfiling.api.interceptor.OfficersCRUDAuthenticationInterceptor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@Tag("app")
@SpringBootTest
@TestPropertySource(properties = {
        "NATIONALITY_LIST=testValue",
})
class OfficerFilingApiApplicationTests {
    @Autowired
    private OfficerFilingController officerFilingController;
    @MockBean
    private TransactionInterceptor TransactionInterceptor;
    @MockBean
    private OpenTransactionInterceptor openTransactionInterceptor;
    @MockBean
    private OfficersCRUDAuthenticationInterceptor officersCRUDAuthenticationInterceptor;

    @Test
    void contextLoads() {
        assertThat(officerFilingController, is(not(nullValue())));
    }

}
