package uk.gov.companieshouse.officerfiling.api.service;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.officers.OfficersResourceHandler;
import uk.gov.companieshouse.api.handler.officers.request.OfficersList;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.api.model.officers.OfficerRoleApi;
import uk.gov.companieshouse.api.model.officers.OfficersApi;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;

@ExtendWith(MockitoExtension.class)
class OfficerServiceImplTest {

    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String TRANSACTION_ID = "987654321";
    private static final String URI = "/company/" + COMPANY_NUMBER + "/officers";

    @Mock
    private ApiClientService apiClientService;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private OfficersList officersList;
    @Mock
    private OfficersResourceHandler officersResourceHandler;
    @Mock
    private ApiResponse<OfficersApi> apiResponse;
    @Mock
    private Logger logger;
    @Mock
    private OfficersApi mockOfficersApi;
    @Mock
    private HttpServletRequest request;
    private OfficerServiceImpl testService;

    @BeforeEach
    void setUp() {
        testService = new OfficerServiceImpl(apiClientService, logger);
    }

    @Test
    void directorsIsReturnedFromOfficerServiceWhenOfficersRetrieved() throws IOException, URIValidationException {
        CompanyOfficerApi officer1 = new CompanyOfficerApi();
        CompanyOfficerApi officer2 = new CompanyOfficerApi();
        officer1.setOfficerRole(OfficerRoleApi.DIRECTOR);
        officer2.setOfficerRole(OfficerRoleApi.NOMINEE_SECRETARY);
        List officersDetails = new ArrayList<CompanyOfficerApi>();
        officersDetails.add(officer1);
        officersDetails.add(officer2);

        when(mockOfficersApi.getItems()).thenReturn(officersDetails);

        when(apiResponse.getData()).thenReturn(mockOfficersApi);
        when(officersList.execute()).thenReturn(apiResponse);
        when(officersResourceHandler.list(URI)).thenReturn(officersList);
        when(internalApiClient.officers()).thenReturn(officersResourceHandler);
        when(apiClientService.getInternalApiClient(PASSTHROUGH_HEADER)).thenReturn(internalApiClient);

        List<CompanyOfficerApi> officers = testService.getListOfActiveDirectorsDetails(request, TRANSACTION_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER);

        assertEquals(officers, officersDetails.subList(0,1));
    }

    @Test
    void exceptionIsThrownWhenOfficersNotFound() throws IOException, URIValidationException {
        when(officersList.execute()).thenThrow(URIValidationException.class);
        when(officersResourceHandler.list(URI)).thenReturn(officersList);
        when(internalApiClient.officers()).thenReturn(officersResourceHandler);
        when(apiClientService.getInternalApiClient(PASSTHROUGH_HEADER)).thenReturn(internalApiClient);

        final var exception = assertThrows(OfficerServiceException.class,
                () -> testService.getListOfActiveDirectorsDetails(request, TRANSACTION_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER));
        assertThat(exception.getMessage(),
            containsString("Error Retrieving list of officers for company: " + COMPANY_NUMBER));
    }
}
