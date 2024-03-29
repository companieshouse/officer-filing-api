package uk.gov.companieshouse.officerfiling.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.delta.officers.SensitiveDateOfBirthAPI;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.entity.Address;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
import uk.gov.companieshouse.officerfiling.api.model.filing.FilingData;
import uk.gov.companieshouse.officerfiling.api.model.filing.OfficerPreviousDetails;
import uk.gov.companieshouse.officerfiling.api.model.mapper.FilingAPIMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilingDataServiceImplTest {

    private static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String TRANS_ID = "23445657412";
    private static final String REF_APPOINTMENT_ID = "12345";
    private static final String REF_ETAG = "6789";
    private static final String RESIGNED_ON_STR = "2022-10-05";
    private static final Instant RESIGNED_ON_INS = Instant.parse("2022-10-05T00:00:00Z");
    public static final String FIRSTNAME = "JOE";
    public static final String MIDDLENAMES = "PETER MARTIN";
    public static final String LASTNAME = "BLOGGS";
    public static final String NATIONALITY_1 = "nationality1";
    public static final String NATIONALITY_2 = "nationality2";
    public static final String NATIONALITY_3 = "nationality3";
    public static final String COMPANY_NAME = "Company Name";
    public static final String FULL_NAME = FIRSTNAME + " " + LASTNAME;
    public static final String DATE_OF_BIRTH_STR = "2000-10-20";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = null;
    public static final String OCCUPATION = "TEST ANALYST";

    private static final Instant DATE_OF_BIRTH_INS = Instant.parse("2000-10-20T00:00:00Z");
    private static final LocalDate DUMMY_DATE = LocalDate.of(2023, 3, 16);
    public static final Instant DIRECTOR_DETAILS_CHANGED_DATE = Instant.parse("2023-10-01T18:35:24Z");
    public static final Address SERVICE_ADDRESS = Address.builder()
            .premises("11")
            .addressLine1("One Street")
            .addressLine2("Two Lane")
            .region("Region")
            .locality("locality")
            .country("England")
            .postalCode("TE1 3ST")
            .build();
    public static final Address RESIDENTIAL_ADDRESS = Address.builder()
            .premises("12")
            .addressLine1("Two Street")
            .addressLine2("Two Lane")
            .region("Region")
            .locality("locality")
            .country("Wales")
            .postalCode("TE2 4ST")
            .build();
    @Mock
    private OfficerFilingService officerFilingService;
    @Mock
    private FilingAPIMapper filingAPIMapper;
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
    @Mock
    private Clock clock;

    @Captor
    private ArgumentCaptor<OfficerFiling> officerFilingCaptor;

    @BeforeEach
    void setUp() {
        testService = new FilingDataServiceImpl(officerFilingService, filingAPIMapper, logger, transactionService,
                companyAppointmentService);
    }

    @Test
    void generateTerminationOfficerFilingWhenFound() {
        final var filingData = new FilingData(FIRSTNAME, MIDDLENAMES, LASTNAME, DATE_OF_BIRTH_STR, RESIGNED_ON_STR, false);
        var offData = OfficerFilingData.builder()
                .dateOfBirth(DATE_OF_BIRTH_INS)
                .firstName(FIRSTNAME)
                .middleNames((MIDDLENAMES))
                .lastName(LASTNAME)
                .referenceAppointmentId(REF_APPOINTMENT_ID)
                .resignedOn(RESIGNED_ON_INS)
                .corporateDirector(false)
                .build();
        final var now = clock.instant();
        final var officerFiling = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();

        SensitiveDateOfBirthAPI dateOfBirthAPI = new SensitiveDateOfBirthAPI();
        dateOfBirthAPI.setDay(20);
        dateOfBirthAPI.setMonth(10);
        dateOfBirthAPI.setYear(2000);

        when(companyAppointment.getDateOfBirth()).thenReturn(dateOfBirthAPI);
        when(companyAppointment.getOfficerRole()).thenReturn("director");
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(officerFiling));
        when(filingAPIMapper.map(officerFiling)).thenReturn(filingData);
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        final var filingApi = testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        final Map<String, Object> expectedMap =
                Map.of("first_name", FIRSTNAME, "middle_names", MIDDLENAMES, "last_name", LASTNAME,
                        "date_of_birth", DATE_OF_BIRTH_STR,
                        "resigned_on", RESIGNED_ON_STR,
                        "is_corporate_director", false);

        assertThat(filingApi.getData(), is(equalTo(expectedMap)));
        assertThat(filingApi.getKind(), is("officer-filing#termination"));
        assertThat(filingApi.getDescription(), is("(TM01) Termination of appointment of a director. Terminating appointment of "
                + FIRSTNAME + " " + MIDDLENAMES + " " + LASTNAME.toUpperCase() + " on 5 October 2022"));
    }

    @Test
    void generateCorporateOfficerFilingWhenFound() {
        final var filingData = new FilingData(null, null, COMPANY_NAME, null, RESIGNED_ON_STR, true);
        final var data = OfficerFilingData.builder()
                .referenceAppointmentId(REF_APPOINTMENT_ID)
                .name(COMPANY_NAME)
                .firstName("")
                .middleNames("")
                .lastName(COMPANY_NAME)
                .resignedOn(RESIGNED_ON_INS)
                .corporateDirector(true)
                .build();
        final var officerFiling = OfficerFiling.builder()
                .data(data)
                .build();

        when(companyAppointment.getOfficerRole()).thenReturn("corporate-director");
        when(companyAppointment.getSurname()).thenReturn(null);
        when(companyAppointment.getName()).thenReturn(COMPANY_NAME);
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(officerFiling));
        when(filingAPIMapper.map(officerFiling)).thenReturn(filingData);
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        final var filingApi = testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        final Map<String, Object> expectedMap =
                Map.of("last_name", COMPANY_NAME,
                        "resigned_on", RESIGNED_ON_STR,
                        "is_corporate_director", true);

        assertThat(filingApi.getData(), is(equalTo(expectedMap)));
        assertThat(filingApi.getKind(), is("officer-filing#termination"));
        assertThat(filingApi.getDescription(), is("(TM01) Termination of appointment of a director. Terminating appointment of "
                + COMPANY_NAME.toUpperCase() + " on 5 October 2022"));
    }

    @Test
    void generateOfficerFilingWhenNotFound() {
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.empty());

        final var exception = assertThrows(IllegalStateException.class,
                () -> testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER));

        assertThat(exception.getMessage(),
                is("Officer not found when generating filing for " + FILING_ID));
    }

    @Test
    void generateAppointmentOfficerFilingWhenFound() {
        final var filingData = new FilingData("Major", FIRSTNAME, MIDDLENAMES, LASTNAME, "former names", DATE_OF_BIRTH_STR, RESIGNED_ON_STR,
                null, "nationality1", "nationality2", "nationality3", "occupation",
                Address.builder().premises("11").addressLine1("One Street").country("England").postalCode("TE1 3ST").build(), false,
                Address.builder().premises("12").addressLine1("Two Street").country("Wales").postalCode("TE2 4ST").build(), false,
                false, true, false, null, "2023-01-01", "Wales");
        var offData = OfficerFilingData.builder()
                .firstName(FIRSTNAME)
                .middleNames(MIDDLENAMES)
                .lastName(LASTNAME)
                .dateOfBirth(DATE_OF_BIRTH_INS)
                .appointedOn(RESIGNED_ON_INS)
                .nationality1("nationality1")
                .nationality2("nationality2")
                .nationality3("nationality3")
                .occupation("occupation")
                .serviceAddress(Address.builder().premises("11").addressLine1("One Street").country("England").postalCode("TE1 3ST").build())
                .isServiceAddressSameAsRegisteredOfficeAddress(false)
                .residentialAddress(Address.builder().premises("12").addressLine1("Two Street").country("Wales").postalCode("TE2 4ST").build())
                .isHomeAddressSameAsServiceAddress(false)
                .directorAppliedToProtectDetails(false)
                .consentToAct(true)
                .corporateDirector(false)
                .countryOfResidence(RESIDENTIAL_ADDRESS.getCountry())
                .build();
        final var now = clock.instant();
        final var officerFiling = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();

        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(officerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(filingAPIMapper.map(officerFiling)).thenReturn(filingData);

        final var filingApi = testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        final Map<String, Object> expectedMap =
                Map.ofEntries(
                        Map.entry("title", "Major"),
                        Map.entry("first_name", FIRSTNAME),
                        Map.entry("middle_names", MIDDLENAMES),
                        Map.entry("last_name", LASTNAME),
                        Map.entry("former_names", "former names"),
                        Map.entry("date_of_birth", DATE_OF_BIRTH_STR),
                        Map.entry("appointed_on", RESIGNED_ON_STR),
                        Map.entry("nationality1", "nationality1"),
                        Map.entry("nationality2", "nationality2"),
                        Map.entry("nationality3", "nationality3"),
                        Map.entry("occupation", "occupation"),
                        Map.entry("service_address", Map.of(
                                "premises", "11",
                                "address_line_1", "One Street",
                                "country", "England",
                                "postal_code", "TE1 3ST")),
                        Map.entry("service_address_same_as_registered_office_address", false),
                        Map.entry("residential_address", Map.of(
                                "premises", "12",
                                "address_line_1", "Two Street",
                                "country", "Wales",
                                "postal_code", "TE2 4ST")),
                        Map.entry("home_address_same_as_service_address", false),
                        Map.entry("director_applied_to_protect_details", false),
                        Map.entry("consent_to_act", true),
                        Map.entry("is_corporate_director", false),
                        Map.entry("country_of_residence", "Wales"),
                        Map.entry("directors_details_changed_date", "2023-01-01"));

        assertThat(filingApi.getData(), is(equalTo(expectedMap)));
        assertThat(filingApi.getKind(), is("officer-filing#appointment"));
        assertThat(filingApi.getDescription(), is(equalTo("(AP01) Appointment of a director. Appointment of JOE PETER MARTIN BLOGGS on 5 October 2022")));
    }

    @Test
    void generateAppointmentOfficerFilingWithDefaultFlagsWhenSameAsFlagsDoesNotExist() {
        final var filingData = new FilingData("Major", FIRSTNAME, MIDDLENAMES, LASTNAME, "former names", DATE_OF_BIRTH_STR, RESIGNED_ON_STR,
                null, "nationality1", "nationality2", "nationality3", "occupation",
                Address.builder().premises("11").addressLine1("One Street").country("England").postalCode("TE1 3ST").build(), null,
                Address.builder().premises("12").addressLine1("Two Street").country("Wales").postalCode("TE2 4ST").build(), null,
                false, true, false, null, "2023-01-01", "Wales");
        var offData = OfficerFilingData.builder()
                .firstName(FIRSTNAME)
                .middleNames(MIDDLENAMES)
                .lastName(LASTNAME)
                .dateOfBirth(DATE_OF_BIRTH_INS)
                .appointedOn(RESIGNED_ON_INS)
                .nationality1("nationality1")
                .nationality2("nationality2")
                .nationality3("nationality3")
                .occupation("occupation")
                .serviceAddress(Address.builder().premises("11").addressLine1("One Street").country("England").postalCode("TE1 3ST").build())
                .isServiceAddressSameAsRegisteredOfficeAddress(null)
                .residentialAddress(Address.builder().premises("12").addressLine1("Two Street").country("Wales").postalCode("TE2 4ST").build())
                .isHomeAddressSameAsServiceAddress(null)
                .directorAppliedToProtectDetails(false)
                .consentToAct(true)
                .corporateDirector(false)
                .countryOfResidence(RESIDENTIAL_ADDRESS.getCountry())
                .build();
        final var now = clock.instant();
        final var officerFiling = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();

        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(officerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(filingAPIMapper.map(officerFiling)).thenReturn(filingData);

        final var filingApi = testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        final Map<String, Object> expectedMap =
                Map.ofEntries(
                        Map.entry("title", "Major"),
                        Map.entry("first_name", FIRSTNAME),
                        Map.entry("middle_names", MIDDLENAMES),
                        Map.entry("last_name", LASTNAME),
                        Map.entry("former_names", "former names"),
                        Map.entry("date_of_birth", DATE_OF_BIRTH_STR),
                        Map.entry("appointed_on", RESIGNED_ON_STR),
                        Map.entry("nationality1", "nationality1"),
                        Map.entry("nationality2", "nationality2"),
                        Map.entry("nationality3", "nationality3"),
                        Map.entry("occupation", "occupation"),
                        Map.entry("service_address", Map.of(
                                "premises", "11",
                                "address_line_1", "One Street",
                                "country", "England",
                                "postal_code", "TE1 3ST")),
                        Map.entry("residential_address", Map.of(
                                "premises", "12",
                                "address_line_1", "Two Street",
                                "country", "Wales",
                                "postal_code", "TE2 4ST")),
                        Map.entry("director_applied_to_protect_details", false),
                        Map.entry("consent_to_act", true),
                        Map.entry("is_corporate_director", false),
                        Map.entry("country_of_residence", "Wales"),
                        Map.entry("service_address_same_as_registered_office_address", false),
                        Map.entry("home_address_same_as_service_address", false),
                        Map.entry("directors_details_changed_date", "2023-01-01"));

        assertThat(filingApi.getData(), is(equalTo(expectedMap)));
        assertThat(filingApi.getKind(), is("officer-filing#appointment"));
        assertThat(filingApi.getDescription(), is(equalTo("(AP01) Appointment of a director. Appointment of JOE PETER MARTIN BLOGGS on 5 October 2022")));
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

    @Test
    void generateUpdateOfficerFilingWhenFound() {
        final var originalData = testOfficerFilingDataBuilder().build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        final var filingApi = testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        assertThat(filingApi.getKind(), is("officer-filing#update"));
        assertThat(filingApi.getDescription(), is(equalTo("(CH01) Update of a director. Update of JOE PETER MARTIN BLOGGS on 1 October 2023")));

        var expectedOfficerFilingData = OfficerFilingData.builder()
                .title(originalData.getTitle())
                .firstName(FIRSTNAME)
                .middleNames(MIDDLENAMES)
                .lastName(LASTNAME)
                .formerNames(originalData.getFormerNames())
                .nationality1(NATIONALITY_1)
                .nationality2(NATIONALITY_2)
                .nationality3(NATIONALITY_3)
                .occupation(OCCUPATION)
                .serviceAddress(SERVICE_ADDRESS)
                .isServiceAddressSameAsRegisteredOfficeAddress(false)
                .residentialAddress(RESIDENTIAL_ADDRESS)
                .isHomeAddressSameAsServiceAddress(false)
                .countryOfResidence(RESIDENTIAL_ADDRESS.getCountry())
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void generateUpdateOfficerFilingWhenNothingUpdated() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .serviceAddressHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(false)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(any(), any())).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        final var filingApi = testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        assertThat(filingApi.getKind(), is("officer-filing#update"));
        assertThat(filingApi.getDescription(), is(equalTo("(CH01) Update of a director. Update of JOE PETER MARTIN BLOGGS on 1 October 2023")));

        var expectedOfficerFilingData = OfficerFilingData.builder()
                .title(null)
                .firstName(null)
                .nationality1(null)
                .occupation(null)
                .serviceAddress(null)
                .residentialAddress(null)
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void generateUpdateOfficerFilingWhenNameHasBeenUpdated() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(true)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(false)
                .serviceAddressHasBeenUpdated(false)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        final var filingApi = testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .title(originalData.getTitle())
                .firstName(FIRSTNAME)
                .middleNames(MIDDLENAMES)
                .lastName(LASTNAME)
                .formerNames(originalData.getFormerNames())
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void generateUpdateOfficerFilingWhenNameHasNotBeenUpdated() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(false)
                .serviceAddressHasBeenUpdated(false)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .title(null)
                .firstName(null)
                .middleNames(null)
                .lastName(null)
                .formerNames(null)
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void generateUpdateOfficerFilingWhenNationalityHasBeenUpdated() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(true)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(false)
                .serviceAddressHasBeenUpdated(false)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        final var filingApi = testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .nationality1(NATIONALITY_1)
                .nationality2(NATIONALITY_2)
                .nationality3(NATIONALITY_3)
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void generateUpdateOfficerFilingWhenNationalityHasNotBeenUpdated() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(false)
                .serviceAddressHasBeenUpdated(false)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        final var filingApi = testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .nationality1(null)
                .nationality2(null)
                .nationality3(null)
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void generateUpdateOfficerFilingWhenOccupationHasBeenUpdated() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(true)
                .residentialAddressHasBeenUpdated(false)
                .serviceAddressHasBeenUpdated(false)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .occupation(OCCUPATION)
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void generateUpdateOfficerFilingWhenOccupationHasNotBeenUpdated() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(false)
                .serviceAddressHasBeenUpdated(false)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .occupation(null)
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {false})
    void generateUpdateOfficerFilingWhenServiceAddressHasBeenUpdatedAndSameAsLinkIsFalse(Boolean sameAsLink) {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(false)
                .serviceAddressHasBeenUpdated(true)
                .isServiceAddressSameAsRegisteredOfficeAddress(sameAsLink)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .serviceAddress(SERVICE_ADDRESS)
                .isServiceAddressSameAsRegisteredOfficeAddress(sameAsLink)
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void generateUpdateOfficerFilingWhenServiceAddressHasBeenUpdatedAndSameAsLinkIsTrue() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(false)
                .serviceAddressHasBeenUpdated(true)
                .isServiceAddressSameAsRegisteredOfficeAddress(true)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .serviceAddress(null)
                .isServiceAddressSameAsRegisteredOfficeAddress(true)
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void generateUpdateOfficerFilingWhenCorrespondenceAddressHasNotBeenUpdated(boolean sameAsLink) {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(false)
                .serviceAddressHasBeenUpdated(false)
                .isServiceAddressSameAsRegisteredOfficeAddress(sameAsLink)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .serviceAddress(null)
                .isServiceAddressSameAsRegisteredOfficeAddress(null)
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = {false})
    void generateUpdateOfficerFilingWhenResidentialAddressHasBeenUpdatedAndSameAsLinkIsFalse(Boolean sameAsLink) {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(true)
                .serviceAddressHasBeenUpdated(false)
                .isHomeAddressSameAsServiceAddress(sameAsLink)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .residentialAddress(RESIDENTIAL_ADDRESS)
                .isHomeAddressSameAsServiceAddress(sameAsLink)
                .countryOfResidence(RESIDENTIAL_ADDRESS.getCountry())
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void generateUpdateOfficerFilingWhenResidentialAddressHasBeenUpdatedAndSameAsLinkIsTrue() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(true)
                .serviceAddressHasBeenUpdated(false)
                .isHomeAddressSameAsServiceAddress(true)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointment.getServiceAddress()).thenReturn(AddressAPI.builder().withCountry("Country").build());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .residentialAddress(null)
                .isHomeAddressSameAsServiceAddress(true)
                .countryOfResidence("Country")
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void generateUpdateOfficerFilingWhenResidentialAddressHasNotBeenUpdated(boolean sameAsLink) {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(false)
                .serviceAddressHasBeenUpdated(false)
                .isHomeAddressSameAsServiceAddress(sameAsLink)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .residentialAddress(null)
                .isHomeAddressSameAsServiceAddress(null)
                .countryOfResidence(null)
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void genUpdateFilingWhenHANotUpdatedButSameAsCAAndCaUpdated() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(false)
                .serviceAddressHasBeenUpdated(true)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointment.getResidentialAddressIsSameAsServiceAddress()).thenReturn(true);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .serviceAddress(SERVICE_ADDRESS)
                .isServiceAddressSameAsRegisteredOfficeAddress(false)
                .residentialAddress(null)
                .isHomeAddressSameAsServiceAddress(null)
                .countryOfResidence(SERVICE_ADDRESS.getCountry())
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void genUpdateFilingWhenHANotUpdatedButSameAsCAAndAddressIsNull() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(null)
                .residentialAddress(null)
                .serviceAddressHasBeenUpdated(true)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointment.getResidentialAddressIsSameAsServiceAddress()).thenReturn(true);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .serviceAddress(SERVICE_ADDRESS)
                .isServiceAddressSameAsRegisteredOfficeAddress(false)
                .residentialAddress(null)
                .isHomeAddressSameAsServiceAddress(null)
                .countryOfResidence(SERVICE_ADDRESS.getCountry())
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void genUpdateFilingWhenHANotUpdatedButSameAsCAAndAddressFieldsAreNull() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(null)
                .residentialAddress(Address.builder().build())
                .serviceAddressHasBeenUpdated(true)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointment.getResidentialAddressIsSameAsServiceAddress()).thenReturn(true);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .serviceAddress(SERVICE_ADDRESS)
                .isServiceAddressSameAsRegisteredOfficeAddress(false)
                .residentialAddress(null)
                .isHomeAddressSameAsServiceAddress(null)
                .countryOfResidence(SERVICE_ADDRESS.getCountry())
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void genUpdateFilingWhenHAUpdatedButSameAsCAAndCaUpdated() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(true)
                .serviceAddressHasBeenUpdated(true)
                .isHomeAddressSameAsServiceAddress(true)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .serviceAddress(SERVICE_ADDRESS)
                .isServiceAddressSameAsRegisteredOfficeAddress(false)
                .residentialAddress(null)
                .isHomeAddressSameAsServiceAddress(true)
                .countryOfResidence(SERVICE_ADDRESS.getCountry())
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void genUpdateFilingWhenHAUpdatedButSameAsCAAndCaMissing() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(true)
                .serviceAddressHasBeenUpdated(false)
                .isHomeAddressSameAsServiceAddress(true)
                .serviceAddress(null)
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointment.getServiceAddress()).thenReturn(AddressAPI.builder().withCountry("Country").build());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .residentialAddress(null)
                .isHomeAddressSameAsServiceAddress(true)
                .countryOfResidence("Country")
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void genUpdateFilingWhenHAUpdatedButSameAsCAAndCaCountryMissing() {
        final var originalData = testOfficerFilingDataBuilder()
                .nameHasBeenUpdated(false)
                .nationalityHasBeenUpdated(false)
                .occupationHasBeenUpdated(false)
                .residentialAddressHasBeenUpdated(true)
                .serviceAddressHasBeenUpdated(false)
                .isHomeAddressSameAsServiceAddress(true)
                .serviceAddress(Address.builder(SERVICE_ADDRESS).country(null).build())
                .build();
        final var originalOfficerFiling = OfficerFiling.builder()
                .createdAt(clock.instant())
                .updatedAt(clock.instant())
                .data(originalData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(originalOfficerFiling));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointment.getForename()).thenReturn(FIRSTNAME);
        when(companyAppointment.getOtherForenames()).thenReturn(MIDDLENAMES);
        when(companyAppointment.getSurname()).thenReturn(LASTNAME);
        when(companyAppointment.getDateOfBirth()).thenReturn(testSensitiveDateOfBirth());
        when(companyAppointment.getServiceAddress()).thenReturn(AddressAPI.builder().withCountry("Country").build());
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, REF_APPOINTMENT_ID, PASSTHROUGH_HEADER))
                .thenReturn(companyAppointment);

        testService.generateOfficerFiling(TRANS_ID, FILING_ID, PASSTHROUGH_HEADER);

        verify(filingAPIMapper).map(officerFilingCaptor.capture());
        var builtOfficerFilingData = officerFilingCaptor.getValue().getData();
        var expectedOfficerFilingData = OfficerFilingData.builder()
                .residentialAddress(null)
                .isHomeAddressSameAsServiceAddress(true)
                .countryOfResidence("Country")
                .build();
        assertThat(builtOfficerFilingData, samePropertyValuesAs(expectedOfficerFilingData, "officerPreviousDetails", "directorsDetailsChangedDate"));
        assertThat(builtOfficerFilingData.getOfficerPreviousDetails(), samePropertyValuesAs(testOfficerPreviousDetails()));
        assertThat(builtOfficerFilingData.getDirectorsDetailsChangedDate(), is(DIRECTOR_DETAILS_CHANGED_DATE));
    }

    @Test
    void testSetTm01DescriptionFieldsWithFullName() {
        // Prepare test data
        final Instant resignedOn = Instant.parse("2022-10-05T00:00:00Z");
        final String formattedResignationDate = "5 October 2022";
        final OfficerFilingData officerFilingData = OfficerFilingData.builder()
                .resignedOn(resignedOn)
                .build();
        final FilingApi filing = new FilingApi();

        // when
        when(companyAppointment.getTitle()).thenReturn("Dr");
        when(companyAppointment.getSurname()).thenReturn("Bloggs");
        when(companyAppointment.getForename()).thenReturn("Joe");
        when(companyAppointment.getOtherForenames()).thenReturn("Peter Martin");

        // under test
        testService.setTm01DescriptionFields(filing, officerFilingData, companyAppointment);

        // then
        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("director name", "DR JOE PETER MARTIN BLOGGS");
        expectedValues.put("termination date", formattedResignationDate);

        assertThat(filing.getDescription(), is("(TM01) Termination of appointment of a director. Terminating appointment of DR JOE PETER MARTIN BLOGGS on 5 October 2022"));
        assertThat(expectedValues, is(filing.getDescriptionValues()));
    }

    @Test
    void testSetTm01DescriptionFieldsWithFirstNameLastName() {
        // Prepare test data
        final Instant resignedOn = Instant.parse("2022-10-05T00:00:00Z");
        final String formattedResignationDate = "5 October 2022";
        final OfficerFilingData officerFilingData = OfficerFilingData.builder()
                .resignedOn(resignedOn)
                .build();
        final FilingApi filing = new FilingApi();

        // when
        when(companyAppointment.getSurname()).thenReturn("Bloggs");
        when(companyAppointment.getForename()).thenReturn("Joe");

        // under test
        testService.setTm01DescriptionFields(filing, officerFilingData, companyAppointment);

        // then
        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("director name", "JOE BLOGGS");
        expectedValues.put("termination date", formattedResignationDate);

        assertThat(filing.getDescription(), is("(TM01) Termination of appointment of a director. Terminating appointment of JOE BLOGGS on 5 October 2022"));
        assertThat(expectedValues, is(filing.getDescriptionValues()));
    }

    @Test
    void testSetTm01DescriptionFieldsWithFirstNameLastNameAndEMPTYTitleWHITESPACEDMiddleName() {
        // Prepare test data
        final Instant resignedOn = Instant.parse("2022-10-05T00:00:00Z");
        final String formattedResignationDate = "5 October 2022";
        final OfficerFilingData officerFilingData = OfficerFilingData.builder()
                .resignedOn(resignedOn)
                .build();
        final FilingApi filing = new FilingApi();

        // when
        when(companyAppointment.getTitle()).thenReturn("");
        when(companyAppointment.getSurname()).thenReturn("Bloggs");
        when(companyAppointment.getOtherForenames()).thenReturn("  ");
        when(companyAppointment.getForename()).thenReturn("Joe");

        // under test
        testService.setTm01DescriptionFields(filing, officerFilingData, companyAppointment);

        // then
        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("director name", "JOE BLOGGS");
        expectedValues.put("termination date", formattedResignationDate);

        assertThat(filing.getDescription(), is("(TM01) Termination of appointment of a director. Terminating appointment of JOE BLOGGS on 5 October 2022"));
        assertThat(expectedValues, is(filing.getDescriptionValues()));
    }

    @Test
    void setAp01DescriptionFieldsWithFullName() {
        // Prepare test data
        final String formattedAppointmentDate = "1 January 2023";
        final OfficerFilingData officerFilingData = OfficerFilingData.builder()
                .appointedOn(Instant.parse("2023-01-01T00:00:00Z"))
                .title("Dr")
                .firstName("Joe")
                .middleNames("Peter Martin")
                .lastName("Bloggs")
                .build();
        final FilingApi filing = new FilingApi();

        // under test
        testService.setAp01DescriptionFields(filing, officerFilingData);

        // then
        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("director name", "DR JOE PETER MARTIN BLOGGS");
        expectedValues.put("appointment date", formattedAppointmentDate);
        assertThat(filing.getDescription(), is("(AP01) Appointment of a director. Appointment of DR JOE PETER MARTIN BLOGGS on 1 January 2023"));
        assertThat(filing.getDescriptionValues(), is(expectedValues));
    }

    @Test
    void setAp01DescriptionFieldsWithFistNameLastName() {
        // Prepare test data
        final String formattedAppointmentDate = "1 January 2023";
        final OfficerFilingData officerFilingData = OfficerFilingData.builder()
                .appointedOn(Instant.parse("2023-01-01T00:00:00Z"))
                .firstName("Joe")
                .lastName("Bloggs")
                .build();
        final FilingApi filing = new FilingApi();

        // under test
        testService.setAp01DescriptionFields(filing, officerFilingData);

        // then
        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("director name", "JOE BLOGGS");
        expectedValues.put("appointment date", formattedAppointmentDate);
        assertThat(filing.getDescription(), is("(AP01) Appointment of a director. Appointment of JOE BLOGGS on 1 January 2023"));
        assertThat(filing.getDescriptionValues(), is(expectedValues));
    }


    @Test
    void setAp01DescriptionFieldsWithFistNameLastNameAndEMPTYTitleWHITESPACEDMiddleName() {
        // Prepare test data
        final String formattedAppointmentDate = "1 January 2023";
        final OfficerFilingData officerFilingData = OfficerFilingData.builder()
                .appointedOn(Instant.parse("2023-01-01T00:00:00Z"))
                .title("")
                .firstName("Joe")
                .lastName("Bloggs")
                .middleNames("  ")
                .build();
        final FilingApi filing = new FilingApi();

        // under test
        testService.setAp01DescriptionFields(filing, officerFilingData);

        // then
        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("director name", "JOE BLOGGS");
        expectedValues.put("appointment date", formattedAppointmentDate);
        assertThat(filing.getDescription(), is("(AP01) Appointment of a director. Appointment of JOE BLOGGS on 1 January 2023"));
        assertThat(filing.getDescriptionValues(), is(expectedValues));
    }

    @Test
    void setCh01DescriptionFieldsWithFullName() {
        // Prepare test data
        final String formattedUpdateDate = "1 January 2023";
        final OfficerFilingData officerFilingData = OfficerFilingData.builder()
                .directorsDetailsChangedDate(Instant.parse("2023-01-01T00:00:00Z"))
                .build();
        final FilingApi filing = new FilingApi();

        // when
        when(companyAppointment.getTitle()).thenReturn("Dr");
        when(companyAppointment.getSurname()).thenReturn("Bloggs");
        when(companyAppointment.getForename()).thenReturn("Joe");
        when(companyAppointment.getOtherForenames()).thenReturn("Peter Martin");

        // under test
        testService.setCh01DescriptionFields(filing, officerFilingData, companyAppointment);

        // Assert
        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("director name", "DR JOE PETER MARTIN BLOGGS");
        expectedValues.put("update date", formattedUpdateDate);
        assertThat(filing.getDescription(), is("(CH01) Update of a director. Update of DR JOE PETER MARTIN BLOGGS on 1 January 2023"));
        assertThat(filing.getDescriptionValues(), is(expectedValues));
    }


    @Test
    void setCh01DescriptionFieldsWithFirstNameLastName() {
        // Prepare test data
        final String formattedUpdateDate = "1 January 2023";
        final OfficerFilingData officerFilingData = OfficerFilingData.builder()
                .directorsDetailsChangedDate(Instant.parse("2023-01-01T00:00:00Z"))
                .build();
        final FilingApi filing = new FilingApi();

        // when
        when(companyAppointment.getSurname()).thenReturn("Bloggs");
        when(companyAppointment.getForename()).thenReturn("Joe");

        // under test
        testService.setCh01DescriptionFields(filing, officerFilingData, companyAppointment);

        // then
        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("director name", "JOE BLOGGS");
        expectedValues.put("update date", formattedUpdateDate);
        assertThat(filing.getDescription(), is("(CH01) Update of a director. Update of JOE BLOGGS on 1 January 2023"));
        assertThat(filing.getDescriptionValues(), is(expectedValues));
    }

    @Test
    void setCh01DescriptionFieldsWithFirstNameLastNameAndEMPTYTitleWHITESPACEDMiddleName() {
        // Prepare test data
        final String formattedUpdateDate = "1 January 2023";
        final OfficerFilingData officerFilingData = OfficerFilingData.builder()
                .directorsDetailsChangedDate(Instant.parse("2023-01-01T00:00:00Z"))
                .build();
        final FilingApi filing = new FilingApi();

        // when
        when(companyAppointment.getSurname()).thenReturn("");
        when(companyAppointment.getSurname()).thenReturn("Bloggs");
        when(companyAppointment.getOtherForenames()).thenReturn("  ");
        when(companyAppointment.getForename()).thenReturn("Joe");

        // under test
        testService.setCh01DescriptionFields(filing, officerFilingData, companyAppointment);

        // then
        Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("director name", "JOE BLOGGS");
        expectedValues.put("update date", formattedUpdateDate);
        assertThat(filing.getDescription(), is("(CH01) Update of a director. Update of JOE BLOGGS on 1 January 2023"));
        assertThat(filing.getDescriptionValues(), is(expectedValues));
    }

    private static OfficerFilingData.Builder testOfficerFilingDataBuilder() {
        return OfficerFilingData.builder()
                .referenceAppointmentId(REF_APPOINTMENT_ID)
                .referenceEtag(REF_ETAG)
                .title("TITLE")
                .firstName(FIRSTNAME)
                .middleNames(MIDDLENAMES)
                .lastName(LASTNAME)
                .formerNames("former names")
                .dateOfBirth(DATE_OF_BIRTH_INS)
                .appointedOn(RESIGNED_ON_INS)
                .nationality1(NATIONALITY_1)
                .nationality2(NATIONALITY_2)
                .nationality3(NATIONALITY_3)
                .occupation(OCCUPATION)
                .serviceAddress(SERVICE_ADDRESS)
                .isServiceAddressSameAsRegisteredOfficeAddress(false)
                .residentialAddress(RESIDENTIAL_ADDRESS)
                .isHomeAddressSameAsServiceAddress(false)
                .directorAppliedToProtectDetails(false)
                .consentToAct(true)
                .corporateDirector(false)
                .directorsDetailsChangedDate(DIRECTOR_DETAILS_CHANGED_DATE)
                .countryOfResidence(RESIDENTIAL_ADDRESS.getCountry())
                .officerPreviousDetails(
                        OfficerPreviousDetails.builder()
                                .title("Major")
                                .firstName(FIRSTNAME)
                                .middleNames(MIDDLENAMES)
                                .lastName(LASTNAME)
                                .dateOfBirth(DATE_OF_BIRTH_STR)
                                .build()
                );
    }

    private static SensitiveDateOfBirthAPI testSensitiveDateOfBirth() {
        SensitiveDateOfBirthAPI sensitiveDateOfBirth = new SensitiveDateOfBirthAPI();
        sensitiveDateOfBirth.setYear(2000);
        sensitiveDateOfBirth.setMonth(10);
        sensitiveDateOfBirth.setDay(20);
        return sensitiveDateOfBirth;
    }

    private static OfficerPreviousDetails testOfficerPreviousDetails() {
        return OfficerPreviousDetails.builder()
                .firstName(FIRSTNAME)
                .middleNames(MIDDLENAMES)
                .lastName(LASTNAME)
                .dateOfBirth(DATE_OF_BIRTH_STR)
                .build();
    }
}