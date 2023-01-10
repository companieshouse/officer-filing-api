package uk.gov.companieshouse.officerfiling.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.company.appointment.request.PrivateOfficerGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyAppointmentServiceException;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyAppointmentServiceImplTest {

    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String NAME = "Joe";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String APPOINTMENT_ID = "app1";

    @Mock
    private ApiClientService apiClientService;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private PrivateOfficerGet getAppointment;
    @Mock
    private PrivateDeltaResourceHandler privateDeltaResourceHandler;
    @Mock
    private ApiResponse<AppointmentFullRecordAPI> apiResponse;
    @Mock
    private Logger logger;
    private CompanyAppointmentServiceImpl testService;

    @BeforeEach
    void setUp() {
        testService = new CompanyAppointmentServiceImpl(apiClientService, logger);
    }

    @Test
    void companyAppointmentIsReturnedFromCompanyAppointmentsAPIWhenFound() throws IOException, URIValidationException {
        when(apiResponse.getData()).thenReturn(testCompanyAppointment(NAME));
        when(getAppointment.execute()).thenReturn(apiResponse);
        when(privateDeltaResourceHandler.getAppointment("/company/" + COMPANY_NUMBER + "/appointments/" + APPOINTMENT_ID + "/full_record")).thenReturn(getAppointment);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(apiClientService.getInternalApiKeyAuthenticatedClient()).thenReturn(internalApiClient);

        AppointmentFullRecordAPI companyAppointment = testService.getCompanyAppointment(COMPANY_NUMBER,APPOINTMENT_ID, PASSTHROUGH_HEADER);

        assertThat(companyAppointment, samePropertyValuesAs(testCompanyAppointment(NAME)));
    }

    @Test
    void exceptionIsThrownWhenCompanyAppointmentIsNotFound() throws IOException, URIValidationException {
        when(getAppointment.execute()).thenThrow(URIValidationException.class);
        when(privateDeltaResourceHandler.getAppointment("/company/" + COMPANY_NUMBER + "/appointments/" + APPOINTMENT_ID + "/full_record")).thenReturn(getAppointment);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(apiClientService.getInternalApiKeyAuthenticatedClient()).thenReturn(internalApiClient);

        final var exception = assertThrows(CompanyAppointmentServiceException.class,
                () -> testService.getCompanyAppointment(COMPANY_NUMBER, APPOINTMENT_ID, PASSTHROUGH_HEADER));
        assertThat(exception.getMessage(),
                is("Error Retrieving appointment " + APPOINTMENT_ID + " for company " + COMPANY_NUMBER));
    }

    private AppointmentFullRecordAPI testCompanyAppointment(String name) {
        var companyAppointment = new AppointmentFullRecordAPI();
        companyAppointment.setName(name);
        return companyAppointment;
    }

}
