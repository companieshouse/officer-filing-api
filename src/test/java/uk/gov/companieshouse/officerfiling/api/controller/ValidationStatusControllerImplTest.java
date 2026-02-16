package uk.gov.companieshouse.officerfiling.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;
import uk.gov.companieshouse.officerfiling.api.exception.FeatureNotEnabledException;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.dto.AddressDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
import uk.gov.companieshouse.officerfiling.api.model.mapper.ErrorMapper;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;
import uk.gov.companieshouse.officerfiling.api.validation.OfficerTerminationValidator;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationStatusControllerImplTest {

    private static final String TRANS_ID = "117524-754816-491724";
    private static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String ETAG = "etag";
    private static final String COMPANY_TYPE = "ltd";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final String PASSTHROUGH_HEADER = "passthrough";

    @Mock
    private OfficerFilingService officerFilingService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Logger logger;
    @Mock
    private TransactionService transactionService;
    @Mock
    private CompanyProfileService companyProfileService;
    @Mock
    private CompanyAppointmentService companyAppointmentService;
    @Mock
    private OfficerFilingMapper officerFilingMapper;
    @Mock
    private Transaction transaction;
    @Mock
    private AppointmentFullRecordAPI companyAppointment;
    @Mock
    private CompanyProfileApi companyProfile;
    @Mock
    private OfficerFilingDto dto;
    @Mock
    private ErrorMapper errorMapper;
    @Mock
    private ApiEnumerations apiEnumerations;

    private OfficerFiling filing;
    private ValidationStatusControllerImpl testController;
    @Mock
    private Clock clock;

    @Mock
    private OfficerTerminationValidator officerTerminationValidator;

    @BeforeEach
    void setUp() {
        testController = new ValidationStatusControllerImpl(officerFilingService, logger,
             companyProfileService, companyAppointmentService, officerFilingMapper,
            errorMapper, apiEnumerations);
        ReflectionTestUtils.setField(testController, "isTm01Enabled", true);
        ReflectionTestUtils.setField(testController, "isAp01Enabled", false);

        var offData = new OfficerFilingData(
                "etag",
                "off-id",
                Instant.parse("2022-09-13T00:00:00Z"));
        final var now = clock.instant();
        filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();
    }

    @Test
    void validateWhenFilingNotFound() {
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.empty());
        when(transaction.getId()).thenReturn(TRANS_ID);

        assertThrows(ResourceNotFoundException.class, () -> testController.validate(transaction, FILING_ID, request));
    }
    @Test
    void validateWhenFilingFoundAndNoValidationErrors() {
        ReflectionTestUtils.setField(testController, "isAp01Enabled", false);
        validationStatusControllerMocks();
        when(dto.getReferenceEtag()).thenReturn(ETAG);
        when(dto.getReferenceAppointmentId()).thenReturn(FILING_ID);
        when(dto.getResignedOn()).thenReturn(LocalDate.of(2009, 10, 1));

        final var response = testController.validate(transaction, FILING_ID, request);
        assertThat(response.getValidationStatusError(), is(nullValue()));
        assertThat(response.isValid(), is(true));
    }

    @Test
    void validateWhenFilingFoundAndValidationErrors() {
        ReflectionTestUtils.setField(testController, "isAp01Enabled", false);
        validationStatusControllerMocks();
        when(companyAppointmentService.getCompanyAppointment( TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(dto.getReferenceEtag()).thenReturn("etag");
        when(dto.getReferenceAppointmentId()).thenReturn(FILING_ID);
        when(dto.getResignedOn()).thenReturn(LocalDate.of(1022, 9, 13));
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2021, 10, 3));
        when(companyProfile.getType()).thenReturn("invalid-type");
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2021, 10, 5));
        when(errorMapper.map(anySet())).thenReturn(new ValidationStatusError[4]);

        when(apiEnumerations.getCompanyType("invalid-type")).thenReturn("Invalid Company Type");
        when(apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_APPOINTMENT_DATE, "Director")).thenReturn("Date Director was removed must be on or after the date the director was appointed");
        when(apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_INCORPORATION_DATE)).thenReturn("The date you enter must be after the company's incorporation date");
        when(apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_2009)).thenReturn("Enter a date that is on or after 1 October 2009. If the director was removed before this date, you must file form 288b instead");
        when(apiEnumerations.getValidation(ValidationEnum.COMPANY_TYPE_NOT_PERMITTED, "Invalid Company Type")).thenReturn("Invalid Company Type not permitted");

        final var response = testController.validate(transaction, FILING_ID, request);
        assertThat(response.isValid(), is(false));
        assertThat(response.getValidationStatusError().length, is(4));
    }

    @Test
    void checkTm01FeatureFlagDisabled(){
        ReflectionTestUtils.setField(testController, "isTm01Enabled", false);
        assertThrows(FeatureNotEnabledException.class,
            () -> testController.validate(transaction, FILING_ID, request));
    }

    void validationStatusControllerMocks() {
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(officerFilingMapper.map(filing)).thenReturn(dto);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(
                PASSTHROUGH_HEADER);
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER,
                PASSTHROUGH_HEADER)).thenReturn(companyProfile);
    }

    @Test
    void validateWhenFilingAP01FoundAndNoValidationErrors() {
        ReflectionTestUtils.setField(testController, "isAp01Enabled", true);
        ReflectionTestUtils.setField(testController, "inputAllowedNationalities", "A very long nationality indeed so long in fact that it breaks the legal length for nationalities,thisIs25Characterslongggh,thisIs25Characterslongggg,thisIs16Charactz,thisIs17Character,thisIs16Characte,thisIsAVeryLongNationalityWhichWilltakeUsOver50Characterslong,Afghan,Albanian,Algerian,American,Andorran,Angolan,Anguillan,Citizen of Antigua and Barbuda,Argentine,Armenian,Australian,Austrian,Azerbaijani,Bahamian,Bahraini,Bangladeshi,Barbadian,Belarusian,Belgian,Belizean,Beninese,Bermudian,Bhutanese,Bolivian,Citizen of Bosnia and Herzegovina,Botswanan,Brazilian,British,British Virgin Islander,Bruneian,Bulgarian,Burkinan,Burmese,Burundian,Cambodian,Cameroonian,Canadian,Cape Verdean,Cayman Islander,Central African,Chadian,Chilean,Chinese,Colombian,Comoran,Congolese (Congo),Congolese (DRC),Cook Islander,Costa Rican,Croatian,Cuban,Cymraes,Cymro,Cypriot,Czech,Danish,Djiboutian,Dominican,Citizen of the Dominican Republic,Dutch,East Timorese\tEcuadorean\tEgyptian\tEmirati,English,Equatorial Guinean,Eritrean,Estonian,Ethiopian,Faroese,Fijian,Filipino,Finnish,French,Gabonese,Gambian,Georgian,German,Ghanaian,Gibraltarian,Greek,Greenlandic,Grenadian,Guamanian,Guatemalan,Citizen of Guinea-Bissau,Guinean,Guyanese,Haitian,Honduran,Hong Konger,Hungarian,Icelandic,Indian,Indonesian,Iranian,Iraqi,Irish,Israeli,Italian,Ivorian,Jamaican,Japanese,Jordanian,Kazakh,Kenyan,Kittitian,Citizen of Kiribati,Kosovan,Kuwaiti,Kyrgyz,Lao,Latvian,Lebanese,Liberian,Libyan,Liechtenstein citizen,Lithuanian,Luxembourger,Macanese,Macedonian,Malagasy,Malawian,Malaysian,Maldivian,Malian,Maltese,Marshallese,Martiniquais,Mauritanian,Mauritian,Mexican,Micronesian,Moldovan,Monegasque,Mongolian,Montenegrin,Montserratian,Moroccan,Mosotho,Mozambican,Namibian,Nauruan,Nepalese,New Zealander,Nicaraguan,Nigerian,Nigerien,Niuean,North Korean,Northern Irish,Norwegian,Omani,Pakistani,Palauan,Palestinian,Panamanian,Papua New Guinean,Paraguayan,Peruvian,Pitcairn Islander,Polish,Portuguese,Prydeinig,Puerto Rican,Qatari,Romanian,Russian,Rwandan,Salvadorean,Sammarinese,Samoan,Sao Tomean,Saudi Arabian,Scottish,Senegalese,Serbian,Citizen of Seychelles,Sierra Leonean,Singaporean,Slovak,Slovenian,Solomon Islander,Somali,South African,South Korean,South Sudanese,Spanish,Sri Lankan,St Helenian,St Lucian,Stateless,Sudanese,Surinamese,Swazi,Swedish,Swiss,Syrian,Taiwanese,Tajik,Tanzanian,Thai,Togolese,Tongan,Trinidadian,Tristanian,Tunisian,Turkish,Turkmen,Turks and Caicos Islander,Tuvaluan,Ugandan,Ukrainian,Uruguayan,Uzbek,Vatican citizen,Citizen of Vanuatu,Venezuelan,Vietnamese,Vincentian,Wallisian,Welsh,Yemeni,Zambian,Zimbabwean");

        ReflectionTestUtils.setField(testController, "ukCountryList", List.of(""));
        ReflectionTestUtils.setField(testController, "countryList", List.of("France"));
        LocalDate localDateDob1 = LocalDate.of(1970, 9, 12);
        validationStatusControllerMocks();
        when(companyProfile.getType()).thenReturn(COMPANY_TYPE);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder().premises("9")
                .addressLine1("Road").locality("Margate").country("France").build());
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder().premises("9")
                .addressLine1("Road").locality("Margate").country("France").build());
        when(dto.getDateOfBirth()).thenReturn(localDateDob1);
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getConsentToAct()).thenReturn(true);
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        final var response = testController.validate(transaction, FILING_ID, request);
        assertThat(response.getValidationStatusError(), is(nullValue()));
        assertThat(response.isValid(), is(true));
    }

    @Test
    void validateWhenFilingCH01FoundAndNoValidationErrors() {
        ReflectionTestUtils.setField(testController, "isCh01Enabled", true);
        when(dto.getReferenceEtag()).thenReturn(ETAG);
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(officerFilingMapper.map(filing)).thenReturn(dto);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getResignedOn()).thenReturn(null);
        when(dto.getDirectorsDetailsChangedDate()).thenReturn(LocalDate.of(2023, Month.JANUARY, 5));

        final var response = testController.validate(transaction, FILING_ID, request);
        assertThat(response.getValidationStatusError(), is(nullValue()));
        assertThat(response.isValid(), is(true));
    }

    @Test
    void validateWhenFilingHasTerminationDateButFeatureTM01IsDisabled() {
        ReflectionTestUtils.setField(testController, "isTm01Enabled", false);
        when(dto.getResignedOn()).thenReturn(LocalDate.of(2009, 10, 1));

        Exception exception = assertThrows(FeatureNotEnabledException.class,
                () -> testController.validate(request, dto, transaction, PASSTHROUGH_HEADER));

        assertEquals(exception.getClass(), FeatureNotEnabledException.class);
    }

    @Test
    void validateWhenFilingHasReferenceETagButFeatureAP01IsDisabled() {
        ReflectionTestUtils.setField(testController, "isAp01Enabled", false);
        when(dto.getReferenceEtag()).thenReturn(ETAG);

        Exception exception = assertThrows(FeatureNotEnabledException.class,
                () -> testController.validate(request, dto, transaction, PASSTHROUGH_HEADER));

        assertEquals(exception.getClass(), FeatureNotEnabledException.class);
    }

    @Test
    void validateWhenFilingIsNeitherTM01orAP01ButFeatureCH01IsDisabled() {
        ReflectionTestUtils.setField(testController, "isCh01Enabled", false);

        Exception exception = assertThrows(FeatureNotEnabledException.class,
                () -> testController.validate(request, dto, transaction, PASSTHROUGH_HEADER));

        assertEquals(exception.getClass(), FeatureNotEnabledException.class);
    }
}