package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;

@ExtendWith(MockitoExtension.class)
class OfficerFilingDataControllerImplTest {

    public static final String TRANS_ID = "117524-754816-491724";
    public static final String FILING_ID = "6332aa6ed28ad2333c3a520a";

    @Mock
    private OfficerFiling officerFiling;

    @Mock
    private OfficerFilingService officerFilingService;

    @Mock
    private Logger logger;

    private OfficerFilingDataControllerImpl testController;

    @BeforeEach
    void setUp() {
        testController = new OfficerFilingDataControllerImpl(officerFilingService, logger);
    }

    @Test
    void getFilingsData() {

        var filing = OfficerFiling.builder().build();
        when(officerFilingService.getFilingsData(FILING_ID)).thenReturn(Collections.singletonList(filing));
        final var filingsList= testController.getFilingsData(TRANS_ID, FILING_ID);

        assertThat(filingsList.get(0), is(filing));
        assertThat(filingsList, hasSize(1));
    }

    @Test
    void getFilingsDataWhenNotFound() {

        when(officerFilingService.getFilingsData(FILING_ID)).thenReturn(Collections.emptyList());

        assertThrows(
            ResourceNotFoundException.class, () -> testController.getFilingsData(TRANS_ID, FILING_ID));
    }
}