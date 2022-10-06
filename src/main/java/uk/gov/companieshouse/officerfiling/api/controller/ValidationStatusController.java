package uk.gov.companieshouse.officerfiling.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;

public interface ValidationStatusController {
    @GetMapping(value = "/{filingResourceId}/validation_status", produces = {"application/json"})
    default ValidationStatusResponse validate(@PathVariable("transId") String transId,
                                              @PathVariable("filingResourceId") String filingResource) {

        throw new NotImplementedException();
    }

}
