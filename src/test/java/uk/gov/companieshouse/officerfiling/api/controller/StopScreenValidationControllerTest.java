package uk.gov.companieshouse.officerfiling.api.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;

class StopScreenValidationControllerTest {

    @Mock
    private HttpServletRequest request;

    @Test
    void getCurrentOrFutureDissolved() {

        var testController = new StopScreenValidationController(){};

        assertThrows(NotImplementedException.class,
                () -> testController.getCurrentOrFutureDissolved("companyNumber", request));
    }
}
