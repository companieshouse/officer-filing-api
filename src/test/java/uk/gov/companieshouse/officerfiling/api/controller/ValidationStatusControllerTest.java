package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ValidationStatusControllerTest {

    @Test
    void validate() {
        final var response = new ValidationStatusController() {
        }.validate("trans-id", "filing-id");

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_IMPLEMENTED));
    }
}