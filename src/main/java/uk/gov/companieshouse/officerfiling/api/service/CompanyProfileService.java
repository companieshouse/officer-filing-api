package uk.gov.companieshouse.officerfiling.api.service;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyAppointmentServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyProfileServiceException;

public interface CompanyProfileService {

    /**
     * Query the company profile service for a given transaction.
     *
     * @param transactionId the ID of the related transaction
     * @param companyNumber the Company Number
     * @param ericPassThroughHeader includes authorisation details
     * @return the company profile if found
     * @throws CompanyProfileServiceException if not found or an error occurred
     */
    CompanyProfileApi getCompanyProfile(final String transactionId, final String companyNumber, final String ericPassThroughHeader)
            throws CompanyAppointmentServiceException;

}
