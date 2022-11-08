package uk.gov.companieshouse.officerfiling.api.service;

import static uk.gov.companieshouse.officerfiling.api.model.entity.Links.PREFIX_PRIVATE;

import java.io.IOException;
import org.springframework.http.HttpStatus;
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

    public TransactionServiceImpl(final ApiClientService apiClientService, Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    /**
     * Query the transaction service for a given transaction.
     *
     * @param transactionId         the Transaction ID
     * @param ericPassThroughHeader includes authorisation for the transaction query
     * @return the transaction if found
     *
     * @throws TransactionServiceException if not found or an error occurred
     */
    @Override
    public Transaction getTransaction(final String transactionId,
            final String ericPassThroughHeader) throws TransactionServiceException {
        try {
            final var uri = "/transactions/" + transactionId;
            final var transaction =
                    apiClientService.getOauthAuthenticatedClient(ericPassThroughHeader)
                            .transactions()
                            .get(uri)
                            .execute()
                            .getData();
            final var logMap = LogHelper.createLogMap(transactionId);
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

    /**
     * Update a given transaction via the transaction service.
     *
     * @param transaction           the Transaction ID
     * @param ericPassThroughHeader includes authorisation for transaction update
     * @throws TransactionServiceException if the transaction update failed
     */
    @Override
    public void updateTransaction(final Transaction transaction, final String ericPassThroughHeader)
            throws TransactionServiceException {
        final var logMap = LogHelper.createLogMap(transaction.getId());
        try {
            logger.debugContext(transaction.getId(), "Updating transaction", logMap);
            final var uri = PREFIX_PRIVATE + "/transactions/" + transaction.getId();
            final var resp =
                    apiClientService.getInternalOauthAuthenticatedClient(ericPassThroughHeader)
                            .privateTransaction()
                            .patch(uri, transaction)
                            .execute();

            if (HttpStatus.NO_CONTENT.value() != resp.getStatusCode()) {
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
