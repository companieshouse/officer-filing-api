package uk.gov.companieshouse.officerfiling.api.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

@RestController
@RequestMapping("/private/transactions/{transId}/officers")
public class ValidationStatusControllerImpl implements ValidationStatusController {
    private final OfficerFilingService officerFilingService;
    private final Logger logger;
    private final LogHelper logHelper;

    public ValidationStatusControllerImpl(OfficerFilingService officerFilingService, Logger logger, LogHelper logHelper) {
        this.officerFilingService = officerFilingService;
        this.logger = logger;
        this.logHelper = logHelper;
    }

    @Override
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{filingResourceId}/validation_status", produces = {"application/json"})
    public ValidationStatusResponse validate(@PathVariable("transId") final String transId,
                                             @PathVariable("filingResourceId") final String filingResource,
                                             final HttpServletRequest request) {

        final var logMap = logHelper.createLogMap(transId, filingResource);
        logMap.put("path", request.getRequestURI());
        logMap.put("method", request.getMethod());
        logger.debugRequest(request, "GET validation request", logMap);

        var maybeOfficerFiling = officerFilingService.get(filingResource, transId);

        return maybeOfficerFiling.map(this::isValid).orElseThrow(ResourceNotFoundException::new);
    }

    private ValidationStatusResponse isValid(OfficerFiling officerFiling) {
        var validationStatus = new ValidationStatusResponse();
        validationStatus.setValid(true);
        return validationStatus;
    }
}
