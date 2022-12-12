package uk.gov.companieshouse.officerfiling.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.company.appointment.request.PrivateOfficerGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.privatetransaction.PrivateTransactionResourceHandler;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionPatch;
import uk.gov.companieshouse.api.handler.transaction.TransactionsResourceHandler;
import uk.gov.companieshouse.api.handler.transaction.request.TransactionsGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyAppoinmentServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.TransactionServiceException;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CompanyAppointmentServiceImplTest {

    public static final String PASSTHROUGH_HEADER = "passthrough";
    public static final String NAME = "Joe";
    public static final String COMPANY_NUMBER = "Joe";
    public static final String APPOINTMENT_ID = "Joe";

    @Mock
    private ApiClientService apiClientService;
    @Mock
    private ApiClient apiClient;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private PrivateOfficerGet getAppointment;
    @Mock
    private PrivateDeltaResourceHandler privateDeltaResourceHandler;
    @Mock
    private PrivateTransactionResourceHandler privateTransactionResourceHandler;
    @Mock
    private ApiResponse<AppointmentFullRecordAPI> apiResponse;
    @Mock
    private ApiResponse<Void> apiResponseVoid;
    @Mock
    private PrivateTransactionPatch privateTransactionPatch;
    @Mock
    private Logger logger;
    @Mock
    private LogHelper logHelper;

    private Transaction testTransaction;
    private CompanyAppointmentServiceImpl testService;

    @BeforeEach
    void setUp() {
        testService = new CompanyAppointmentServiceImpl(apiClientService, logger);
    }

    @Test
    void companyAppoinmentIsReturnedFromCompanyApoointmentsAPIWhenFound() throws IOException, URIValidationException {
        when(apiResponse.getData()).thenReturn(testCompanyAppointment(NAME));
        when(getAppointment.execute()).thenReturn(apiResponse);
        when(privateDeltaResourceHandler.getAppointment("/company/" + COMPANY_NUMBER + "/appointments/" + APPOINTMENT_ID + "/full_record")).thenReturn(getAppointment);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(apiClientService.getInternalOauthAuthenticatedClient(PASSTHROUGH_HEADER)).thenReturn(internalApiClient);

        AppointmentFullRecordAPI companyAppointment = testService.getCompanyAppointment(COMPANY_NUMBER,APPOINTMENT_ID, PASSTHROUGH_HEADER);

        assertThat(companyAppointment, samePropertyValuesAs(testCompanyAppointment(NAME)));
    }

    @Test
    void exceptionIsThrownWhenCompanyAppointmentIsNotFound() throws IOException, URIValidationException {
        when(apiClientService.getInternalOauthAuthenticatedClient(PASSTHROUGH_HEADER)).thenThrow(IOException.class);

        assertThrows(CompanyAppoinmentServiceException.class, () -> testService.getCompanyAppointment(COMPANY_NUMBER, APPOINTMENT_ID, PASSTHROUGH_HEADER));
    }

    private AppointmentFullRecordAPI testCompanyAppointment(String name) {
        var companyAppointment = new AppointmentFullRecordAPI();
        companyAppointment.setName(name);
        return companyAppointment;
    }

}
