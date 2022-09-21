package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;

@ExtendWith(MockitoExtension.class)
class OfficerFilingControllerTest {

    @Mock
    private OfficerFilingDto dto;
    @Mock
    private BindingResult bindingResult;

    @Test
    void createFiling() {
        final var response = new OfficerFilingController() {
        }.createFiling("trans-id", dto, bindingResult);

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_IMPLEMENTED));
    }
}