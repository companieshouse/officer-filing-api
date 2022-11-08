package uk.gov.companieshouse.officerfiling.api.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;

public interface OfficerFilingController {
    /**
     * Create an Officer Filing.
     *
     * @param transId the Transaction ID
     * @param dto     the request body payload DTO
     * @param result  the MVC binding result (with any validation errors)
     * @param request the servlet request
     * @throws NotImplementedException implementing classes must perform work
     */
    @PostMapping
    default ResponseEntity<Object> createFiling(@PathVariable("transId") final String transId,
            @RequestBody @Valid @NotNull final OfficerFilingDto dto, final BindingResult result,
            final HttpServletRequest request) {
        throw new NotImplementedException();
    }

    /**
     * Retrieve Officer Filing submission for review by the user before completing the submission.
     *
     * @param transId        the Transaction ID
     * @param filingResource the Officer Filing ID
     * @throws NotImplementedException implementing classes must perform work
     */
    @GetMapping
    default ResponseEntity<OfficerFilingDto> getFilingForReview(
            @PathVariable("transId") String transId,
            @PathVariable("filingResource") String filingResource) {
        throw new NotImplementedException();
    }
}
