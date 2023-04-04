package uk.gov.companieshouse.officerfiling.api.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.api.model.officers.OfficersApi;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

@Service
public class OfficerServiceImpl implements OfficerService {

    private static final List<String> ALLOWED_OFFICER_ROLES = List.of("director", "corporate-director", "nominee-director", "corporate-nominee-director");
    private final ApiClientService apiClientService;
    private final Logger logger;

    public OfficerServiceImpl(ApiClientService apiClientService, Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    /**
     * Query the officers service for a given company.
     *
     * @param transactionId the ID of the related transaction
     * @param companyNumber the Company Number
     * @param ericPassThroughHeader includes authorisation details
     * @return the list of officers if found
     * @throws OfficerServiceException if not found or an error occurred
     * @throws ServiceUnavailableException if public API is unavailable
     */

    @Override
    public List<CompanyOfficerApi> getListOfActiveDirectorsDetails(final HttpServletRequest request, final String transactionId,
        final String companyNumber, final String ericPassThroughHeader)
        throws OfficerServiceException {

            return getListOfActiveDirectors(
                getOfficersList(transactionId, companyNumber, ericPassThroughHeader
                ), request);
    }

    private OfficersApi getOfficersList(final String transactionId, final String companyNumber, final String ericPassThroughHeader)
            throws OfficerServiceException {
        try {
            final String uri = "/company/" + companyNumber + "/officers";
            final OfficersApi officersList = apiClientService.getInternalApiClient(ericPassThroughHeader)
                            .officers()
                            .list(uri)
                            .execute()
                            .getData();
            logger.debugContext(transactionId, "Retrieved list of Officers", new LogHelper.Builder(transactionId)
                    .withCompanyNumber(companyNumber)
                    .build());
            return officersList;
        }
        catch (final URIValidationException | IOException e) {
            throw new OfficerServiceException("Error Retrieving list of officers for company: " + companyNumber, e);
        }
    }

    private List<CompanyOfficerApi> getListOfActiveDirectors(OfficersApi officersList, HttpServletRequest request) {
        var directorsList = new ArrayList<CompanyOfficerApi>();

        for (CompanyOfficerApi officer : officersList.getItems()) {
            if (officer != null) {
                if (officer.getOfficerRole() != null) {
                    if (ALLOWED_OFFICER_ROLES.contains(officer.getOfficerRole().getOfficerRole()) && officer.getResignedOn() == null) {
                        directorsList.add(officer);
                    }
                }
                else {
                    logger.errorRequest(request, "null data was found in the oracle-query-api data within the Role field");
                }
            }
            else {
                logger.errorRequest(request, "null data was found in the oracle-query-api data, the officer was null");
            }
        }
        return directorsList;
    }
}
