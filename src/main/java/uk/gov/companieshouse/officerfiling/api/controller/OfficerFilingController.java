package uk.gov.companieshouse.officerfiling.api.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;

public interface OfficerFilingController {
    @PostMapping
    default ResponseEntity<Object> createFiling(@PathVariable("transId") final String transId,
            @RequestBody @Valid @NotNull final OfficerFilingDto dto, final BindingResult result,
            final HttpServletRequest request) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping
    default ResponseEntity<OfficerFilingDto> getFilingForReview(@PathVariable("transId") String transId,
                                                             @PathVariable("filingResource") String filingResource) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
