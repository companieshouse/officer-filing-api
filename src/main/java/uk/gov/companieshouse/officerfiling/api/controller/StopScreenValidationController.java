package uk.gov.companieshouse.officerfiling.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import uk.gov.companieshouse.api.company.CompanyProfile;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;

import javax.servlet.http.HttpServletRequest;

public interface StopScreenValidationController {
    /**
     * Check if company has a cessation date.
     *
     * @param companyNumber the company number to check
     * @param request the servlet request
     * @throws NotImplementedException implementing classes must perform work
     */
    @GetMapping
    default ResponseEntity<Object> getHasCessationDate(
            @RequestAttribute("companyNumber") String companyNumber,
            HttpServletRequest request) {
        throw new NotImplementedException();
    }
}
