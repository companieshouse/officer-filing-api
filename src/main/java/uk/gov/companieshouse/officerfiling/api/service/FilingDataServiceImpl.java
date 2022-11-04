package uk.gov.companieshouse.officerfiling.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.entity.Date3Tuple;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;
import uk.gov.companieshouse.officerfiling.api.utils.MapHelper;

/**
 * Produces Filing Data format for consumption as JSON by filing-resource-handler external service.
 */
@Service
public class FilingDataServiceImpl implements FilingDataService {

    private final OfficerFilingService officerFilingService;
    private final OfficerFilingMapper filingMapper;
    private final Logger logger;

    public FilingDataServiceImpl(OfficerFilingService officerFilingService,
            OfficerFilingMapper filingMapper, Logger logger) {
        this.officerFilingService = officerFilingService;
        this.filingMapper = filingMapper;
        this.logger = logger;
    }

    /**
     * Generate FilingApi data enriched by names and date of birth from company-appointments API.
     *
     * @param transactionId the Transaction ID
     * @param filingId      the Officer Filing ID
     * @return the FilingApi data for JSON response
     */
    @Override
    public FilingApi generateOfficerFiling(String transactionId, String filingId) {
        var filing = new FilingApi();
        filing.setKind("officer-filing#termination"); // TODO: handling other kinds to come later

        setFilingApiData(filing, transactionId, filingId);
        return filing;
    }

    private void setFilingApiData(FilingApi filing, String transactionId, String filingId) {
        var officerFilingOpt = officerFilingService.get(filingId, transactionId);
        var officerFiling = officerFilingOpt.orElseThrow(() -> new ResourceNotFoundException(
                String.format("Officer not found when generating filing for %s", filingId)));
        // TODO this is dummy data until we get the details from company-appointments API
        var enhancedOfficerFiling = OfficerFiling.builder(officerFiling)
                .dateOfBirth(new Date3Tuple(20, 10, 2000))
                .firstName("JOE")
                .lastName("BLOGGS")
                .build();
        var filingData = filingMapper.mapFiling(enhancedOfficerFiling);
        var dataMap = MapHelper.convertObject(filingData);

        final var logMap = LogHelper.createLogMap(transactionId, filingId);

        logMap.put("Data to submit", dataMap);
        logger.debugContext(transactionId, filingId, logMap);

        filing.setData(dataMap);
    }

}