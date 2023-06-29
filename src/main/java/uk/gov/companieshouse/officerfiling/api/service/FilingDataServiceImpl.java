package uk.gov.companieshouse.officerfiling.api.service;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.entity.Date3Tuple;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;
import uk.gov.companieshouse.officerfiling.api.utils.MapHelper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * Produces Filing Data format for consumption as JSON by filing-resource-handler external service.
 */
@Service
public class FilingDataServiceImpl implements FilingDataService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private final OfficerFilingService officerFilingService;
    private final OfficerFilingMapper filingMapper;
    private final Logger logger;
    private final TransactionService transactionService;
    private final CompanyAppointmentService companyAppointmentService;
    @Value("${OFFICER_FILING_DESCRIPTION:"
        + "(TM01) Termination of appointment of director. Terminating appointment of {director name} on {termination date}}")
    private String filingDescription;
    private final Supplier<LocalDate> dateNowSupplier;

    public FilingDataServiceImpl(OfficerFilingService officerFilingService,
            OfficerFilingMapper filingMapper, Logger logger, TransactionService transactionService,
                                 CompanyAppointmentService companyAppointmentService,
        Supplier<LocalDate> dateNowSupplier) {
        this.officerFilingService = officerFilingService;
        this.filingMapper = filingMapper;
        this.logger = logger;
        this.transactionService = transactionService;
        this.companyAppointmentService = companyAppointmentService;
        this.dateNowSupplier = dateNowSupplier;
    }

    /**
     * Generate FilingApi data enriched by names and date of birth from company-appointments API.
     *
     * @param transactionId the Transaction ID
     * @param filingId      the Officer Filing ID
     * @param ericPassThroughHeader includes authorisation for company appointment fetch
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
        OfficerFiling officerFiling = officerFilingOpt.orElseThrow(() -> new ResourceNotFoundException(
                String.format("Officer not found when generating filing for %s", filingId)));

        final var transaction = transactionService.getTransaction(transactionId, ericPassThroughHeader);
        String companyNumber = transaction.getCompanyNumber();
        String appointmentId = officerFiling.getOfficerFilingData().getReferenceAppointmentId();

        final AppointmentFullRecordAPI companyAppointment = companyAppointmentService.getCompanyAppointment(transactionId, companyNumber,
                appointmentId, ericPassThroughHeader);

        var enhancedOfficerFiling = OfficerFiling.builder(officerFiling)
                .officerFilingData(new OfficerFilingData(null,null, null,null, new Date3Tuple(companyAppointment.getDateOfBirth()), null, companyAppointment.getName(), null, null, null, null, null, null, null, null, null, null, null, null, mapCorporateDirector(transaction, companyAppointment)))
                .build();

        var filingData = filingMapper.mapFiling(enhancedOfficerFiling);
        var dataMap = MapHelper.convertObject(filingData, PropertyNamingStrategies.SNAKE_CASE);

        logger.debugContext(transactionId, "Created filing data for submission", new LogHelper.Builder(transaction)
                .withFilingId(filingId)
                .build());

        filing.setData(dataMap);
        setDescriptionFields(filing, companyAppointment);
    }

    /**
     * Map officer role to corporate_director boolean.
     */
    public Boolean mapCorporateDirector(Transaction transaction, AppointmentFullRecordAPI companyAppointment) {
        if (List.of("corporate-director", "corporate-nominee-director").contains(companyAppointment.getOfficerRole())) {
            return true;
        }
        if (List.of("director", "nominee-director").contains(companyAppointment.getOfficerRole())) {
            return false;
        }

        logger.infoContext(transaction.getId(), "Unrecognised Officer Role: " + companyAppointment.getOfficerRole(),
                new LogHelper.Builder(transaction).build());
        return false;
    }

    private void setDescriptionFields(FilingApi filing, AppointmentFullRecordAPI companyAppointment) {
        String formattedTerminationDate = dateNowSupplier.get().format(formatter);
        filing.setDescriptionIdentifier(filingDescription);
        var officerFilingName = companyAppointment.getForename() + " " + companyAppointment.getSurname().toUpperCase();
        filing.setDescription(filingDescription.replace("{director name}", officerFilingName)
            .replace("{termination date}", formattedTerminationDate));
        Map<String, String> values = new HashMap<>();
        values.put("termination date", formattedTerminationDate);
        values.put("director name", officerFilingName);
        filing.setDescriptionValues(values);
    }

}