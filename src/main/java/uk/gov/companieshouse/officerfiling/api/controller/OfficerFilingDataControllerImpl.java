package uk.gov.companieshouse.officerfiling.api.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;

@RestController
@RequestMapping("/private/transactions/{transId}/officers")
public class OfficerFilingDataControllerImpl implements OfficerFilingDataController {
    public static final String VALIDATION_STATUS = "validation_status";
    private final OfficerFilingService officerFilingService;
    private final Logger logger;

    public OfficerFilingDataControllerImpl(final OfficerFilingService officerFilingService, final Logger logger) {
        this.officerFilingService = officerFilingService;
        this.logger = logger;
    }

    @Override
    @GetMapping(value = "/{filingResourceId}/filings", produces = {"application/json"})
    public List<OfficerFiling> getFilingsData(@PathVariable("transId") final String transId,
                                             @PathVariable("filingResourceId") final String filingResource) {

        var officerFilings = officerFilingService.getFilingsData(filingResource);

        if (officerFilings.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        return officerFilings;
    }
}
