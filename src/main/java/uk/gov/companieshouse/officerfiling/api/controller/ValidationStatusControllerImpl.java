package uk.gov.companieshouse.officerfiling.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;

@RestController
@RequestMapping("/private/transactions/{transId}/officers")
public class ValidationStatusControllerImpl implements ValidationStatusController {
    private final OfficerFilingService officerFilingService;

    public ValidationStatusControllerImpl(OfficerFilingService officerFilingService) {
        this.officerFilingService = officerFilingService;
    }

    @Override
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{filingResourceId}/validation_status", produces = {"application/json"})
    public ValidationStatusResponse validate(@PathVariable("transId") final String transId,
                                     @PathVariable("filingResourceId") final String filingResource) {

        var maybeOfficerFiling = officerFilingService.get(filingResource);

        return maybeOfficerFiling.map(this::isValid).orElseThrow(ResourceNotFoundException::new);
    }

    private ValidationStatusResponse isValid(OfficerFiling officerFiling) {
        var validationStatus = new ValidationStatusResponse();
        validationStatus.setValid(true);
        return validationStatus;
    }
}
