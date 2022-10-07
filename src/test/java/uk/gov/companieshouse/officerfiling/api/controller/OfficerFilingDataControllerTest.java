package uk.gov.companieshouse.officerfiling.api.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;

class OfficerFilingDataControllerTest {

    @Test
    void getFilingsData() {

        var testController = new OfficerFilingDataController(){};

        assertThrows(NotImplementedException.class,
            () -> testController.getFilingsData("trans-id", "filing-id"));
    }
}