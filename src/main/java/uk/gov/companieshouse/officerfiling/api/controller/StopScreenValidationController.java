package uk.gov.companieshouse.officerfiling.api.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;

public interface StopScreenValidationController {
    /**
     * Check if company has a cessation date.
     *
     * @param companyNumber the company number to check
     * @param request       the servlet request
     * @throws NotImplementedException implementing classes must perform work
     */
    @GetMapping
    default ResponseEntity<Object> getCurrentOrFutureDissolved(
            @RequestAttribute("companyNumber") String companyNumber,
            HttpServletRequest request) {
        throw new NotImplementedException();
    }
}
