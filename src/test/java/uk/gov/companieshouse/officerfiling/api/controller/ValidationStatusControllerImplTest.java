package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
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
class ValidationStatusControllerImplTest {

    public static final String TRANS_ID = "117524-754816-491724";
    public static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    @Mock
    private OfficerFilingService officerFilingService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Logger logger;

    private ValidationStatusControllerImpl testController;

    @BeforeEach
    void setUp() {
        testController = new ValidationStatusControllerImpl(officerFilingService, logger);
    }

    @Test
    void validateWhenFound() {
        var filing = OfficerFiling.builder().build();
        when(officerFilingService.get(FILING_ID, )).thenReturn(Optional.of(filing));
        final var response= testController.validate(TRANS_ID, FILING_ID, request);

        assertThat(response.isValid(), is(true));
    }

    @Test
    void validateWhenNotFound() {
        when(officerFilingService.get(FILING_ID, )).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> testController.validate(TRANS_ID, FILING_ID, request));
    }
}