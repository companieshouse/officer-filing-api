package uk.gov.companieshouse.officerfiling.api.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.entity.Date3Tuple;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.utils.MapHelper;

@Service
public class FilingDataServiceImpl implements FilingDataService {

    private final OfficerFilingService officerFilingService;
    private final OfficerFilingMapper filingMapper;
    private final Logger logger;

    public FilingDataServiceImpl(OfficerFilingService officerFilingService, OfficerFilingMapper filingMapper,
                                 Logger logger) {
        this.officerFilingService = officerFilingService;
        this.filingMapper = filingMapper;
        this.logger = logger;
    }

    @Override
    public FilingApi generateOfficerFiling(String transactionId, String filingId) {
        var filing = new FilingApi();
        filing.setKind("officer-filing#termination");

        setFilingApiData(filing, transactionId, filingId);
        return filing;
    }

    private void setFilingApiData(FilingApi filing, String transactionId, String filingId) {
        var officerFilingOpt = officerFilingService.get(filingId, transactionId);
        var officerFiling = officerFilingOpt.orElseThrow(() -> new ResourceNotFoundException(
                String.format("Officer not found when generating filing for %s", filingId)));
        // TODO this is dummy data until we get the details from company-appointments API
        var enhancedOfficerFiling = officerFiling.builder(officerFiling)
                .dateOfBirth(new Date3Tuple(20, 10, 2000))
                .firstName("JOE")
                .lastName("BLOGGS")
                .build();
        var filingData = filingMapper.mapFiling(enhancedOfficerFiling);
        var dataMap = MapHelper.convertObject(filingData);

        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("Data to submit", dataMap);
        logMap.put("transaction_id", transactionId);
        logger.debugContext(transactionId, filingId, logMap);

        filing.setData(dataMap);
    }

}
