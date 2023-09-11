package uk.gov.companieshouse.officerfiling.api.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileServiceImpl;
import uk.gov.companieshouse.officerfiling.api.service.TransactionServiceImpl;

@ExtendWith(MockitoExtension.class)
class OfficerAppointmentValidatorTest {
    private static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String TRANS_ID = "12345-54321-76666";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final String ETAG = "etag";
    private static final String COMPANY_TYPE = "ltd";
    private static final String OFFICER_ROLE = "director";

    private OfficerAppointmentValidator officerAppointmentValidator;
    private List<ApiError> apiErrorsList;

    @Mock
    private HttpServletRequest request;
    @Mock
    private Logger logger;
    @Mock
    private TransactionServiceImpl transactionService;
    @Mock
    private CompanyProfileServiceImpl companyProfileService;
    @Mock
    private Transaction transaction;
    @Mock
    private CompanyProfileApi companyProfile;
    @Mock
    private AppointmentFullRecordAPI companyAppointment;
    @Mock
    private ApiEnumerations apiEnumerations;
    @Mock
    private OfficerFilingDto dto;

    @BeforeEach
    void setUp() {
        String allowedNationalities = "A very long nationality indeed so long in fact that it breaks the legal length for nationalities,thisIs25Characterslongggh,thisIs25Characterslongggg,thisIs16Charactz,thisIs17Character,thisIs16Characte,thisIsAVeryLongNationalityWhichWilltakeUsOver50Characterslong,Afghan,Albanian,Algerian,American,Andorran,Angolan,Anguillan,Citizen of Antigua and Barbuda,Argentine,Armenian,Australian,Austrian,Azerbaijani,Bahamian,Bahraini,Bangladeshi,Barbadian,Belarusian,Belgian,Belizean,Beninese,Bermudian,Bhutanese,Bolivian,Citizen of Bosnia and Herzegovina,Botswanan,Brazilian,British,British Virgin Islander,Bruneian,Bulgarian,Burkinan,Burmese,Burundian,Cambodian,Cameroonian,Canadian,Cape Verdean,Cayman Islander,Central African,Chadian,Chilean,Chinese,Colombian,Comoran,Congolese (Congo),Congolese (DRC),Cook Islander,Costa Rican,Croatian,Cuban,Cymraes,Cymro,Cypriot,Czech,Danish,Djiboutian,Dominican,Citizen of the Dominican Republic,Dutch,East Timorese\tEcuadorean\tEgyptian\tEmirati,English,Equatorial Guinean,Eritrean,Estonian,Ethiopian,Faroese,Fijian,Filipino,Finnish,French,Gabonese,Gambian,Georgian,German,Ghanaian,Gibraltarian,Greek,Greenlandic,Grenadian,Guamanian,Guatemalan,Citizen of Guinea-Bissau,Guinean,Guyanese,Haitian,Honduran,Hong Konger,Hungarian,Icelandic,Indian,Indonesian,Iranian,Iraqi,Irish,Israeli,Italian,Ivorian,Jamaican,Japanese,Jordanian,Kazakh,Kenyan,Kittitian,Citizen of Kiribati,Kosovan,Kuwaiti,Kyrgyz,Lao,Latvian,Lebanese,Liberian,Libyan,Liechtenstein citizen,Lithuanian,Luxembourger,Macanese,Macedonian,Malagasy,Malawian,Malaysian,Maldivian,Malian,Maltese,Marshallese,Martiniquais,Mauritanian,Mauritian,Mexican,Micronesian,Moldovan,Monegasque,Mongolian,Montenegrin,Montserratian,Moroccan,Mosotho,Mozambican,Namibian,Nauruan,Nepalese,New Zealander,Nicaraguan,Nigerian,Nigerien,Niuean,North Korean,Northern Irish,Norwegian,Omani,Pakistani,Palauan,Palestinian,Panamanian,Papua New Guinean,Paraguayan,Peruvian,Pitcairn Islander,Polish,Portuguese,Prydeinig,Puerto Rican,Qatari,Romanian,Russian,Rwandan,Salvadorean,Sammarinese,Samoan,Sao Tomean,Saudi Arabian,Scottish,Senegalese,Serbian,Citizen of Seychelles,Sierra Leonean,Singaporean,Slovak,Slovenian,Solomon Islander,Somali,South African,South Korean,South Sudanese,Spanish,Sri Lankan,St Helenian,St Lucian,Stateless,Sudanese,Surinamese,Swazi,Swedish,Swiss,Syrian,Taiwanese,Tajik,Tanzanian,Thai,Togolese,Tongan,Trinidadian,Tristanian,Tunisian,Turkish,Turkmen,Turks and Caicos Islander,Tuvaluan,Ugandan,Ukrainian,Uruguayan,Uzbek,Vatican citizen,Citizen of Vanuatu,Venezuelan,Vietnamese,Vincentian,Wallisian,Welsh,Yemeni,Zambian,Zimbabwean";
        officerAppointmentValidator = new OfficerAppointmentValidator(logger, companyProfileService, apiEnumerations, allowedNationalities);
        //added in some fake nationalities to be used in test length validation.
       // ReflectionTestUtils.setField(officerAppointmentValidator, "allowedNationalities", "A very long nationality indeed so long in fact that it breaks the legal length for nationalities,thisIs25Characterslongggh,thisIs25Characterslongggg,thisIs16Charactz,thisIs17Character,thisIs16Characte,thisIsAVeryLongNationalityWhichWilltakeUsOver50Characterslong,Afghan,Albanian,Algerian,American,Andorran,Angolan,Anguillan,Citizen of Antigua and Barbuda,Argentine,Armenian,Australian,Austrian,Azerbaijani,Bahamian,Bahraini,Bangladeshi,Barbadian,Belarusian,Belgian,Belizean,Beninese,Bermudian,Bhutanese,Bolivian,Citizen of Bosnia and Herzegovina,Botswanan,Brazilian,British,British Virgin Islander,Bruneian,Bulgarian,Burkinan,Burmese,Burundian,Cambodian,Cameroonian,Canadian,Cape Verdean,Cayman Islander,Central African,Chadian,Chilean,Chinese,Colombian,Comoran,Congolese (Congo),Congolese (DRC),Cook Islander,Costa Rican,Croatian,Cuban,Cymraes,Cymro,Cypriot,Czech,Danish,Djiboutian,Dominican,Citizen of the Dominican Republic,Dutch,East Timorese\tEcuadorean\tEgyptian\tEmirati,English,Equatorial Guinean,Eritrean,Estonian,Ethiopian,Faroese,Fijian,Filipino,Finnish,French,Gabonese,Gambian,Georgian,German,Ghanaian,Gibraltarian,Greek,Greenlandic,Grenadian,Guamanian,Guatemalan,Citizen of Guinea-Bissau,Guinean,Guyanese,Haitian,Honduran,Hong Konger,Hungarian,Icelandic,Indian,Indonesian,Iranian,Iraqi,Irish,Israeli,Italian,Ivorian,Jamaican,Japanese,Jordanian,Kazakh,Kenyan,Kittitian,Citizen of Kiribati,Kosovan,Kuwaiti,Kyrgyz,Lao,Latvian,Lebanese,Liberian,Libyan,Liechtenstein citizen,Lithuanian,Luxembourger,Macanese,Macedonian,Malagasy,Malawian,Malaysian,Maldivian,Malian,Maltese,Marshallese,Martiniquais,Mauritanian,Mauritian,Mexican,Micronesian,Moldovan,Monegasque,Mongolian,Montenegrin,Montserratian,Moroccan,Mosotho,Mozambican,Namibian,Nauruan,Nepalese,New Zealander,Nicaraguan,Nigerian,Nigerien,Niuean,North Korean,Northern Irish,Norwegian,Omani,Pakistani,Palauan,Palestinian,Panamanian,Papua New Guinean,Paraguayan,Peruvian,Pitcairn Islander,Polish,Portuguese,Prydeinig,Puerto Rican,Qatari,Romanian,Russian,Rwandan,Salvadorean,Sammarinese,Samoan,Sao Tomean,Saudi Arabian,Scottish,Senegalese,Serbian,Citizen of Seychelles,Sierra Leonean,Singaporean,Slovak,Slovenian,Solomon Islander,Somali,South African,South Korean,South Sudanese,Spanish,Sri Lankan,St Helenian,St Lucian,Stateless,Sudanese,Surinamese,Swazi,Swedish,Swiss,Syrian,Taiwanese,Tajik,Tanzanian,Thai,Togolese,Tongan,Trinidadian,Tristanian,Tunisian,Turkish,Turkmen,Turks and Caicos Islander,Tuvaluan,Ugandan,Ukrainian,Uruguayan,Uzbek,Vatican citizen,Citizen of Vanuatu,Venezuelan,Vietnamese,Vincentian,Wallisian,Welsh,Yemeni,Zambian,Zimbabwean");
        apiErrorsList = new ArrayList<>();
    }

