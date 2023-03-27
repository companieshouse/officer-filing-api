package uk.gov.companieshouse.officerfiling.api.controller;

import static uk.gov.companieshouse.officerfiling.api.utils.Constants.TRANSACTION_ID_KEY;

import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.service.OfficerService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper.Builder;

@RestController
public class OfficerControllerImpl implements OfficerController {

    private final OfficerService officerService;
    private final Logger logger;

    public OfficerControllerImpl(OfficerService officerService, final Logger logger) {
        this.officerService = officerService;
        this.logger = logger;
    }

    @GetMapping("/transactions/{transactionId}/officers/{filingResourceId}/active-officers-details")
    public ResponseEntity<Object> getListActiveDirectorsDetails(
            @RequestAttribute("transaction") Transaction transaction,
            final HttpServletRequest request) {

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transaction.getId());

        try {
            logger.debugContext(transaction.getId(), "Calling service to retrieve the active officers details", new Builder(transaction)
                .withRequest(request)
                .build());

            return ResponseEntity.status(HttpStatus.OK).body(officerService.getListActiveDirectorsDetails(request, transaction.getCompanyNumber()));
        } catch (OfficerServiceException e) {
            logger.debugContext(transaction.getId(), "Error retrieving active officers details", new Builder(transaction)
                .withRequest(request)
                .build());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
