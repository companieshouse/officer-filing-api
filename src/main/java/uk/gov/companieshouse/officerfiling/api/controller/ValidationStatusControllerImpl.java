package uk.gov.companieshouse.officerfiling.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;

@RestController
@RequestMapping("/private/transactions/{transId}/officers")
public class ValidationStatusControllerImpl implements ValidationStatusController {
    @Override
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{filingResourceId}/validation_status", produces = {"application/json"})
    public ValidationStatusResponse validate(@PathVariable("transId") final String transId,
                                     @PathVariable("filingResourceId") final String filingResource) {

        var validationStatus = new ValidationStatusResponse();
        validationStatus.setValid(true);
        return validationStatus;
    }
}
