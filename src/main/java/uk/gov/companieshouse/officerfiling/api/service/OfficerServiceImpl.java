package uk.gov.companieshouse.officerfiling.api.service;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.officers.OfficersApi;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.client.OracleQueryClient;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.model.entity.ActiveOfficerDetails;

@Service
public class OfficerServiceImpl implements OfficerService{

    private final ApiClientService apiClientService;
    private final OracleQueryClient oracleQueryClient;

    private final Logger logger;

    @Autowired
    public OfficerServiceImpl(ApiClientService apiClientService, OracleQueryClient oracleQueryClient,
        Logger logger) {
        this.apiClientService = apiClientService;
        this.oracleQueryClient = oracleQueryClient;
        this.logger = logger;
    }

    public List<ActiveOfficerDetails> getListActiveDirectorDetails(HttpServletRequest request, String companyNumber) throws OfficerServiceException {
        return createListOfActiveDirectors(request, oracleQueryClient.getActiveOfficersDetails(companyNumber));
    }

    private List<ActiveOfficerDetails> createListOfActiveDirectors(HttpServletRequest request, List<ActiveOfficerDetails> officersDetails) {
        var directorDetails = new ArrayList<ActiveOfficerDetails>();

        for (ActiveOfficerDetails officer : officersDetails) {
            if (officer != null) {
                if (officer.getRole() != null) {
                    if (officer.getRole().equals("Director")) {
                        directorDetails.add(officer);
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
        return directorDetails;
    }
}
