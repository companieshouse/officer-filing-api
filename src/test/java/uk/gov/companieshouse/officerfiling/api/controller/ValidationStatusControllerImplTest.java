package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ValidationStatusControllerImplTest {

    public static final String TRANS_ID = "117524-754816-491724";
    public static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private ValidationStatusControllerImpl testController;

    @BeforeEach
    void setUp() {
        testController = new ValidationStatusControllerImpl();
    }

    @Test
    void validate() {
        final var response= testController.validate(TRANS_ID, FILING_ID);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }
}