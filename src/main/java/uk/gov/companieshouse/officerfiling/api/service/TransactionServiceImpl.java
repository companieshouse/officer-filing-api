package uk.gov.companieshouse.officerfiling.api.service;

import static uk.gov.companieshouse.officerfiling.api.model.entity.Links.PREFIX_PRIVATE;

import java.io.IOException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.TransactionServiceException;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final ApiClientService apiClientService;
    private final Logger logger;
    private final LogHelper logHelper;

    public TransactionServiceImpl(final ApiClientService apiClientService, Logger logger, LogHelper logHelper) {
        this.apiClientService = apiClientService;
        this.logger = logger;
        this.logHelper = logHelper;
    }

    @Override
    public Transaction getTransaction(final String transactionId,
            final String ericPassThroughHeader) throws TransactionServiceException {
        try {
            final var uri = "/transactions/" + transactionId;
            final var transaction = apiClientService.getOauthAuthenticatedClient(ericPassThroughHeader)
                    .transactions()
                    .get(uri)
                    .execute()
                    .getData();
            final var logMap = logHelper.createLogMap(transactionId, null);
            logMap.put("company_number", transaction.getCompanyNumber());
            logMap.put("company_name", transaction.getCompanyName());
            logger.debugContext(transactionId, "Retrieved transaction details", logMap);
            return transaction;
        }
        catch (final URIValidationException | IOException e) {
            throw new TransactionServiceException("Error Retrieving Transaction " + transactionId,
                    e);
        }
    }

    @Override
    public void updateTransaction(final Transaction transaction, final String ericPassThroughHeader)
            throws TransactionServiceException {
        final var logMap = logHelper.createLogMap(transaction.getId(), null);
        try {
            logger.debugContext(transaction.getId(), "Updating transaction", logMap);
            final var uri = PREFIX_PRIVATE + "/transactions/" + transaction.getId();
            final var resp = apiClientService.getInternalOauthAuthenticatedClient(ericPassThroughHeader)
                    .privateTransaction()
                    .patch(uri, transaction)
                    .execute();

            if (resp.getStatusCode() != 204) {
                throw new IOException("Invalid Status Code received: " + resp.getStatusCode());
            }
        }
        catch (final IOException | URIValidationException e) {
            logger.errorContext(transaction.getId(), "Invalid Status Code received", e, logMap);
            throw new TransactionServiceException(
                    "Error Updating Transaction " + transaction.getId(), e);
        }
    }

}
