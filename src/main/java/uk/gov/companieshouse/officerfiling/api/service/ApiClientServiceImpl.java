package uk.gov.companieshouse.officerfiling.api.service;

import java.io.IOException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Service
public class ApiClientServiceImpl implements ApiClientService {

    @Override
    public ApiClient getApiKeyAuthenticatedClient() {
        return ApiSdkManager.getSDK();
    }

    @Override
    public ApiClient getOauthAuthenticatedClient(final String ericPassThroughHeader) throws IOException {
        return ApiSdkManager.getSDK(ericPassThroughHeader);
    }

    @Override
    public InternalApiClient getInternalOauthAuthenticatedClient(final String ericPassThroughHeader)
            throws IOException {
        return ApiSdkManager.getPrivateSDK(ericPassThroughHeader);
    }
}
