package uk.gov.companieshouse.officerfiling.api;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
    @MockitoBean
    private TransactionInterceptor transactionInterceptor;
    @MockitoBean
    private OpenTransactionInterceptor openTransactionInterceptor;
    @MockitoBean
    private OfficersCRUDAuthenticationInterceptor officersCRUDAuthenticationInterceptor;

    @Test
    void contextLoads() {
        assertThat(officerFilingController, is(not(nullValue())));
    }

}
