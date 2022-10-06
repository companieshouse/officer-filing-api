package uk.gov.companieshouse.officerfiling.api.service;

import static uk.gov.companieshouse.officerfiling.api.model.entity.Links.PREFIX_PRIVATE;

import java.io.IOException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.officerfiling.api.exception.TransactionServiceException;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final ApiClientService apiClientService;

    public TransactionServiceImpl(final ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @Override
    public Transaction getTransaction(final String transactionId,
            final String ericPassThroughHeader) throws TransactionServiceException {
        try {
            final var uri = "/transactions/" + transactionId;
            return apiClientService.getOauthAuthenticatedClient(ericPassThroughHeader)
                    .transactions()
                    .get(uri)
                    .execute()
                    .getData();
        }
        catch (final URIValidationException | IOException e) {
            throw new TransactionServiceException("Error Retrieving Transaction " + transactionId,
                    e);
        }
    }

    @Override
    public void updateTransaction(final Transaction transaction, final String ericPassThroughHeader)
            throws TransactionServiceException {
        try {
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
            throw new TransactionServiceException(
                    "Error Updating Transaction " + transaction.getId(), e);
        }
    }
}
