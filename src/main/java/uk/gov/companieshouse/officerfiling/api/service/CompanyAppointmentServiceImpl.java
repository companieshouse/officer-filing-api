package uk.gov.companieshouse.officerfiling.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.HttpServerErrorException.ServiceUnavailable;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyAppointmentServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

import java.io.IOException;
import java.util.Map;

@Service
public class CompanyAppointmentServiceImpl implements CompanyAppointmentService{

    private final ApiClientService apiClientService;
    private final Logger logger;

    public CompanyAppointmentServiceImpl(ApiClientService apiClientService, Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    /**
     * Query the company appointment service for a given transaction.
     *
     * @param companyNumber the Company Number
     * @param appointmentId the Appointment ID
     * @param ericPassThroughHeader includes authorisation for company appointment fetch
     * @return the appointment if found
     * @throws CompanyAppointmentServiceException if not found or an error occurred
     */
    @Override
    public AppointmentFullRecordAPI getCompanyAppointment(String transactionId, String companyNumber, String appointmentId,
                                                          final String ericPassThroughHeader) throws CompanyAppointmentServiceException {
        try {
            final String uri = "/company/" + companyNumber + "/appointments/" + appointmentId + "/full_record";
            final AppointmentFullRecordAPI companyAppointment =
                    apiClientService.getInternalApiClient(ericPassThroughHeader)
                            .privateDeltaResourceHandler()
                            .getAppointment(uri)
                            .execute()
                            .getData();
            logger.debugContext(transactionId, "Retrieved company appointment details", new LogHelper.Builder(transactionId)
                    .withFilingId(appointmentId)
                    .withCompanyNumber(companyNumber)
                    .withCompanyName(companyAppointment.getName())
                    .build());
            return companyAppointment;
        }
        catch (final ApiErrorResponseException e) {
            throw new ServiceUnavailableException("The service is down. Try again later");
        }
        catch (final URIValidationException | IOException e) {
            throw new CompanyAppointmentServiceException("Error Retrieving appointment " + appointmentId + " for company " + companyNumber, e);
        }
    }
}
