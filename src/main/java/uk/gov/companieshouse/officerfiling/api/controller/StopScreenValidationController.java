package uk.gov.companieshouse.officerfiling.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.companieshouse.api.company.CompanyProfile;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;

import javax.servlet.http.HttpServletRequest;

public interface StopScreenValidationController {
    /**
     * Check if company has a cessation date.
     *
     * @param companyNumber the company number to check
     * @param transactionId the transaction id passed for logging
     * @param request       the servlet request
     * @throws NotImplementedException implementing classes must perform work
     */
    @GetMapping
    default ResponseEntity<Object> getCurrentOrFutureDissolved(
            @RequestAttribute("companyNumber") String companyNumber,
            @RequestAttribute("transactionId") String transactionId,
            HttpServletRequest request) {
        throw new NotImplementedException();
    }
}
