package uk.gov.companieshouse.officerfiling.api.service;

import java.io.IOException;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;

/**
 * Retrieve CHS authenticated client objects used for interaction with external CHS services.
 * <ul>
 *   <li>{@link ApiClient} is for Public API access</li>
 *   <li>{@link InternalApiClient} is for Private API access</li>
 * </ul>
 */
public interface ApiClientService {
    ApiClient getApiKeyAuthenticatedClient();

    ApiClient getOauthAuthenticatedClient(String ericPassThroughHeader) throws IOException;

    InternalApiClient getInternalOauthAuthenticatedClient(String ericPassThroughHeader)
            throws IOException;
}
