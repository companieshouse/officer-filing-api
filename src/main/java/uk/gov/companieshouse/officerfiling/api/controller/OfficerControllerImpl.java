package uk.gov.companieshouse.officerfiling.api.controller;

import static uk.gov.companieshouse.officerfiling.api.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.officerfiling.api.utils.Constants.TRANSACTION_ID_KEY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.model.entity.ActiveOfficerDetails;
import uk.gov.companieshouse.officerfiling.api.service.OfficerService;

@RestController
public class OfficerControllerImpl implements OfficerController {

    @Autowired
    private OfficerService officerService;
    private final Logger logger;

    public OfficerControllerImpl(final Logger logger) {
        this.logger = logger;
    }

    @GetMapping("/transactions/{transactionId}/officers/{filingResourceId}/active-officers-details")
    public ResponseEntity<List<ActiveOfficerDetails>> getListActiveDirectorDetails(
            @RequestAttribute("transaction") Transaction transaction,
            @PathVariable("transactionId") String transId,
            @RequestHeader(value = ERIC_REQUEST_ID_KEY) String reqId,
            final HttpServletRequest request) {

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transId);

        try {
            logger.debugContext(reqId, "Calling service to retrieve the active officers details."
                , logMap);
            return ResponseEntity.status(HttpStatus.OK).body(officerService.getListActiveDirectorDetails(request, transaction.getCompanyNumber()));
        } catch (OfficerServiceException e) {
            logger.errorContext(reqId, "Error retrieving active officers details.", e,
                logMap);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
