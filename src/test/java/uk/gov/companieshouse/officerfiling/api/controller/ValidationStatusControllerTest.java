package uk.gov.companieshouse.officerfiling.api.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;

@ExtendWith(MockitoExtension.class)
class ValidationStatusControllerTest {

    private final ValidationStatusController testController = new ValidationStatusController() {
    };
    @Mock
    private HttpServletRequest request;
    @Mock
    private Transaction transaction;

    @Test
    void validate() {
        assertThrows(NotImplementedException.class, () -> testController.validate(
            transaction,"6332aa6ed28ad2333c3a520a", request));
    }

}