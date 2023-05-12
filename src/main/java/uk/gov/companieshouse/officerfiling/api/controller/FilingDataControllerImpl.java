package uk.gov.companieshouse.officerfiling.api.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.FeatureNotEnabledException;
import uk.gov.companieshouse.officerfiling.api.service.FilingDataService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@RestController
@RequestMapping("/private/transactions/{transactionId}/officers")
public class FilingDataControllerImpl implements FilingDataController {
    private final FilingDataService filingDataService;
    private final Logger logger;
    @Value("${FEATURE_FLAG_ENABLE_TM01:true}")
    private boolean isTm01Enabled;

    public FilingDataControllerImpl(final FilingDataService filingDataService,
            final Logger logger) {
        this.filingDataService = filingDataService;
        this.logger = logger;
    }

    /**
     * Controller endpoint: retrieve Filing Data. Returns a list containing a single resource;
     * Future capability to return multiple resources if a Transaction contains multiple Officer
     * Filings.
     *
     * @param transId        the Transaction ID
     * @param filingResourceId the Filing Resource ID
     * @param request        the servlet request
     * @return List of FilingApi resources
     */
    @Override
    @GetMapping(value = "/{filingResourceId}/filings", produces = {"application/json"})
    public List<FilingApi> getFilingsData(@PathVariable("transactionId") final String transId,
            @PathVariable("filingResourceId") final String filingResourceId,
            final HttpServletRequest request) {

        if(!isTm01Enabled){
            throw new FeatureNotEnabledException();
        }

        logger.debugContext(transId, "Getting filing data", new LogHelper.Builder(transId)
                        .withFilingId(filingResourceId)
                        .withRequest(request)
                        .build());

        final var passthroughHeader = request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());
        final var filingApi = filingDataService.generateOfficerFiling(transId, filingResourceId, passthroughHeader);

        logger.infoContext(transId, "Generated officer filing data", new LogHelper.Builder(transId)
                .withFilingId(filingResourceId)
                .withRequest(request)
                .build());

        return List.of(filingApi);
    }
}
