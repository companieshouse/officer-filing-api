package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;

@ExtendWith(MockitoExtension.class)
class OfficerFilingControllerImplTest {
    private OfficerFilingController testController;
    @Mock
    private OfficerFilingService officerFilingService;
    @Mock
    private OfficerFilingMapper filingMapper;
    @Mock
    private OfficerFilingDto dto;
    @Mock
    private OfficerFiling filing;
    @Mock
    private BindingResult result;

    @BeforeEach
    void setUp() {
        testController = new OfficerFilingControllerImpl(officerFilingService, filingMapper);
    }

    @Test
    void createFiling() {
        when(filingMapper.map(dto)).thenReturn(filing);

        final var response = testController.createFiling("id", dto, result);

        verify(officerFilingService).save(filing);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    }
}