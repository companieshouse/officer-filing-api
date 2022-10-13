package uk.gov.companieshouse.officerfiling.api.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;

public interface OfficerFilingDataController {

    @GetMapping
    default List<FilingApi> getFilingsData(@PathVariable("transId") String transId,
                                           @PathVariable("filingResource") String filingResource,
                                           HttpServletRequest request) {
        throw new NotImplementedException();
    }
}
