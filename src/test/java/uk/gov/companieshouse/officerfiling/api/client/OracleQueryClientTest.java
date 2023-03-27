package uk.gov.companieshouse.officerfiling.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.model.entity.ActiveOfficerDetails;

@ExtendWith(MockitoExtension.class)
class OracleQueryClientTest {

    private static final String ACTIVE_OFFICERS_PATH = "/officers/active";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String CLIENT_URL = "http://oracle-query-api";

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Logger logger;
    private OracleQueryClient testClient;



    @BeforeEach
    void setup() {
        testClient = new OracleQueryClient(restTemplate, logger);
        ReflectionTestUtils.setField(testClient, "oracleQueryApiUrl", CLIENT_URL);
    }


    @Test
    void listOfActiveOfficersDetailsReturnedFromOracleWhenFound() throws OfficerServiceException {
        var officer1 = new ActiveOfficerDetails();
        var officer2 = new ActiveOfficerDetails();
        ActiveOfficerDetails[] officerArray = {officer1, officer2};

        when(restTemplate.getForEntity(CLIENT_URL + "/company/" + COMPANY_NUMBER + ACTIVE_OFFICERS_PATH, ActiveOfficerDetails[].class))
                .thenReturn(new ResponseEntity<>(officerArray, HttpStatus.OK));

        var result = testClient.getActiveOfficersDetails(COMPANY_NUMBER);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void exceptionIsThrownWhenListOfActiveDirectorsIsNotFound() {
        when(restTemplate.getForEntity(CLIENT_URL + "/company/" + COMPANY_NUMBER + ACTIVE_OFFICERS_PATH, ActiveOfficerDetails[].class))
                .thenReturn(new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE));

        var serviceException = assertThrows(OfficerServiceException.class, () -> testClient.getActiveOfficersDetails(COMPANY_NUMBER));
        assertTrue(serviceException.getMessage().contains(COMPANY_NUMBER));
        assertTrue(serviceException.getMessage().contains(HttpStatus.SERVICE_UNAVAILABLE.toString()));
    }
}
