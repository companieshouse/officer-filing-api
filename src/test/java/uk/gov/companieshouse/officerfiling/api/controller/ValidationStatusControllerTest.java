package uk.gov.companieshouse.officerfiling.api.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;

class ValidationStatusControllerTest {

    @Test
    void validate() {

        var testController = new ValidationStatusController() {};

        assertThrows(NotImplementedException.class, () -> testController.validate("trans-id", "filing-id"));

    }
}