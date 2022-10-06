package uk.gov.companieshouse.officerfiling.api.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;

class OfficerFilingDataControllerImplTest {

    @Test
    void getFilingsData() {

        assertThrows(NotImplementedException.class, () -> new OfficerFilingDataController() {
        }.getFilingsData("trans-id", "filing-id"));
    }
}