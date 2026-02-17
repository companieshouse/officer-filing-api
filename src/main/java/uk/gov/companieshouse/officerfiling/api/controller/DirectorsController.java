package uk.gov.companieshouse.officerfiling.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;

public interface DirectorsController {

    /**
     * Retrieve list of directors.
     *
     * @param transaction the Transaction
     * @param request the servlet request
     * @throws NotImplementedException implementing classes must perform work
     */
    @GetMapping
    default ResponseEntity<Object> getListActiveDirectorsDetails(
        @PathVariable("transactionId") String unusedTransactionId,
        @RequestAttribute("transaction") Transaction transaction,
        HttpServletRequest request) {
        throw new NotImplementedException();
    }

    /**
     * Retrieve information needed for the TM01 check your answers page.
     *
     * @param transaction the Transaction
     * @param request the servlet request
     * @throws NotImplementedException implementing classes must perform work
     */
    @GetMapping
    default ResponseEntity<Object> getRemoveCheckAnswersDirectorDetails(
            @PathVariable("transactionId") String unusedTransactionId,
            @RequestAttribute("transaction") Transaction transaction,
            @PathVariable("filingId") String filingId, HttpServletRequest request) {
        throw new NotImplementedException();
    }
}