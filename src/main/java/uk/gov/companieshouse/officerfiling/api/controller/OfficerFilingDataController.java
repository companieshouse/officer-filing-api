package uk.gov.companieshouse.officerfiling.api.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;

public interface OfficerFilingDataController {

    @GetMapping
    default List<OfficerFiling> getFilingsData(@PathVariable("transId") String transId,
                                               @PathVariable("filingResource") String filingResource) {
        throw new NotImplementedException();
    }
}
