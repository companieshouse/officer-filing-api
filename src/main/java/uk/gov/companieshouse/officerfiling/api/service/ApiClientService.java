package uk.gov.companieshouse.officerfiling.api.service;

import java.io.IOException;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;

public interface ApiClientService {
    ApiClient getApiKeyAuthenticatedClient();

    ApiClient getOauthAuthenticatedClient(String ericPassThroughHeader) throws IOException;

    InternalApiClient getInternalOauthAuthenticatedClient(String ericPassThroughHeader)
            throws IOException;
}
