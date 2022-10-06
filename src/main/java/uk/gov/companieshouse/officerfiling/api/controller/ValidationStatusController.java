package uk.gov.companieshouse.officerfiling.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ValidationStatusController {
    @GetMapping(value = "/{filingResourceId}/validation_status", produces = {"application/json"})
    default ResponseEntity validate(@PathVariable("transId") String transId,
                                    @PathVariable("filingResourceId") String filingResource) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

}
