package uk.gov.companieshouse.officerfiling.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

import java.io.IOException;

@Service
public class CompanyProfileServiceImpl implements CompanyProfileService {

    private final ApiClientService apiClientService;
    private final Logger logger;

    public CompanyProfileServiceImpl(ApiClientService apiClientService, Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    /**
     * Query the company profile service for a given transaction.
     *
     * @param transactionId the ID of the related transaction
     * @param companyNumber the Company Number
     * @param ericPassThroughHeader includes authorisation details
     * @return the company profile if found
     * @throws CompanyProfileServiceException if not found or an error occurred
     * @throws ServiceUnavailableException if public API is unavailable
     */
    @Override
    public CompanyProfileApi getCompanyProfile(final String transactionId, final String companyNumber, final String ericPassThroughHeader)
            throws CompanyProfileServiceException {
        try {
            final String uri = "/company/" + companyNumber;
            final CompanyProfileApi companyProfile = apiClientService.getInternalApiClient(ericPassThroughHeader)
                            .company()
                            .get(uri)
                            .execute()
                            .getData();
            logger.debugContext(transactionId, "Retrieved company profile details", new LogHelper.Builder(transactionId)
                    .withCompanyNumber(companyNumber)
                    .withCompanyName(companyProfile.getCompanyName())
                    .build());
            return companyProfile;
        }
        catch (final ApiErrorResponseException e) {
            throw new ServiceUnavailableException("The service is down. Try again later");
        }
        catch (final URIValidationException | IOException e) {
            throw new CompanyProfileServiceException("Error Retrieving company profile " + companyNumber, e);
        }
    }
}
