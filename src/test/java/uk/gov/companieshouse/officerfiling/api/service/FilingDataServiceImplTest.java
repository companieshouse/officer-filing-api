package uk.gov.companieshouse.officerfiling.api.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.delta.officers.SensitiveDateOfBirthAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.entity.Date3Tuple;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
import uk.gov.companieshouse.officerfiling.api.model.filing.FilingData;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;

@ExtendWith(MockitoExtension.class)
class FilingDataServiceImplTest {

    private static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String TRANS_ID = "23445657412";
    private static final String REF_APPOINTMENT_ID = "12345";
    private static final String REF_ETAG = "6789";
    private static final String RESIGNED_ON_STR = "2022-10-05";
    private static final Instant RESIGNED_ON_INS = Instant.parse("2022-10-05T00:00:00Z");
    public static final String FIRSTNAME = "JOE";
    public static final String LASTNAME = "BLOGGS";
    public static final String FULL_NAME = FIRSTNAME + " " + LASTNAME;
    public static final String DATE_OF_BIRTH_STR = "2000-10-20";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = null;
    public static final Date3Tuple DATE_OF_BIRTH_TUPLE = new Date3Tuple(20, 10, 2000);
    private static final LocalDate DUMMY_DATE = LocalDate.of(2023, 3, 16);
    @Mock
    private OfficerFilingService officerFilingService;
    @Mock
    private OfficerFilingMapper officerFilingMapper;
    @Mock
    private Logger logger;
    @Mock
    private TransactionService transactionService;
    @Mock
    private CompanyAppointmentService companyAppointmentService;
    @Mock
    private Transaction transaction;
    @Mock
    private AppointmentFullRecordAPI companyAppointment;
    @Mock
    private Supplier<LocalDate> dateNowSupplier;

    private FilingDataServiceImpl testService;
    private Clock clock;

    @BeforeEach
    void setUp() {
        testService = new FilingDataServiceImpl(officerFilingService, officerFilingMapper, logger, transactionService,
                companyAppointmentService, dateNowSupplier);
        ReflectionTestUtils.setField(testService, "filingDescription",
            "(TM01) Termination of appointment of director. Terminating appointment of {director name} on {termination date}");
    }

    @Test
    void generateOfficerFilingWhenFound() {
        final var filingData = new FilingData(FIRSTNAME, LASTNAME, DATE_OF_BIRTH_STR, RESIGNED_ON_STR, true);
        var offData = new OfficerFilingData(
                null,
                null,
                null,
                null,
                DATE_OF_BIRTH_TUPLE,
                Collections.singletonList(null),
                null,
                FIRSTNAME,
                LASTNAME,
                null,
                null,
                null,
                null,
                REF_APPOINTMENT_ID,
                null,
                RESIGNED_ON_INS,
                null,
                null,
                null,
                null
        );
        final var now = clock.instant();
        final var officerFiling = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();

        SensitiveDateOfBirthAPI dateOfBirthAPI = new SensitiveDateOfBirthAPI();
        dateOfBirthAPI.setDay(20);
        dateOfBirthAPI.setMonth(10);
        dateOfBirthAPI.setYear(2000);

        when(companyAppointment.getDateOfBirth()).thenReturn(dateOfBirthAPI);
        when(companyAppointment.getOfficerRole()).thenReturn("corporate-director");
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(officerFiling));
        when(officerFilingMapper.mapFiling(officerFiling)).thenReturn(filingData);
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER ))
                .thenReturn(companyAppointment);
        when(dateNowSupplier.get()).thenReturn(DUMMY_DATE);

        final var filingApi = testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        final Map<String, Object> expectedMap =
                Map.of("first_name", FIRSTNAME, "last_name", LASTNAME,
                        "date_of_birth", DATE_OF_BIRTH_STR,
                        "resigned_on", RESIGNED_ON_STR,
                        "is_corporate_director", true);

        assertThat(filingApi.getData(), is(equalTo(expectedMap)));
        assertThat(filingApi.getKind(), is("officer-filing#termination"));
        assertThat(filingApi.getDescription(), is("(TM01) Termination of appointment of director. Terminating appointment of "
            + FIRSTNAME + " " + LASTNAME.toUpperCase()  + " on 16 March 2023"));
    }

    @Test
    void generateOfficerFilingWhenNotFound() {
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.empty());

        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER));

        assertThat(exception.getMessage(),
                is("Officer not found when generating filing for " + FILING_ID));
    }

    @ParameterizedTest
    @CsvSource({
            "corporate-director,true",
            "corporate-nominee-director,true",
            "director,false",
            "nominee-director,false",
            "invalid-role,false",
    })
    void mapCorporateDirector(String officerRole, boolean isCorporateDirector) {
        when(companyAppointment.getOfficerRole()).thenReturn(officerRole);
        final Boolean corporateDirector = testService.mapCorporateDirector(transaction, companyAppointment);
        assertThat(corporateDirector, is(isCorporateDirector));
    }
}