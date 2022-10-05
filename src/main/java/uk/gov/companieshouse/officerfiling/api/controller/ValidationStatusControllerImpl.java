package uk.gov.companieshouse.officerfiling.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/private/transactions/{transId}/officers")
public class ValidationStatusControllerImpl implements ValidationStatusController {
    @Override
    @GetMapping(value = "/{filingResourceId}/validation_status", produces = {"application/json"})
    public ResponseEntity validate(@PathVariable("transId") final String transId,
                                   @PathVariable("filingResourceId") final String filingResource) {

        return ResponseEntity.ok().build();
    }
}
