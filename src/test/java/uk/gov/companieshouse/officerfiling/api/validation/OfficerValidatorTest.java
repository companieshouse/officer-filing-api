package uk.gov.companieshouse.officerfiling.api.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfficerValidatorTest {

    private OfficerValidator officerValidator;
    private List<ApiError> apiErrorsList;
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";

    @Mock
    private HttpServletRequest request;
    @Mock
    private Logger logger;
    @Mock
    private CompanyProfileServiceImpl companyProfileService;
    @Mock
    private CompanyProfileApi companyProfile;
    @Mock
    private CompanyAppointmentService companyAppointmentService;
    @Mock
    private ApiEnumerations apiEnumerations;
    @Mock
    private OfficerFilingDto dto;
    @Mock
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        final String allowedNationalities = "A very long nationality indeed so long in fact that it breaks the legal length for nationalities,thisIs25Characterslongggh,thisIs25Characterslongggg,thisIs16Charactz,thisIs17Character,thisIs16Characte,thisIsAVeryLongNationalityWhichWilltakeUsOver50Characterslong,Afghan,Albanian,Algerian,American,Andorran,Angolan,Anguillan,Citizen of Antigua and Barbuda,Argentine,Armenian,Australian,Austrian,Azerbaijani,Bahamian,Bahraini,Bangladeshi,Barbadian,Belarusian,Belgian,Belizean,Beninese,Bermudian,Bhutanese,Bolivian,Citizen of Bosnia and Herzegovina,Botswanan,Brazilian,British,British Virgin Islander,Bruneian,Bulgarian,Burkinan,Burmese,Burundian,Cambodian,Cameroonian,Canadian,Cape Verdean,Cayman Islander,Central African,Chadian,Chilean,Chinese,Colombian,Comoran,Congolese (Congo),Congolese (DRC),Cook Islander,Costa Rican,Croatian,Cuban,Cymraes,Cymro,Cypriot,Czech,Danish,Djiboutian,Dominican,Citizen of the Dominican Republic,Dutch,East Timorese\tEcuadorean\tEgyptian\tEmirati,English,Equatorial Guinean,Eritrean,Estonian,Ethiopian,Faroese,Fijian,Filipino,Finnish,French,Gabonese,Gambian,Georgian,German,Ghanaian,Gibraltarian,Greek,Greenlandic,Grenadian,Guamanian,Guatemalan,Citizen of Guinea-Bissau,Guinean,Guyanese,Haitian,Honduran,Hong Konger,Hungarian,Icelandic,Indian,Indonesian,Iranian,Iraqi,Irish,Israeli,Italian,Ivorian,Jamaican,Japanese,Jordanian,Kazakh,Kenyan,Kittitian,Citizen of Kiribati,Kosovan,Kuwaiti,Kyrgyz,Lao,Latvian,Lebanese,Liberian,Libyan,Liechtenstein citizen,Lithuanian,Luxembourger,Macanese,Macedonian,Malagasy,Malawian,Malaysian,Maldivian,Malian,Maltese,Marshallese,Martiniquais,Mauritanian,Mauritian,Mexican,Micronesian,Moldovan,Monegasque,Mongolian,Montenegrin,Montserratian,Moroccan,Mosotho,Mozambican,Namibian,Nauruan,Nepalese,New Zealander,Nicaraguan,Nigerian,Nigerien,Niuean,North Korean,Northern Irish,Norwegian,Omani,Pakistani,Palauan,Palestinian,Panamanian,Papua New Guinean,Paraguayan,Peruvian,Pitcairn Islander,Polish,Portuguese,Prydeinig,Puerto Rican,Qatari,Romanian,Russian,Rwandan,Salvadorean,Sammarinese,Samoan,Sao Tomean,Saudi Arabian,Scottish,Senegalese,Serbian,Citizen of Seychelles,Sierra Leonean,Singaporean,Slovak,Slovenian,Solomon Islander,Somali,South African,South Korean,South Sudanese,Spanish,Sri Lankan,St Helenian,St Lucian,Stateless,Sudanese,Surinamese,Swazi,Swedish,Swiss,Syrian,Taiwanese,Tajik,Tanzanian,Thai,Togolese,Tongan,Trinidadian,Tristanian,Tunisian,Turkish,Turkmen,Turks and Caicos Islander,Tuvaluan,Ugandan,Ukrainian,Uruguayan,Uzbek,Vatican citizen,Citizen of Vanuatu,Venezuelan,Vietnamese,Vincentian,Wallisian,Welsh,Yemeni,Zambian,Zimbabwean";
        apiErrorsList = new ArrayList<>();
        officerValidator = new OfficerValidator(logger, companyProfileService, companyAppointmentService, allowedNationalities, apiEnumerations) {
            // Anonymous subclass to directly test methods implemented in the abstract class
        };
    }

    @Test
    void testValidateRequiredTransactionFields() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        officerValidator.validateRequiredTransactionFields(request, apiErrorsList, transaction);

        assertThat(apiErrorsList)
                .as("An error should not be produced when all required transaction fields are present")
                .isEmpty();
    }

    @Test
    void testValidateRequiredTransactionFieldsWhenCompanyNumberIsNull() {
        when(transaction.getCompanyNumber()).thenReturn(null);

        officerValidator.validateRequiredTransactionFields(request, apiErrorsList, transaction);
        assertThat(apiErrorsList)
                .as("An error should be produced when company number is null")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("The company number cannot be null or blank");
    }

    @Test
    void testValidateRequiredTransactionFieldsWhenCompanyNumberIsBlank() {
        when(transaction.getCompanyNumber()).thenReturn(" ");

        officerValidator.validateRequiredTransactionFields(request, apiErrorsList, transaction);
        assertThat(apiErrorsList)
                .as("An error should be produced when company number is null")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("The company number cannot be null or blank");
    }

    @Test
    void validateTitleLength() {
        when(dto.getTitle()).thenReturn("MrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMr");
        when(apiEnumerations.getValidation(ValidationEnum.TITLE_LENGTH)).thenReturn(
                "Title can be no longer than 50 characters");

        officerValidator.validateTitle(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when title is over 50 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Title can be no longer than 50 characters");
    }

    @Test
    void validateTitleCharacters() {
        when(dto.getTitle()).thenReturn("Mrゃ");
        when(apiEnumerations.getValidation(ValidationEnum.TITLE_CHARACTERS)).thenReturn(
                "Title must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        officerValidator.validateTitle(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when title name contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Title must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateTitleNumbers() {
        when(dto.getTitle()).thenReturn("Ms123");
        when(apiEnumerations.getValidation(ValidationEnum.TITLE_CHARACTERS)).thenReturn(
                "Title must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        officerValidator.validateTitle(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when title name contains numbers")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Title must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateFirstNameBlank() {
        when(dto.getFirstName()).thenReturn("");
        when(apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_BLANK)).thenReturn(
                "Enter the director’s full first name");

        officerValidator.validateFirstName(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when first name is blank")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter the director’s full first name");
    }

    @Test
    void validateFirstNameLength() {
        when(dto.getFirstName()).thenReturn("JohnJohnJohnJohnJohnJohnJohnJohnJohnJohnJohnJohnJohn");
        when(apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_LENGTH)).thenReturn(
                "First name can be no longer than 50 characters");

        officerValidator.validateFirstName(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when first name is over 50 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("First name can be no longer than 50 characters");
    }

    @Test
    void validateFirstNameCharacters() {
        when(dto.getFirstName()).thenReturn("Johnゃ");
        when(apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_CHARACTERS)).thenReturn(
                "First name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        officerValidator.validateFirstName(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when first name contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("First name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateFirstNameNumbers() {
        when(dto.getFirstName()).thenReturn("John123");
        when(apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_CHARACTERS)).thenReturn(
                "First name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        officerValidator.validateFirstName(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when first name contains numbers")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("First name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateLastNameBlank() {
        when(dto.getLastName()).thenReturn("");
        when(apiEnumerations.getValidation(ValidationEnum.LAST_NAME_BLANK)).thenReturn(
                "Enter the director’s full last name");

        officerValidator.validateLastName(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when last name is blank")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter the director’s full last name");
    }

    @Test
    void validateLastNameLength() {
        when(dto.getLastName()).thenReturn("SmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmithSmith");
        when(apiEnumerations.getValidation(ValidationEnum.LAST_NAME_LENGTH)).thenReturn(
                "Last name can be no longer than 160 characters");

        officerValidator.validateLastName(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when last name is over 160 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Last name can be no longer than 160 characters");
    }

    @Test
    void validateLastNameCharacters() {
        when(dto.getLastName()).thenReturn("Smithゃ");
        when(apiEnumerations.getValidation(ValidationEnum.LAST_NAME_CHARACTERS)).thenReturn(
                "Last name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        officerValidator.validateLastName(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when last name contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Last name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateLastNameNumbers() {
        when(dto.getLastName()).thenReturn("Smith123");
        when(apiEnumerations.getValidation(ValidationEnum.LAST_NAME_CHARACTERS)).thenReturn(
                "Last name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        officerValidator.validateLastName(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when last name contains numbers")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Last name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateMiddleNameLength() {
        when(dto.getMiddleNames()).thenReturn("DoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoe");
        when(apiEnumerations.getValidation(ValidationEnum.MIDDLE_NAME_LENGTH)).thenReturn(
                "Middle name can be no longer than 50 characters");

        officerValidator.validateMiddleNames(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when Middle name is over 50 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Middle name can be no longer than 50 characters");
    }

    @Test
    void validateMiddleNameCharacters() {
        when(dto.getMiddleNames()).thenReturn("Doeゃ");
        when(apiEnumerations.getValidation(ValidationEnum.MIDDLE_NAME_CHARACTERS)).thenReturn(
                "Middle name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        officerValidator.validateMiddleNames(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when Middle name contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Middle name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateMiddleNameNumbers() {
        when(dto.getMiddleNames()).thenReturn("Doe123");
        when(apiEnumerations.getValidation(ValidationEnum.MIDDLE_NAME_CHARACTERS)).thenReturn(
                "Middle name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        officerValidator.validateMiddleNames(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when Middle name contains numbers")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Middle name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    void validateNationality1Blank(String nationality) {
        when(dto.getNationality1()).thenReturn(nationality);
        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_BLANK)).thenReturn(
                "Enter the director’s nationality");

        officerValidator.validateNationality1(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when nationality1 is blank")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter the director’s nationality");
    }

    @Test
    void validateNationality1IsAllowed() {
        when(dto.getNationality1()).thenReturn("Britishhhh");
        when(apiEnumerations.getValidation(ValidationEnum.INVALID_NATIONALITY)).thenReturn(
                "Select a nationality from the list");

        officerValidator.validateNationality1(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when nationality1 does not match an allowed nationality")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Select a nationality from the list");
    }

    @Test
    void validateNationality2IsAllowed() {
        when(dto.getNationality1()).thenReturn("Afghan");
        when(dto.getNationality2()).thenReturn("Britishhhh");
        when(apiEnumerations.getValidation(ValidationEnum.INVALID_NATIONALITY)).thenReturn(
                "Select a nationality from the list");

        officerValidator.validateNationality2(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when nationality2 does not match an allowed nationality")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Select a nationality from the list");
    }

    @Test
    void validateNationality2DuplicateOfNationality1() {
        when(dto.getNationality1()).thenReturn("Afghan");
        when(dto.getNationality2()).thenReturn("Afghan");
        when(apiEnumerations.getValidation(ValidationEnum.DUPLICATE_NATIONALITY2)).thenReturn(
                "Enter a different second nationality");

        officerValidator.validateNationality2(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when nationality2 is a duplicate of nationality1")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a different second nationality");
    }

    @Test
    void validateNationality3IsAllowed() {
        when(dto.getNationality3()).thenReturn("Britishhhh");
        when(apiEnumerations.getValidation(ValidationEnum.INVALID_NATIONALITY)).thenReturn(
                "Select a nationality from the list");

        officerValidator.validateNationality3(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when nationality3 does not match an allowed nationality")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Select a nationality from the list");
    }

    @Test
    void validateNationality3DuplicateOfNationality1() {
        when(dto.getNationality1()).thenReturn("Afghan");
        when(dto.getNationality3()).thenReturn("Afghan");
        when(apiEnumerations.getValidation(ValidationEnum.DUPLICATE_NATIONALITY3)).thenReturn(
                "Enter a different third nationality");

        officerValidator.validateNationality3(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when nationality3 is a duplicate of nationality1")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a different third nationality");
    }

    @Test
    void validateNationality3DuplicateOfNationality2() {
        when(dto.getNationality2()).thenReturn("Afghan");
        when(dto.getNationality3()).thenReturn("Afghan");
        when(apiEnumerations.getValidation(ValidationEnum.DUPLICATE_NATIONALITY3)).thenReturn(
                "Enter a different third nationality");

        officerValidator.validateNationality3(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when nationality3 is a duplicate of nationality1")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a different third nationality");
    }

    @Test
    void validateNationality1Length() {
        when(dto.getNationality1()).thenReturn("Abcdefghijklmnopqrstuvwxyz Abcdefghijklmnopqrstuvwxyz Abcdefghijklmnopqrstuvwxyz");
        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH)).thenReturn(
                "Nationality must be 50 characters or less");

        officerValidator.validateNationalityLength(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when nationality contains more than 50 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Nationality must be 50 characters or less");
    }

    @Test
    void validateNationality1And2LengthWhen50Exactly() {
        when(dto.getNationality1()).thenReturn("Abcdefghijklmnopqrstuvwxyz");
        when(dto.getNationality2()).thenReturn("Abcdefghijklmnopqrstuvwx");
        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH49)).thenReturn(
                "For technical reasons, we are currently unable to accept dual nationalities with a total of more than 49 characters");

        officerValidator.validateNationalityLength(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when nationality1 and 2 contain more than 49 characters between them")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("For technical reasons, we are currently unable to accept dual nationalities with a total of more than 49 characters");
    }

    @Test
    void validateNationality1And3LengthWhen50Exactly() {
        when(dto.getNationality1()).thenReturn("Abcdefghijklmnopqrstuvwxyz");
        when(dto.getNationality3()).thenReturn("Abcdefghijklmnopqrstuvwx");
        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH49)).thenReturn(
                "For technical reasons, we are currently unable to accept dual nationalities with a total of more than 49 characters");

        officerValidator.validateNationalityLength(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when nationality1 and 2 contain more than 49 characters between them")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("For technical reasons, we are currently unable to accept dual nationalities with a total of more than 49 characters");
    }

    @Test
    void validateNationality1And2LengthWhen49Exactly() {
        when(dto.getNationality1()).thenReturn("Abcdefghijklmnopqrstuvwxyz");
        when(dto.getNationality2()).thenReturn("Abcdefghijklmnopqrstuvw");

        officerValidator.validateNationalityLength(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should not be produced when nationality1 and 2 contain exactly 49 characters between them")
                .isEmpty();
    }

    @Test
    void validateNationality1And2And3LengthWhen49Exactly() {
        when(dto.getNationality1()).thenReturn("Abcdefghijklmnopqrst");
        when(dto.getNationality2()).thenReturn("Abcdefghijklmnopqrst");
        when(dto.getNationality3()).thenReturn("abcdefghi");
        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH48)).thenReturn(
                "For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 48 characters");

        officerValidator.validateNationalityLength(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when nationalities 1, 2, and 3 contain more than 48 characters between them")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 48 characters");
    }

    @Test
    void validateNationality1And2And3LengthWhen48Exactly() {
        when(dto.getNationality1()).thenReturn("Abcdefghijklmnopqrst");
        when(dto.getNationality2()).thenReturn("Abcdefghijklmnopqrst");
        when(dto.getNationality3()).thenReturn("abcdefgh");

        officerValidator.validateNationalityLength(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should not be produced when nationalities 1, 2, and 3 contain exactly 48 characters between them")
                .isEmpty();
    }

    @Test
    void validateCompanyNotDissolvedWhenCompanyActive() {
        when(companyProfile.getCompanyStatus()).thenReturn("active");

        officerValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should not be produced company status is not dissolved")
                .isEmpty();
    }

    @Test
    void validateCompanyNotDissolvedWhenCompanyStatusIsNull() {
        when(companyProfile.getCompanyStatus()).thenReturn(null);

        officerValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should not be produced company status is not dissolved")
                .isEmpty();
    }

    @Test
    void validateCompanyNotDissolvedWhenCompanyStatusIsDissolved() {
        when(companyProfile.getCompanyStatus()).thenReturn("dissolved");
        when(apiEnumerations.getValidation(ValidationEnum.COMPANY_DISSOLVED)).thenReturn(
                "You cannot add or remove a director from a company that has been dissolved or is in the process of being dissolved");

        officerValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should not be produced company status is dissolved")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You cannot add or remove a director from a company that has been dissolved or is in the process of being dissolved");
    }

    @Test
    void validateCompanyNotDissolvedWhenCompanyStatusIsNotDissolvedButHasDateOfCessation() {
        when(companyProfile.getCompanyStatus()).thenReturn("active");
        when(companyProfile.getDateOfCessation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 4));
        when(apiEnumerations.getValidation(ValidationEnum.COMPANY_DISSOLVED)).thenReturn(
                "You cannot add or remove a director from a company that has been dissolved or is in the process of being dissolved");

        officerValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should not be produced company status is dissolved")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You cannot add or remove a director from a company that has been dissolved or is in the process of being dissolved");
    }

}
