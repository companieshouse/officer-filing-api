package uk.gov.companieshouse.officerfiling.api.controller;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.FeatureNotEnabledException;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@RestController
public class StopScreenValidationControllerImpl implements StopScreenValidationController {

    private final CompanyProfileService companyProfileService;
    @Value("${FEATURE_FLAG_ENABLE_TM01:true}")
    private boolean isTm01Enabled;

    public StopScreenValidationControllerImpl(final CompanyProfileService companyProfileService) {
        this.companyProfileService = companyProfileService;
    }

    @Override
    @ResponseBody
    @GetMapping(value = "/officer-filing/company/{companyNumber}/eligibility-check/past-future-dissolved", produces = {"application/json"})
    public ResponseEntity<Object> getCurrentOrFutureDissolved(
            @PathVariable("companyNumber") String companyNumber,
            final HttpServletRequest request) {

        if(!isTm01Enabled){
            throw new FeatureNotEnabledException();
        }

        try {
            final var passthroughHeader =
                    request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());

            final var transactionId = "No Transaction ID";

            final var companyProfile = companyProfileService.getCompanyProfile(transactionId, companyNumber, passthroughHeader);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(companyProfile.getDateOfCessation() != null || Objects.equals(companyProfile.getCompanyStatus(), "dissolved"));

        } catch (CompanyProfileServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
