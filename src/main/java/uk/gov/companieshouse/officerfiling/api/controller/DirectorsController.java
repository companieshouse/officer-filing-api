package uk.gov.companieshouse.officerfiling.api.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
            @RequestAttribute("transaction") Transaction transaction,
            @RequestAttribute("filingId") String filingId, HttpServletRequest request) {
        throw new NotImplementedException();
    }
}