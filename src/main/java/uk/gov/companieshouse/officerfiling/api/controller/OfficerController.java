package uk.gov.companieshouse.officerfiling.api.controller;

import static uk.gov.companieshouse.officerfiling.api.utils.Constants.ERIC_REQUEST_ID_KEY;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;
import uk.gov.companieshouse.officerfiling.api.model.entity.ActiveOfficerDetails;

public interface OfficerController {

    /**
     * Create an Officer Filing.
     *
     * @param transaction the Transaction
     * @param transId        the Transaction ID
     * @param reqId the request ID
     * @throws NotImplementedException implementing classes must perform work
     */
    @GetMapping
    default ResponseEntity<List<ActiveOfficerDetails>> getListActiveDirectorDetails(
        @RequestAttribute("transaction") Transaction transaction,
        @PathVariable("transactionId") String transId,
        @RequestHeader(value = ERIC_REQUEST_ID_KEY) String reqId) {
        throw new NotImplementedException();
    }
}
