package uk.gov.companieshouse.officerfiling.api.service;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.officers.OfficersResourceHandler;
import uk.gov.companieshouse.api.handler.officers.request.OfficersList;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.officers.OfficersApi;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.client.OracleQueryClient;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.model.entity.ActiveOfficerDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfficerServiceImplTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String SUBMISSION_ID = "ABCDEFG";
    @MockBean
    private HttpServletRequest request;
    @MockBean
    private Logger logger;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient apiClient;

    @Mock
    private OracleQueryClient oracleQueryClient;

    @Mock
    private OfficersResourceHandler officersResourceHandler;

    @Mock
    private OfficersList officersList;

    @Mock
    private ApiResponse<OfficersApi> apiResponse;
    private OfficerService testService;

    @BeforeEach
    void setUp() {
        testService = new OfficerServiceImpl(oracleQueryClient, logger);
    }

    @Test
    void getListOfActiveDirectorsDetailsWhenFound() throws OfficerServiceException {
        List<ActiveOfficerDetails> officers = testListOfOfficers();
        List<ActiveOfficerDetails> Directors = officers.subList(0, 2);
        when(oracleQueryClient.getActiveOfficersDetails(COMPANY_NUMBER)).thenReturn(officers);

        var response = testService.getListActiveDirectorsDetails(request, COMPANY_NUMBER);
        assertThat(response).hasSize(2);
        assertThat(response).isEqualTo(Directors);
    }

    @Test
    void exceptionIsThrownWhenOfficersDetailsNotFound() throws OfficerServiceException {
        when(oracleQueryClient.getActiveOfficersDetails(COMPANY_NUMBER)).thenThrow(OfficerServiceException.class);

        final var exception = assertThrows(OfficerServiceException.class,
            () -> testService.getListActiveDirectorsDetails(request, COMPANY_NUMBER));
    }

    private List<ActiveOfficerDetails> testListOfOfficers() {
        ActiveOfficerDetails officer1 = new ActiveOfficerDetails();
        ActiveOfficerDetails officer2 = new ActiveOfficerDetails();
        ActiveOfficerDetails officer3 = new ActiveOfficerDetails();
        officer1.setRole("Director");
        officer2.setRole("Director");
        officer3.setRole("Secretary");
        List<ActiveOfficerDetails> officers = Arrays.asList(officer1, officer2, officer3);
        return officers;
    }
}
