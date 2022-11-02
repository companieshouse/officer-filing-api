package uk.gov.companieshouse.officerfiling.api.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;

class FilingDataControllerTest {

    @Mock
    private HttpServletRequest request;

    @Test
    void getFilingsData() {

        var testController = new FilingDataController(){};

        assertThrows(NotImplementedException.class,
            () -> testController.getFilingsData("trans-id", "filing-id", request));
    }
}