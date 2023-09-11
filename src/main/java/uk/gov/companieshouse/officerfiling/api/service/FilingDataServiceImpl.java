package uk.gov.companieshouse.officerfiling.api.service;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
import uk.gov.companieshouse.officerfiling.api.model.mapper.FilingAPIMapper;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;
import uk.gov.companieshouse.officerfiling.api.utils.MapHelper;

/**
 * Produces Filing Data format for consumption as JSON by filing-resource-handler external service.
 */
@Service
public class FilingDataServiceImpl implements FilingDataService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private final OfficerFilingService officerFilingService;
    private final FilingAPIMapper filingAPIMapper;
    private final Logger logger;
    private final TransactionService transactionService;
    private final CompanyAppointmentService companyAppointmentService;
    @Value("${OFFICER_FILING_DESCRIPTION:"
            + "(TM01) Termination of appointment of director. Terminating appointment of {director name} on {termination date}}")
    private String filingDescription;
    private final Supplier<LocalDate> dateNowSupplier;

    public FilingDataServiceImpl(OfficerFilingService officerFilingService,
            FilingAPIMapper filingAPIMapper, Logger logger, TransactionService transactionService,
            CompanyAppointmentService companyAppointmentService,
            Supplier<LocalDate> dateNowSupplier) {
        this.officerFilingService = officerFilingService;
        this.filingAPIMapper = filingAPIMapper;
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
        Optional<OfficerFiling> officerFiling = officerFilingService.get(filingId, transactionId);
        if(officerFiling.isPresent()) {
            OfficerFilingData presentOfficerFilingData = officerFiling.get().getData();
            if (presentOfficerFilingData.getResignedOn() != null) {
                //has a removal date so must be a TM01
                filing.setKind("officer-filing#termination");
                setTerminationFilingApiData(filing, transactionId, filingId, ericPassThroughHeader);
            } else if (presentOfficerFilingData.getReferenceEtag() == null) {
                //has no Etag (and has no removal date) so it must be an AP01
                filing.setKind("officer-filing#appointment");
                setAppointmentFilingApiData(transactionId, filingId, ericPassThroughHeader);
            } else {
                throw new NotImplementedException("Kind cannot be calculated using given data for transaction " + transactionId );
            }
        } else {
            filing.setKind("officer-filing#termination");
            setTerminationFilingApiData(filing, transactionId, filingId, ericPassThroughHeader);
        }


        return filing;
    }

    private void setTerminationFilingApiData(FilingApi filing, String transactionId, String filingId,
            String ericPassThroughHeader) {
        var officerFilingOpt = officerFilingService.get(filingId, transactionId);
        var officerFiling = officerFilingOpt.orElseThrow(() -> new NotImplementedException(
                String.format("Officer not found when generating filing for %s", filingId)));
        final var transaction = transactionService.getTransaction(transactionId, ericPassThroughHeader);
        String companyNumber = transaction.getCompanyNumber();
        String appointmentId = officerFiling.getData().getReferenceAppointmentId();
        final AppointmentFullRecordAPI companyAppointment = companyAppointmentService.getCompanyAppointment(transactionId, companyNumber,
                appointmentId, ericPassThroughHeader);
        String surname;
        var firstname = "";
        // if it is a corporate Director then we must pass the name field into the lastName field
        // as that is where chips expects the corporate director name to be
        if (companyAppointment.getOfficerRole().equalsIgnoreCase("corporate-director")) {
            surname = companyAppointment.getName();
            firstname = "";
        } else {
            surname = companyAppointment.getSurname();
            firstname = companyAppointment.getForename();
        }

        var dataBuilder = OfficerFilingData.builder(officerFiling.getData())
                .firstName(firstname)
                .lastName(surname)
                .name(companyAppointment.getName())
                .corporateDirector(mapCorporateDirector(transaction, companyAppointment));

        // For non-corporate Directors
        if(companyAppointment.getDateOfBirth() != null){
            LocalDate date =  LocalDate.parse(companyAppointment.getDateOfBirth().getYear() + "-" + companyAppointment.getDateOfBirth().getMonth() + "-" + companyAppointment.getDateOfBirth().getDay());
            Instant dobInstant = date.atStartOfDay().toInstant(ZoneOffset.UTC);
            dataBuilder = dataBuilder
                    .dateOfBirth(dobInstant);
        }

        var enhancedOfficerFilingBuilder = OfficerFiling.builder(officerFiling)
                .data(dataBuilder.build())
                .createdAt(officerFiling.getCreatedAt())
                .updatedAt(officerFiling.getUpdatedAt());

        var enhancedOfficerFiling = enhancedOfficerFilingBuilder.build();
        var filingData = filingAPIMapper.map(enhancedOfficerFiling);
        var dataMap = MapHelper.convertObject(filingData, PropertyNamingStrategies.SNAKE_CASE);
        logger.debugContext(transactionId, "Created filing data for submission", new LogHelper.Builder(transaction)
                .withFilingId(filingId)
                .build());

        filing.setData(dataMap);
        setDescriptionFields(filing, companyAppointment);
    }

    private void setAppointmentFilingApiData(String transactionId, String filingId,
            String ericPassThroughHeader) {
        //TODO - just creating a blank one of these for the moment,  need to add filing data into here as we add it in.
        final var transaction = transactionService.getTransaction(transactionId, ericPassThroughHeader);

        logger.debugContext(transactionId, "Created filing data for submission", new LogHelper.Builder(transaction)
                .withFilingId(filingId)
                .build());
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
        var surname = "";
        var officerFilingName = "";
        if(companyAppointment.getSurname()!= null) {
            surname = companyAppointment.getSurname().toUpperCase();
            officerFilingName = companyAppointment.getForename() + " " + surname;
        } else {
            // is a corporate director
            officerFilingName = companyAppointment.getName();
        }
        filing.setDescription(filingDescription.replace("{director name}", officerFilingName)
                .replace("{termination date}", formattedTerminationDate));
        Map<String, String> values = new HashMap<>();
        values.put("termination date", formattedTerminationDate);
        values.put("director name", officerFilingName);
        filing.setDescriptionValues(values);
    }
}