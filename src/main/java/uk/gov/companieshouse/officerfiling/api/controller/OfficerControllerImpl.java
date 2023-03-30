package uk.gov.companieshouse.officerfiling.api.controller;

import static uk.gov.companieshouse.officerfiling.api.utils.Constants.TRANSACTION_ID_KEY;

import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
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

    public OfficerControllerImpl(final OfficerService officerService, final Logger logger) {
        this.officerService = officerService;
        this.logger = logger;
    }

    @Override
    @ResponseBody
    @GetMapping(value = "/private/transactions/{transactionId}/officers/active-directors-details", produces = {"application/json"})
    public ResponseEntity<Object> getListActiveDirectorsDetails(
            @RequestAttribute("transaction") Transaction transaction,
            final HttpServletRequest request) {

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transaction.getId());

        try {
            logger.debugContext(transaction.getId(), "Calling service to retrieve the active officers details", new Builder(transaction)
                .withRequest(request)
                .build());

            final var directorsDetails = officerService.getListActiveDirectorsDetails(request, transaction.getCompanyNumber());

            return ResponseEntity.status(HttpStatus.OK).body(directorsDetails);
        } catch (OfficerServiceException e) {
            logger.debugContext(transaction.getId(), "Error retrieving active officers details", new Builder(transaction)
                .withRequest(request)
                .build());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
