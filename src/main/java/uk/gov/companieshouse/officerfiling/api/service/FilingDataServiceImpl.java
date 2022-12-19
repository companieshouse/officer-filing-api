package uk.gov.companieshouse.officerfiling.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.entity.Date3Tuple;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;
import uk.gov.companieshouse.officerfiling.api.utils.MapHelper;

import java.time.Instant;

/**
 * Produces Filing Data format for consumption as JSON by filing-resource-handler external service.
 */
@Service
public class FilingDataServiceImpl implements FilingDataService {

    private final OfficerFilingService officerFilingService;
    private final OfficerFilingMapper filingMapper;
    private final Logger logger;
    private final TransactionService transactionService;
    private final CompanyAppointmentService companyAppointmentService;

    public FilingDataServiceImpl(OfficerFilingService officerFilingService,
            OfficerFilingMapper filingMapper, Logger logger, TransactionService transactionService,
                                 CompanyAppointmentService companyAppointmentService) {
        this.officerFilingService = officerFilingService;
        this.filingMapper = filingMapper;
        this.logger = logger;
        this.transactionService = transactionService;
        this.companyAppointmentService = companyAppointmentService;
    }

    /**
     * Generate FilingApi data enriched by names and date of birth from company-appointments API.
     *
     * @param transactionId the Transaction ID
     * @param filingId      the Officer Filing ID
     * @return the FilingApi data for JSON response
     */
    @Override
    public FilingApi generateOfficerFiling(String transactionId, String filingId, String ericPassThroughHeader) {
        var filing = new FilingApi();
        filing.setKind("officer-filing#termination"); // TODO: handling other kinds to come later

        setFilingApiData(filing, transactionId, filingId, ericPassThroughHeader);
        return filing;
    }

    private void setFilingApiData(FilingApi filing, String transactionId, String filingId,
                                  String ericPassThroughHeader) {
        var officerFilingOpt = officerFilingService.get(filingId, transactionId);
        var officerFiling = officerFilingOpt.orElseThrow(() -> new ResourceNotFoundException(
                String.format("Officer not found when generating filing for %s", filingId)));

        final Transaction transaction = transactionService.getTransaction(transactionId, ericPassThroughHeader);
        String companyNumber = transaction.getCompanyNumber();

        final AppointmentFullRecordAPI companyAppointment = companyAppointmentService.getCompanyAppointment(companyNumber,
                filingId, ericPassThroughHeader);

        OfficerFiling enhancedOfficerFiling = OfficerFiling.builder(officerFiling)
                .dateOfBirth(new Date3Tuple(companyAppointment.getDateOfBirth()))
                .name(companyAppointment.getName())
                .referenceEtag(companyAppointment.getEtag())
                .build();

        var filingData = filingMapper.mapFiling(enhancedOfficerFiling);
        var dataMap = MapHelper.convertObject(filingData);

        final var logMap = LogHelper.createLogMap(transactionId, filingId);

        logMap.put("Data to submit", dataMap);
        logger.debugContext(transactionId, filingId, logMap);

        filing.setData(dataMap);
    }

}
