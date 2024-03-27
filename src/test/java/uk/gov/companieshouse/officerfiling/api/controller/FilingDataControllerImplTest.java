package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.FeatureNotEnabledException;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.service.FilingDataService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@ExtendWith(MockitoExtension.class)
class FilingDataControllerImplTest {

    public static final String TRANS_ID = "117524-754816-491724";
    public static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String PASSTHROUGH_HEADER = "passthrough";

    @Mock
    private OfficerFiling officerFiling;

    @Mock
    private FilingDataService filingDataService;

    @Mock
    private Logger logger;

    @Mock
    private HttpServletRequest request;

    private FilingDataControllerImpl testController;

    @BeforeEach
    void setUp() {
        testController = new FilingDataControllerImpl(filingDataService, logger);
        ReflectionTestUtils.setField(testController, "isTm01Enabled", true);
    }

    @Test
    void getFilingsData() {
        var filingApi = new FilingApi();
        when(filingDataService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(filingApi);

        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);

        final var filingsList= testController.getFilingsData(TRANS_ID, FILING_ID, request);

        assertThat(filingsList, Matchers.contains(filingApi));
    }

    @Test
    void getFilingsDataWhenNotFound() {

        when(filingDataService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER)).thenThrow(new ResourceNotFoundException("Test Resource not found"));
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);

        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> testController.getFilingsData(TRANS_ID, FILING_ID, request));
        assertThat(exception.getMessage(), is("Test Resource not found"));
    }

    @Test
    void checkTm01FeatureFlagDisabled(){
        ReflectionTestUtils.setField(testController, "isTm01Enabled", false);
        assertThrows(FeatureNotEnabledException.class,
            () -> testController.getFilingsData(TRANS_ID, FILING_ID, request));
    }
}