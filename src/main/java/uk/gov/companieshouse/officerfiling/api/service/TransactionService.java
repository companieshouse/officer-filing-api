package uk.gov.companieshouse.officerfiling.api.service;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.officerfiling.api.exception.TransactionServiceException;

public interface TransactionService {
    Transaction getTransaction(String transactionId, final String ericPassThroughHeader) throws TransactionServiceException;

    void updateTransaction(Transaction transaction, final String ericPassThroughHeader)
            throws TransactionServiceException;
}
