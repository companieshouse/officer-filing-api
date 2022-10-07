package uk.gov.companieshouse.officerfiling.api.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@ExtendWith(MockitoExtension.class)
class ApiClientServiceImplTest {
    public static final String PASSTHROUGH_HEADER = "passthrough";
    private ApiClientService testService;

    @Mock
    private ApiClient apiClient;
    @Mock
    private InternalApiClient internalApiClient;

    @BeforeEach
    void setUp() {
        testService = new ApiClientServiceImpl();
    }

    @Test
    void getApiKeyAuthenticatedClient() {
        try (final var sdkManager = mockStatic(ApiSdkManager.class)) {
            sdkManager.when(ApiSdkManager::getSDK).thenReturn(apiClient);

            final var client = testService.getApiKeyAuthenticatedClient();

            assertThat(client, is(apiClient));
        }
    }

    @Test
    void getOauthAuthenticatedClient() throws IOException {
        try (final var sdkManager = mockStatic(ApiSdkManager.class)) {
            sdkManager.when(() -> ApiSdkManager.getSDK(PASSTHROUGH_HEADER)).thenReturn(apiClient);

            final var client = testService.getOauthAuthenticatedClient(PASSTHROUGH_HEADER);

            assertThat(client, is(apiClient));
        }
    }

    @Test
    void getInternalOauthAuthenticatedClient() throws IOException {
        try (final var sdkManager = mockStatic(ApiSdkManager.class)) {
            sdkManager.when(() -> ApiSdkManager.getPrivateSDK(PASSTHROUGH_HEADER)).thenReturn(internalApiClient);

            final var client = testService.getInternalOauthAuthenticatedClient(PASSTHROUGH_HEADER);

            assertThat(client, is(internalApiClient));
        }
    }
}