package uk.gov.companieshouse.officerfiling.api.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;

class ValidationStatusControllerTest {

    @Mock
    private HttpServletRequest request;

    @Test
    void validate() {

        var testController = new ValidationStatusController() {};

        assertThrows(NotImplementedException.class, () -> testController.validate("trans-id", "filing-id", request));

    }
}