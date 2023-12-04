package uk.gov.companieshouse.officerfiling.api.service;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
import uk.gov.companieshouse.officerfiling.api.model.filing.OfficerPreviousDetails;
import uk.gov.companieshouse.officerfiling.api.model.mapper.FilingAPIMapper;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;
import uk.gov.companieshouse.officerfiling.api.utils.MapHelper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Produces Filing Data format for consumption as JSON by filing-resource-handler external service.
 */
@Service
public class FilingDataServiceImpl implements FilingDataService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
    public static final String DIRECTOR_NAME = "director name";
    private final OfficerFilingService officerFilingService;
    private final FilingAPIMapper filingAPIMapper;
    private final Logger logger;
    private final TransactionService transactionService;
    private final CompanyAppointmentService companyAppointmentService;
    private static final String TM01_FILING_DESCRIPTION = "(TM01) Termination of appointment of a director. Terminating appointment of {director name} on {termination date}";
    private static final String AP01_FILING_DESCRIPTION = "(AP01) Appointment of a director. Appointment of {director name} on {appointment date}";
    private static final String CH01_FILING_DESCRIPTION = "(CH01) Update of a director. Update of {director name} on {update date}";
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
        final var filing = new FilingApi();
        final var officerFiling = officerFilingService.get(filingId, transactionId)
                .orElseThrow(() -> new IllegalStateException(String.format("Officer not found when generating filing for %s", filingId)));
        final var presentOfficerFilingData = officerFiling.getData();

        if (presentOfficerFilingData.getResignedOn() != null) {
            // Has a removal date so must be a TM01
            filing.setKind("officer-filing#termination");
            setTerminationFilingApiData(filing, transactionId, filingId, ericPassThroughHeader, officerFiling);
        } else if (presentOfficerFilingData.getReferenceEtag() == null) {
            // Has no Etag and has no removal date so it must be an AP01
            filing.setKind("officer-filing#appointment");
            setAppointmentFilingApiData(filing, transactionId, filingId, ericPassThroughHeader, officerFiling);
            // add same as booleans if null / or does not exist in appointment.
            if (presentOfficerFilingData.getIsServiceAddressSameAsRegisteredOfficeAddress() == null) {
                filing.getData().put("service_address_same_as_registered_office_address", false);
            }
            if (presentOfficerFilingData.getIsServiceAddressSameAsHomeAddress() == null) {
                filing.getData().put("service_address_same_as_home_address", false);
            }
        } else {
            // Must be a CH01
            filing.setKind("officer-filing#update");
            setUpdateFilingApiData(filing, transactionId, filingId, ericPassThroughHeader, officerFiling);
        }

        return filing;
    }

    private void setTerminationFilingApiData(FilingApi filing, String transactionId, String filingId, String ericPassThroughHeader, OfficerFiling officerFiling) {
        final var transaction = transactionService.getTransaction(transactionId, ericPassThroughHeader);
        String companyNumber = transaction.getCompanyNumber();
        String appointmentId = officerFiling.getData().getReferenceAppointmentId();
        final AppointmentFullRecordAPI companyAppointment = companyAppointmentService.getCompanyAppointment(transactionId, companyNumber,
                appointmentId, ericPassThroughHeader);
        String surname;
        var middleNames = "";
        var firstname = "";
        // if it is a corporate Director then we must pass the name field into the lastName field
        // as that is where chips expects the corporate director name to be
        if (companyAppointment.getOfficerRole().equalsIgnoreCase("corporate-director")) {
            surname = companyAppointment.getName();
        } else {
            surname = companyAppointment.getSurname();
            middleNames = companyAppointment.getOtherForenames();
            firstname = companyAppointment.getForename();
        }

        var dataBuilder = OfficerFilingData.builder(officerFiling.getData())
                .firstName(firstname)
                .middleNames(middleNames)
                .lastName(surname)
                .name(companyAppointment.getName())
                .corporateDirector(mapCorporateDirector(transaction, companyAppointment));

        // For non-corporate Directors
        if(companyAppointment.getDateOfBirth() != null){
            LocalDate date = LocalDate.of(companyAppointment.getDateOfBirth().getYear(), companyAppointment.getDateOfBirth().getMonth(), companyAppointment.getDateOfBirth().getDay());
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
        logger.debugContext(transactionId, "Created termination filing data for submission", new LogHelper.Builder(transaction)
                .withFilingId(filingId)
                .build());

        filing.setData(dataMap);
        setTm01DescriptionFields(filing, enhancedOfficerFiling.getData(),  companyAppointment);
    }

    private void setAppointmentFilingApiData(FilingApi filing, String transactionId, String filingId, String ericPassThroughHeader, OfficerFiling officerFiling) {
        final var transaction = transactionService.getTransaction(transactionId, ericPassThroughHeader);

        var enhancedOfficerFiling = OfficerFiling.builder(officerFiling)
                .createdAt(officerFiling.getCreatedAt())
                .updatedAt(officerFiling.getUpdatedAt())
                .data(OfficerFilingData.builder(officerFiling.getData())
                        .build())
                .build();

        var filingData = filingAPIMapper.map(enhancedOfficerFiling);
        var dataMap = MapHelper.convertObject(filingData, PropertyNamingStrategies.SNAKE_CASE);
        logger.debugContext(transactionId, "Created appointment filing data for submission", new LogHelper.Builder(transaction)
                .withFilingId(filingId)
                .build());

        filing.setData(dataMap);
        setAp01DescriptionFields(filing, enhancedOfficerFiling.getData());
    }

    private void setUpdateFilingApiData(FilingApi filing, String transactionId, String filingId, String ericPassThroughHeader, OfficerFiling officerFiling) {
        final var transaction = transactionService.getTransaction(transactionId, ericPassThroughHeader);
        final var enhancedOfficerFiling = OfficerFiling.builder(officerFiling)
                .createdAt(officerFiling.getCreatedAt())
                .updatedAt(officerFiling.getUpdatedAt())
                // Temporarily hard-coded to test xml into CHIPS. Will be replaced when CH01 filing data is complete.
                .data(OfficerFilingData.builder()
                        .title("Ms")
                        .firstName("Robinson")
                        .middleNames("Ann")
                        .lastName("Wilder")
                        .appointedOn(Instant.parse("2023-10-01T18:35:24.00Z"))
                        .directorsDetailsChangedDate(Instant.parse("2023-10-01T18:35:24.00Z"))
                        .officerPreviousDetails(OfficerPreviousDetails.builder()
                                .title("Ms")
                                .firstName("Crossland")
                                .middleNames("Ann")
                                .lastName("Wilder")
                                .dateOfBirth("1961-06-13T00:00:00.00Z") // Jackson doesn't map this properly if it is an instant
                                .build())
                        .build())
                .build();

        var filingData = filingAPIMapper.map(enhancedOfficerFiling);
        var dataMap = MapHelper.convertObject(filingData, PropertyNamingStrategies.SNAKE_CASE);
        logger.debugContext(transactionId, "Created update filing data for submission", new LogHelper.Builder(transaction)
                .withFilingId(filingId)
                .build());

        filing.setData(dataMap);
        setCh01DescriptionFields(filing, enhancedOfficerFiling.getData());
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

    private void setTm01DescriptionFields(FilingApi filing, OfficerFilingData officerFilingData, AppointmentFullRecordAPI companyAppointment) {
        final String formattedTerminationDate = LocalDate.ofInstant(officerFilingData.getResignedOn(), ZoneOffset.UTC).format(formatter);
        filing.setDescriptionIdentifier(TM01_FILING_DESCRIPTION);
        var surname = "";
        var officerFilingName = "";
        if(companyAppointment.getSurname()!= null) {
            surname = companyAppointment.getSurname().toUpperCase();
            officerFilingName = companyAppointment.getForename() + " " + surname;
        } else {
            // is a corporate director
            officerFilingName = companyAppointment.getName();
        }
        filing.setDescription(TM01_FILING_DESCRIPTION.replace("{" + DIRECTOR_NAME + "}", officerFilingName)
                .replace("{termination date}", formattedTerminationDate));
        Map<String, String> values = new HashMap<>();
        values.put("termination date", formattedTerminationDate);
        values.put(DIRECTOR_NAME, officerFilingName);
        filing.setDescriptionValues(values);
    }

    private void setAp01DescriptionFields(FilingApi filing, OfficerFilingData officerFilingData) {
        final String formattedAppointmentDate = LocalDate.ofInstant(officerFilingData.getAppointedOn(), ZoneOffset.UTC).format(formatter);
        final String officerFilingName = officerFilingData.getFirstName().toUpperCase() + " " + officerFilingData.getLastName().toUpperCase();
        filing.setDescriptionIdentifier(AP01_FILING_DESCRIPTION);
        filing.setDescription(AP01_FILING_DESCRIPTION.replace("{" + DIRECTOR_NAME + "}", officerFilingName)
                .replace("{appointment date}", formattedAppointmentDate));
        filing.setDescriptionValues(Map.of(
                "appointment date", formattedAppointmentDate,
                DIRECTOR_NAME, officerFilingName
        ));
    }

    private void setCh01DescriptionFields(FilingApi filing, OfficerFilingData officerFilingData) {
        final String formattedUpdateDate = LocalDate.ofInstant(officerFilingData.getDirectorsDetailsChangedDate(), ZoneOffset.UTC).format(formatter);
        final String officerFilingName = officerFilingData.getFirstName().toUpperCase() + " " + officerFilingData.getLastName().toUpperCase();
        filing.setDescriptionIdentifier(CH01_FILING_DESCRIPTION);
        filing.setDescription(CH01_FILING_DESCRIPTION.replace("{" + DIRECTOR_NAME + "}", officerFilingName)
                .replace("{update date}", formattedUpdateDate));
        filing.setDescriptionValues(Map.of(
                "update date", formattedUpdateDate,
                DIRECTOR_NAME, officerFilingName
        ));
    }
}
