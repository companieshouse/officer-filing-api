package uk.gov.companieshouse.officerfiling.api.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;

@ExtendWith(MockitoExtension.class)
class OfficerFilingControllerTest {

    private final OfficerFilingController testController = new OfficerFilingController() {
    };
    @Mock
    private OfficerFilingDto dto;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Transaction transaction;

    @Test
    void createFiling() {
        assertThrows(NotImplementedException.class,
                () -> testController.createFiling(transaction, dto, bindingResult, request));
    }

    @Test
    void getFilingForReview() {
        assertThrows(NotImplementedException.class,
                () -> testController.getFilingForReview("trans-id", "filing-resource"));
    }
}