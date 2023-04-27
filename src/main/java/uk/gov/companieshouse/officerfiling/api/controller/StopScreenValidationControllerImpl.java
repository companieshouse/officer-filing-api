package uk.gov.companieshouse.officerfiling.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

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
    @GetMapping(value = "/officer-filing/company/{companyNumber}/past-future-dissolved", produces = {"application/json"})
    public ResponseEntity<Object> getCurrentOrFutureDissolved(
            @RequestAttribute("companyNumber") String companyNumber,
            final HttpServletRequest request) {

        final var passthroughHeader =
                request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());

        final var transactionId = "No Transaction ID";

        final var companyProfile = companyProfileService.getCompanyProfile(transactionId, companyNumber, passthroughHeader);

        if (companyProfile.getDateOfCessation() != null || companyProfile.getCompanyStatus() == "dissolved") {
            return ResponseEntity.status(HttpStatus.OK).body(true);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(false);
        }
    }
}
