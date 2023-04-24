package uk.gov.companieshouse.officerfiling.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileServiceImpl;

import javax.servlet.http.HttpServletRequest;

public class StopScreenValidationControllerImpl implements StopScreenValidationController {

    private final Logger logger;

    private final CompanyProfileService companyProfileService;

    public StopScreenValidationControllerImpl(final Logger logger, final CompanyProfileService companyProfileService) {
        this.logger = logger;
        this.companyProfileService = companyProfileService;
    }

    @Override
    @ResponseBody
    @GetMapping(value = "/company/{companyNumber}", produces = {"application/json"})
    public ResponseEntity<Object> getHasCessationDate(
            @RequestAttribute("companyNumber") String companyNumber,
            @RequestAttribute("transaction") Transaction transaction,
            final HttpServletRequest request) {

        final var companyProfile = companyProfileService.getHasCessationDate(companyNumber, transaction, request);

        //check dissolved status too

        if (companyProfile.getDateOfCessation() != null) {
            return ResponseEntity.status(HttpStatus.OK).body(true);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(false);
        }
    }
}
