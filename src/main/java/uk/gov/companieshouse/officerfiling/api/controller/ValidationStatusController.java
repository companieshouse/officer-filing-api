package uk.gov.companieshouse.officerfiling.api.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;
import uk.gov.companieshouse.officerfiling.api.model.entity.Links;

public interface ValidationStatusController {
    /**
     * Controller endpoint: Perform final validation checks.
     * Handle requests from the Transaction service attempting to close a Transaction belonging
     * to this Filing resource.
     * This endpoint's URI is provided by the Filing resource in
     * {@link Links#getValidationStatus()}.
     *
     * @param transId        the Transaction ID
     * @param filingResource the Filing resource ID
     * @param request        the servlet request
     * @throws NotImplementedException implementing classes must perform work
     */
    @GetMapping(value = "/{filingResourceId}/validation_status", produces = {"application/json"})
    default ValidationStatusResponse validate(@PathVariable("transactionId") String transId,
            @PathVariable("filingResourceId") String filingResource,
            final HttpServletRequest request) {
        throw new NotImplementedException();
    }

}
