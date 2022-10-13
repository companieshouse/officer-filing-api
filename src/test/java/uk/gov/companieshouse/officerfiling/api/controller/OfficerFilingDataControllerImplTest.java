package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.service.FilingService;

@ExtendWith(MockitoExtension.class)
class OfficerFilingDataControllerImplTest {

    public static final String TRANS_ID = "117524-754816-491724";
    public static final String FILING_ID = "6332aa6ed28ad2333c3a520a";

    @Mock
    private OfficerFiling officerFiling;

    @Mock
    private FilingService filingService;

    @Mock
    private Logger logger;

    @Mock
    private HttpServletRequest request;

    private OfficerFilingDataControllerImpl testController;

    @BeforeEach
    void setUp() {
        testController = new OfficerFilingDataControllerImpl(filingService, logger);
    }

    @Test
    void getFilingsData() {
        var filingApi = new FilingApi();
        when(filingService.generateOfficerFiling(FILING_ID)).thenReturn(filingApi);
        final var filingsList= testController.getFilingsData(TRANS_ID, FILING_ID, request);

        assertThat(filingsList, Matchers.contains(filingApi));
    }

    @Test
    void getFilingsDataWhenNotFound() {

        when(filingService.generateOfficerFiling(FILING_ID)).thenThrow(new ResourceNotFoundException("Test Resource not found"));

        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> testController.getFilingsData(TRANS_ID, FILING_ID, request));
        assertThat(exception.getMessage(), is("Test Resource not found"));
    }
}