package uk.gov.companieshouse.officerfiling.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyAppoinmentServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.TransactionServiceException;
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
     * @throws CompanyAppoinmentServiceException if not found or an error occurred
     */

    @Override
    public AppointmentFullRecordAPI getCompanyAppointment(String companyNumber, String appointmentId,
                                                          final String ericPassThroughHeader) throws CompanyAppoinmentServiceException {
        try {
            final String uri = "/company/" + companyNumber + "/appointments/" + appointmentId + "/full_record";
            final AppointmentFullRecordAPI companyAppointment =
                    apiClientService.getInternalOauthAuthenticatedClient(ericPassThroughHeader)
                            .privateDeltaResourceHandler()
                            .getAppointment(uri)
                            .execute()
                            .getData();
            final Map logMap = LogHelper.createLogMap(companyNumber, appointmentId);
            //logMap.put("company_number", companyAppointment.);
            //logMap.put("company_name", companyAppointment.g);
            logger.debugContext(appointmentId, "Retrieved company appointment details", logMap);
            return companyAppointment;
        }
        catch (final URIValidationException | IOException e) {
            throw new CompanyAppoinmentServiceException("Error Retrieving appointment " + appointmentId + " for company " + companyNumber,
                    e);
        }
    }
}
