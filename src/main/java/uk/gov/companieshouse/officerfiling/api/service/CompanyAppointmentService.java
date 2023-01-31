package uk.gov.companieshouse.officerfiling.api.service;

import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyAppointmentServiceException;

public interface CompanyAppointmentService {
    /**
     * Retrieve a company appointment by appointment ID and company number.
     *
     * @param companyNumber the Company Number
     * @param appointmentId the Appointment ID
     * @param ericPassThroughHeader includes authorisation for company appointment fetch
     * @return the appointment if found
     * @throws CompanyAppointmentServiceException if Transaction not found or an error occurred
     */
    AppointmentFullRecordAPI getCompanyAppointment(String transactionId, String companyNumber, String appointmentId,
                                                   final String ericPassThroughHeader)
            throws CompanyAppointmentServiceException;

}
