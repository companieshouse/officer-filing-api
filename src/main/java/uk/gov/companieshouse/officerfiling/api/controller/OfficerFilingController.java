package uk.gov.companieshouse.officerfiling.api.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;

public interface OfficerFilingController {
    @PostMapping
    default ResponseEntity<Object> createFiling(@PathVariable("transId") String transId,
            @RequestBody @Valid @NotNull OfficerFilingDto dto, BindingResult result) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}