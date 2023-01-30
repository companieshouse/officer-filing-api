package uk.gov.companieshouse.officerfiling.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.http.HttpStatusCodes;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.privatetransaction.PrivateTransactionResourceHandler;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionPatch;
import uk.gov.companieshouse.api.handler.transaction.TransactionsResourceHandler;
import uk.gov.companieshouse.api.handler.transaction.request.TransactionsGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.TransactionServiceException;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    public static final String PASSTHROUGH_HEADER = "passthrough";
    public static final String TRANS_ID = "12345";
    @Mock
    private ApiClientService apiClientService;
    @Mock
    private ApiClient apiClient;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private TransactionsGet transactionsGet;
    @Mock
    private TransactionsResourceHandler transactionsResourceHandler;
    @Mock
    private PrivateTransactionResourceHandler privateTransactionResourceHandler;
    @Mock
    private ApiResponse<Transaction> apiResponse;
    @Mock
    private ApiResponse<Void> apiResponseVoid;
    @Mock
    private PrivateTransactionPatch privateTransactionPatch;
    @Mock
    private Logger logger;
    @Mock
    private LogHelper logHelper;

    private Transaction testTransaction;
    private TransactionServiceImpl testService;

    @BeforeEach
    void setUp() {
        testService = new TransactionServiceImpl(apiClientService, logger);
        testTransaction = testTransaction(TRANS_ID);
    }

    @Test
    void getTransactionWhenFound() throws IOException, URIValidationException {
        when(apiResponse.getData()).thenReturn(testTransaction(TRANS_ID));
        when(transactionsGet.execute()).thenReturn(apiResponse);
        when(transactionsResourceHandler.get("/transactions/" + TRANS_ID)).thenReturn(transactionsGet);
        when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);

        var transaction = testService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER);

        assertThat(transaction, samePropertyValuesAs(testTransaction(TRANS_ID)));
    }

    @Test
    void getTransactionWhenNotFound() throws IOException, URIValidationException {
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenThrow(IOException.class);

        assertThrows(TransactionServiceException.class, () -> testService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER));
    }

    @Test
    void updateTransactionWhenResponse204() throws IOException, URIValidationException {
        when(privateTransactionPatch.execute()).thenReturn(apiResponseVoid);
        when(privateTransactionResourceHandler.patch("/private/transactions/12345", testTransaction)).thenReturn(
                privateTransactionPatch);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(apiClientService.getInternalApiClient(PASSTHROUGH_HEADER)).thenReturn(internalApiClient);

        when(apiResponseVoid.getStatusCode()).thenReturn(HttpStatusCodes.STATUS_CODE_NO_CONTENT);
        testService.updateTransaction(testTransaction, PASSTHROUGH_HEADER);

        verify(apiResponseVoid).getStatusCode();
    }

    @Test
    void updateTransactionWhenResponseNot204() throws IOException, URIValidationException {
        when(privateTransactionPatch.execute()).thenReturn(apiResponseVoid);
        when(privateTransactionResourceHandler.patch("/private/transactions/12345", testTransaction)).thenReturn(
                privateTransactionPatch);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(apiClientService.getInternalApiClient(PASSTHROUGH_HEADER)).thenReturn(internalApiClient);

        when(apiResponseVoid.getStatusCode()).thenReturn(HttpStatusCodes.STATUS_CODE_NOT_FOUND);

        assertThrows(TransactionServiceException.class,
                () -> testService.updateTransaction(testTransaction, PASSTHROUGH_HEADER));
    }

    private Transaction testTransaction(String id) {
        var transaction = new Transaction();
        transaction.setId(id);
        return transaction;
    }

}