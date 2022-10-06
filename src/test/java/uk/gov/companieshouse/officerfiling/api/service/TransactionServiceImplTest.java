package uk.gov.companieshouse.officerfiling.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.transaction.TransactionsResourceHandler;
import uk.gov.companieshouse.api.handler.transaction.request.TransactionsGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.officerfiling.api.exception.TransactionServiceException;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    public static final String PASS_THOROUGH = "passThorough";
    @Mock
    private ApiClientService apiClientService;
    @Mock
    private ApiClient apiClient;
    @Mock
    private TransactionsGet transactionsGet;
    @Mock
    private TransactionsResourceHandler transactionsResourceHandler;
    @Mock
    private ApiResponse<Transaction> apiResponse;

    private TransactionServiceImpl testService;

    @BeforeEach
    void setUp() {
        testService = new TransactionServiceImpl(apiClientService);
    }

    @Test
    public void getTransactionWhenFound() throws IOException, URIValidationException {
        var transactionId = "12345";

        when(apiResponse.getData()).thenReturn(testTransaction(transactionId));
        when(transactionsGet.execute()).thenReturn(apiResponse);
        when(transactionsResourceHandler.get("/transactions/" + transactionId)).thenReturn(transactionsGet);
        when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
        when(apiClientService.getOauthAuthenticatedClient(PASS_THOROUGH)).thenReturn(apiClient);

        var transaction = testService.getTransaction(transactionId, "passThorough");

        assertThat(transaction, samePropertyValuesAs(testTransaction(transactionId)));
    }

    @Test
    public void getTransactionWhenNotFound() throws IOException, URIValidationException {
        var transactionId = "12345";

        when(apiClientService.getOauthAuthenticatedClient(PASS_THOROUGH)).thenThrow(IOException.class);

        assertThrows(TransactionServiceException.class, () ->  testService.getTransaction(transactionId, "passThorough"));
    }

    private Transaction testTransaction(String id) {
        var transaction = new Transaction();
        transaction.setId(id);
        return transaction;
    }

}