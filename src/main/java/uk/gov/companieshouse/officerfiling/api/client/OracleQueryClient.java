package uk.gov.companieshouse.officerfiling.api.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.model.entity.ActiveOfficerDetails;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

@Component
public class OracleQueryClient {

    public static final String ORACLE_QUERY_API_STATUS_MESSAGE = "Oracle query api returned with status = %s, companyNumber = %s";
    @Value("${ORACLE_QUERY_API_URL}")
    private String oracleQueryApiUrl;
    @Autowired
    private final RestTemplate restTemplate;
    private final Logger logger;

    public OracleQueryClient(RestTemplate restTemplate, Logger logger) {
        this.restTemplate = restTemplate;
        this.logger = logger;
    }

    public List<ActiveOfficerDetails> getActiveOfficersDetails(String companyNumber) throws OfficerServiceException {
        var officersDetailsUrl = String.format("%s/company/%s/officers/active", oracleQueryApiUrl, companyNumber);

        logger.debugContext(companyNumber, "Calling Oracle Query API URL",
            new LogHelper.Builder(companyNumber).build());

        ResponseEntity<ActiveOfficerDetails[]> response = restTemplate.getForEntity(officersDetailsUrl, ActiveOfficerDetails[].class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new OfficerServiceException(String.format(ORACLE_QUERY_API_STATUS_MESSAGE, response.getStatusCode(), companyNumber));
        }
        if (response.getBody() == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(response.getBody());
    }
}
