package uk.gov.companieshouse.officerfiling.api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
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
                                              @PathVariable("filingResourceId") final String filingResource,
                                              final HttpServletRequest request) {

        final Map<String, Object> logMap = new HashMap<>();

        logMap.put("filingId", filingResource);
        logger.debugRequest(request, "GET /private/transactions/{transId}/officers{filingId}/filings", logMap);

        var officerFilings = officerFilingService.getFilingsData(filingResource);

        if (officerFilings.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        logMap.put("officerFilings", officerFilings);
        logger.infoContext(transId, "Officer filing data", logMap);

        return officerFilings;
    }
}