    @Test
    void validationWhenValid() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfile.getType()).thenReturn(COMPANY_TYPE);

        when(companyProfileService.getCompanyProfile(transaction.getId(), COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("No validation errors should have been raised")
                .isEmpty();
    }

    @Test
    void validateWhenTransactionCompanyNumberNull() {
        when(transaction.getCompanyNumber()).thenReturn(null);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Fail-early validation error should occur if transaction contains a null company number")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("The company number cannot be null or blank");
    }

    @Test
    void validateWhenTransactionCompanyNumberBlank() {
        when(transaction.getCompanyNumber()).thenReturn(" ");
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Fail-early validation error should occur if transaction contains a blank company number")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("The company number cannot be null or blank");
    }

    @Test
    void validationWhenCompanyProfileServiceUnavailable() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(apiEnumerations.getValidation(ValidationEnum.SERVICE_UNAVAILABLE)).thenReturn("Sorry, this service is unavailable. You will be able to use the service later");
        when(companyProfileService.getCompanyProfile(transaction.getId(), COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenThrow(
            new ServiceUnavailableException());
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
            .as("An error should be produced when the Company Profile Service is unavailable")
            .hasSize(1)
            .extracting(ApiError::getError)
            .contains("Sorry, this service is unavailable. You will be able to use the service later");
    }

    @Test
    void validationWhenCompanyNotFound() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfileService.getCompanyProfile(transaction.getId(), COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenThrow(
            new CompanyProfileServiceException("Error Retrieving company"));
        when(apiEnumerations.getValidation(ValidationEnum.CANNOT_FIND_COMPANY)).thenReturn("We cannot find the company");
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
            .as("An error should be produced when a Company cannot be found")
            .hasSize(1)
            .extracting(ApiError::getError)
            .contains("We cannot find the company");
    }

    @Test
    void validateCompanyNotDissolvedWhenDissolvedDateExists() {
        when(companyProfile.getCompanyStatus()).thenReturn("active");
        when(companyProfile.getDateOfCessation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 4));
        when(apiEnumerations.getValidation(ValidationEnum.COMPANY_DISSOLVED)).thenReturn("You cannot remove a director from a company that has been dissolved or is in the process of being dissolved");
        officerAppointmentValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when dissolved date exists")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You cannot remove a director from a company that has been dissolved or is in the process of being dissolved");
    }

    @Test
    void validateCompanyNotDissolvedWhenStatusIsDissolved() {
        when(companyProfile.getCompanyStatus()).thenReturn("dissolved");
        when(apiEnumerations.getValidation(ValidationEnum.COMPANY_DISSOLVED)).thenReturn("You cannot remove a director from a company that has been dissolved or is in the process of being dissolved");
        officerAppointmentValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when the company has a status of 'dissolved'")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You cannot remove a director from a company that has been dissolved or is in the process of being dissolved");
    }

    @Test
    void validateCompanyNotDissolvedWhenValid() {
        when(companyProfile.getCompanyStatus()).thenReturn("active");
        officerAppointmentValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should not be produced when the company is active")
                .isEmpty();
    }

    @Test
    void validateCompanyNotDissolvedWhenCompanyStatusNull() {
        when(companyProfile.getCompanyStatus()).thenReturn(null);
        officerAppointmentValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when company status is null")
                .isEmpty();
    }

    @Test
    void validateAllowedCompanyTypeWhenValid() {
        when(companyProfile.getType()).thenReturn("ltd");
        officerAppointmentValidator.validateAllowedCompanyType(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should not be produced when the company is of a valid type")
                .isEmpty();
    }

    @Test
    void validateAllowedCompanyTypeWhenInvalid() {
        when(companyProfile.getType()).thenReturn("invalid-type");
        when(apiEnumerations.getValidation(ValidationEnum.COMPANY_TYPE_NOT_PERMITTED, "Invalid Company Type")).thenReturn("Invalid Company Type not permitted");
        when(apiEnumerations.getCompanyType("invalid-type")).thenReturn("Invalid Company Type");
        officerAppointmentValidator.validateAllowedCompanyType(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when the company does not have a valid type")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Invalid Company Type not permitted");
    }

    @Test
    void validateAllowedCompanyTypeWhenNull() {
        when(companyProfile.getType()).thenReturn(null);
        officerAppointmentValidator.validateAllowedCompanyType(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when companyType is null")
                .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"corporate-director", "corporate-nominee-director", "director", "nominee-director"})
    void validateOfficerRoleWhenValid(String officerRole) {
        when(companyAppointment.getOfficerRole()).thenReturn(officerRole);
        officerAppointmentValidator.validateOfficerRole(request, apiErrorsList, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should not be produced when officer role is of a valid type")
                .isEmpty();
    }

    @Test
    void validateOfficerRoleWhenInvalid() {
        when(companyAppointment.getOfficerRole()).thenReturn("invalid-role");
        when(apiEnumerations.getValidation(ValidationEnum.OFFICER_ROLE)).thenReturn("You can only remove directors");
        officerAppointmentValidator.validateOfficerRole(request, apiErrorsList, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should be produced when officer role is not a valid type")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You can only remove directors");
    }

    @Test
    void validateOfficerRoleWhenNull() {
        when(companyAppointment.getOfficerRole()).thenReturn(null);
        officerAppointmentValidator.validateOfficerRole(request, apiErrorsList, companyAppointment);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when officerRole is null")
                .isEmpty();
    }

    @Test
    void validateWhenMissingFirstName() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("French");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_BLANK)).thenReturn(
                "Enter the director’s full first name");

        var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when first name is missing")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter the director’s full first name");

        when(dto.getFirstName()).thenReturn("");
        apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when first name is missing")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter the director’s full first name");
    }

    @Test
    void validateWhenMissingLastName() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(apiEnumerations.getValidation(ValidationEnum.LAST_NAME_BLANK)).thenReturn(
                "Enter the director’s last name");

        var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when last name is missing")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter the director’s last name");

        when(dto.getLastName()).thenReturn("");
        apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when last name is missing")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter the director’s last name");
    }

    @Test
    void validateFirstNameLength() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("JohnJohnJohnJohnJohnJohnJohnJohnJohnJohnJohnJohnJohn");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_LENGTH)).thenReturn(
                "First name can be no longer than 50 characters");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when first name is over 50 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("First name can be no longer than 50 characters");
    }

    @Test
    void validateLastNameLength() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("SmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmith");
        when(apiEnumerations.getValidation(ValidationEnum.LAST_NAME_LENGTH)).thenReturn(
                "Last name can be no longer than 160 characters");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when last name is over 160 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Last name can be no longer than 160 characters");
    }

    @Test
    void validateMiddleNameLength() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getMiddleNames()).thenReturn("DoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoe");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(apiEnumerations.getValidation(ValidationEnum.MIDDLE_NAME_LENGTH)).thenReturn(
                "Middle name or names can be no longer than 50 characters");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when middle name is over 50 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Middle name or names can be no longer than 50 characters");
    }

    @Test
    void validateTitleLength() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getMiddleNames()).thenReturn("Doe");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getTitle()).thenReturn("MrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMr");
        when(dto.getFormerNames()).thenReturn("Anton,Doe");
        when(dto.getNationality1()).thenReturn("British");

        when(apiEnumerations.getValidation(ValidationEnum.TITLE_LENGTH)).thenReturn(
                "Title can be no longer than 50 characters");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when title is over 50 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Title can be no longer than 50 characters");
    }

    @Test
    void validateFormerNameLength() {
        String formerNames = "JamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJamesJames,Francis";
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getMiddleNames()).thenReturn("Doe");
        when(dto.getTitle()).thenReturn("Mr");
        when(dto.getFormerNames()).thenReturn(formerNames);
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(apiEnumerations.getValidation(ValidationEnum.FORMER_NAMES_LENGTH)).thenReturn(
                "Previous names can be no longer than 160 characters");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when former names are over 160 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Previous names can be no longer than 160 characters");
    }

    @Test
    void validateFirstNameCharacters() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("Johnゃ");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_CHARACTERS)).thenReturn(
                "First name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when first name contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("First name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateLastNameCharacters() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smithゃ");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(apiEnumerations.getValidation(ValidationEnum.LAST_NAME_CHARACTERS)).thenReturn(
                "Last name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when last name contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Last name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateMiddleNameCharacters() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getMiddleNames()).thenReturn("Doeゃ");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(apiEnumerations.getValidation(ValidationEnum.MIDDLE_NAME_CHARACTERS)).thenReturn(
                "Middle name or names must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when middle name contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Middle name or names must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateTitleCharacters() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getMiddleNames()).thenReturn("Doe");
        when(dto.getTitle()).thenReturn("Mrゃ");
        when(dto.getFormerNames()).thenReturn("Anton,Doe");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        when(apiEnumerations.getValidation(ValidationEnum.TITLE_CHARACTERS)).thenReturn(
                "Title must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when title contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Title must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateFormerNameCharacters() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getMiddleNames()).thenReturn("Doe");
        when(dto.getTitle()).thenReturn("Mr");
        when(dto.getFormerNames()).thenReturn("Anton,Doeゃ");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(apiEnumerations.getValidation(ValidationEnum.FORMER_NAMES_CHARACTERS)).thenReturn(
                "Previous name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when former names forename contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Previous name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validationWhenUnderage() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(apiEnumerations.getValidation(ValidationEnum.DATE_OF_BIRTH_UNDERAGE)).thenReturn(
                "You can only appoint a person as a director if they are at least 16 years old");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(LocalDate.now().getYear()-15, 1, 1));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when a director's date of birth is under 16")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You can only appoint a person as a director if they are at least 16 years old");
    }

    @Test
    void validationWhenOverage() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(apiEnumerations.getValidation(ValidationEnum.DATE_OF_BIRTH_OVERAGE)).thenReturn(
                "You can only appoint a person as a director if they are under 110 years old");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(LocalDate.now().getYear()-110, 1, 1));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when a director's date of birth is over 110")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You can only appoint a person as a director if they are under 110 years old");
    }

    @Test
    void validationWhenMissingAge() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(apiEnumerations.getValidation(ValidationEnum.DATE_OF_BIRTH_BLANK)).thenReturn(
                "Enter the director’s date of birth");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when a director's date of birth is missing")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter the director’s date of birth");
    }

    @Test
    void validateOccupationCharacters() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getOccupation()).thenReturn("Engineerゃ");
        when(dto.getNationality1()).thenReturn("British");

        when(apiEnumerations.getValidation(ValidationEnum.OCCUPATION_CHARACTERS)).thenReturn(
                "Occupation must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when occupation contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Occupation must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateOccupationLength() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getOccupation()).thenReturn("EngineerEngineerEngineerEngineerEngineerEngineerEngineerEngineerEngineerEngineerEngineerEngineerEngineer");

        when(apiEnumerations.getValidation(ValidationEnum.OCCUPATION_LENGTH)).thenReturn(
                "Occupation must be 100 characters or less");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when occupation contains more than 100 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Occupation must be 100 characters or less");
    }

    @Test
    void validateNationality1Length() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("A very long nationality indeed so long in fact that it breaks the legal length for nationalities");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH)).thenReturn(
                "Nationality must be 50 characters or less");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when nationality contains more than 50 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Nationality must be 50 characters or less");
    }

    @Test
    void validateNationality1PlusNationality2Length() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getNationality2()).thenReturn("A very long nationality indeed so long in fact that it breaks the legal length for nationalities");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH49)).thenReturn(
                "For technical reasons, we are currently unable to accept dual nationalities with a total of more than 49 characters including commas");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("For technical reasons, we are currently unable to accept dual nationalities with a total of more than 49 characters including commas")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("For technical reasons, we are currently unable to accept dual nationalities with a total of more than 49 characters including commas");
    }
    @Test
    void validateNationality1PlusNationality2Equals50ButhasCommaToMakeIt51Length() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("thisIs25Characterslongggg");
        when(dto.getNationality2()).thenReturn("thisIs25Characterslongggh");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH49)).thenReturn(
                "For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 49 characters including commas");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 49 characters including commas")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 49 characters including commas");
    }

    @Test
    void validateNationality1PlusNationality2PlusNationality3Equals49ButhasCommasToMakeIt51Length() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("thisIs16Characte");
        when(dto.getNationality2()).thenReturn("thisIs17Character");
        when(dto.getNationality3()).thenReturn("thisIs16Charactz");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH48)).thenReturn(
                "For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 48 characters including commas");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 48 characters including commas")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 48 characters including commas");
    }
    @Test
    void validateNationality1PlusNationality2PlusNationality3GreaterThan50Length() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getNationality2()).thenReturn("French");
        when(dto.getNationality3()).thenReturn("thisIsAVeryLongNationalityWhichWilltakeUsOver50Characterslong");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH48)).thenReturn(
                "For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 48 characters including commas");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 48 characters including commas")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 48 characters including commas");
    }

    @Test
    void validateNationality1FromAllowedList() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("Britishhhh");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        when(apiEnumerations.getValidation(ValidationEnum.INVALID_NATIONALITY)).thenReturn(
                "Select a nationality from the list");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Select a nationality from the list")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Select a nationality from the list");
    }
    @Test
    void validateNationality2FromAllowedList() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("French");
        when(dto.getNationality2()).thenReturn("Britishhhh");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        when(apiEnumerations.getValidation(ValidationEnum.INVALID_NATIONALITY)).thenReturn(
                "Select a nationality from the list");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Select a nationality from the list")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Select a nationality from the list");
    }
    @Test
    void validateNationality3FromAllowedList() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("French");
        when(dto.getNationality2()).thenReturn("German");
        when(dto.getNationality3()).thenReturn("Britishhhh");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        when(apiEnumerations.getValidation(ValidationEnum.INVALID_NATIONALITY)).thenReturn(
                "Select a nationality from the list");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Select a nationality from the list")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Select a nationality from the list");
    }

    @Test
    void validateNationality1NotBlank() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));


        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_BLANK)).thenReturn(
                "Enter the director’s nationality");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Enter the director’s nationality")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter the director’s nationality");
    }
    @Test
    void validateNationality1And2NotDuplicates() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getNationality2()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));


        when(apiEnumerations.getValidation(ValidationEnum.DUPLICATE_NATIONALITY2)).thenReturn(
                "Enter a different second nationality");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Enter a different second nationality")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a different second nationality");
    }
    @Test
    void validateNationality1And3NotDuplicates() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getNationality2()).thenReturn("French");
        when(dto.getNationality3()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));


        when(apiEnumerations.getValidation(ValidationEnum.DUPLICATE_NATIONALITY3)).thenReturn(
                "Enter a different third nationality");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Enter a different third nationality")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a different third nationality");
    }
    @Test
    void validateNationality2And3NotDuplicates() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("French");
        when(dto.getNationality2()).thenReturn("British");
        when(dto.getNationality3()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));


        when(apiEnumerations.getValidation(ValidationEnum.DUPLICATE_NATIONALITY3)).thenReturn(
                "Enter a different third nationality");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Enter a different third nationality")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a different third nationality");
    }
    @Test
    void validateWhenMissingAppointmentDate() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("French");
        when(apiEnumerations.getValidation(ValidationEnum.APPOINTMENT_DATE_MISSING)).thenReturn(
                "Enter the day the director was appointed");

        var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when appointment date is missing")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter the day the director was appointed");
    }

    @Test
    void validateAppointmentDatePastOrPresentWhenFuture() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .appointedOn(LocalDate.now().plusDays(1))
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.APPOINTMENT_DATE_IN_PAST)).thenReturn("Enter a date that is today or in the past");
        officerAppointmentValidator.validateAppointmentPastOrPresent(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when appointment date is in the future")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a date that is today or in the past");
    }

    @Test
    void validateAppointmentDatePastOrPresentWhenPresent() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .appointedOn(LocalDate.now())
                .build();
        officerAppointmentValidator.validateAppointmentPastOrPresent(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should not be produced when appointment date is in the present")
                .isEmpty();
    }

    @Test
    void validateAppointmentDatePastOrPresentWhenPast() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .appointedOn(LocalDate.now().minusDays(1))
                .build();
        officerAppointmentValidator.validateAppointmentPastOrPresent(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should not be produced when appointment date is in the past")
                .isEmpty();
    }

    @Test
    void validateAppointmentDateAfterIncorporationDateWhenValid() {
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 4));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .appointedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerAppointmentValidator.validateAppointmentDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should not be produced when appointment date is after incorporation date")
                .isEmpty();
    }

    @Test
    void validateAppointmentDateAfterIncorporationDateWhenInvalid() {
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 6));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .appointedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.APPOINTMENT_DATE_AFTER_INCORPORATION_DATE)).thenReturn("The date you enter must be after the company's incorporation date");
        officerAppointmentValidator.validateAppointmentDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when appointment date is before incorporation date")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("The date you enter must be after the company's incorporation date");
    }

    @Test
    void validateAppointmentDateAfterIncorporationDateWhenSameDay() {
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 5));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .appointedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerAppointmentValidator.validateAppointmentDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should not be produced when appointment date is the incorporation date")
                .isEmpty();
    }

    @Test
    void validateAppointmentDateAfterIncorporationDateWhenCreationDateNull() {
        when(companyProfile.getDateOfCreation()).thenReturn(null);
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .appointedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerAppointmentValidator.validateAppointmentDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when incorporation date is null")
                .isEmpty();
    }

}