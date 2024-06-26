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
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.officerfiling.api.model.dto.AddressDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileServiceImpl;
import uk.gov.companieshouse.officerfiling.api.service.TransactionServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfficerAppointmentValidatorTest {
    private static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String TRANS_ID = "12345-54321-76666";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final String ETAG = "etag";
    private static final String COMPANY_TYPE = "ltd";
    private static final String OFFICER_ROLE = "director";
    private static final AddressDto validResidentialAddress = AddressDto.builder().premises("9")
            .addressLine1("Road").locality("Margate").country("France").build();
    private static final AddressDto validCorrespondenceAddressOutOfUK = AddressDto.builder().premises("61")
            .addressLine1("EU Road").locality("EU Town").country("France").build();
    private static final AddressDto validCorrespondenceAddressInUK = AddressDto.builder().premises("51")
            .addressLine1("UK Road").locality("UK Town").country("England").postalCode("AB12 3CD").build();
    private static final String ALLOWED_NATIONALITIES = "A very long nationality indeed so long in fact that it breaks the legal length for nationalities,thisIs25Characterslongggh,thisIs25Characterslongggg,thisIs16Charactz,thisIs17Character,thisIs16Characte,thisIsAVeryLongNationalityWhichWilltakeUsOver50Characterslong,Afghan,Albanian,Algerian,American,Andorran,Angolan,Anguillan,Citizen of Antigua and Barbuda,Argentine,Armenian,Australian,Austrian,Azerbaijani,Bahamian,Bahraini,Bangladeshi,Barbadian,Belarusian,Belgian,Belizean,Beninese,Bermudian,Bhutanese,Bolivian,Citizen of Bosnia and Herzegovina,Botswanan,Brazilian,British,British Virgin Islander,Bruneian,Bulgarian,Burkinan,Burmese,Burundian,Cambodian,Cameroonian,Canadian,Cape Verdean,Cayman Islander,Central African,Chadian,Chilean,Chinese,Colombian,Comoran,Congolese (Congo),Congolese (DRC),Cook Islander,Costa Rican,Croatian,Cuban,Cymraes,Cymro,Cypriot,Czech,Danish,Djiboutian,Dominican,Citizen of the Dominican Republic,Dutch,East Timorese\tEcuadorean\tEgyptian\tEmirati,English,Equatorial Guinean,Eritrean,Estonian,Ethiopian,Faroese,Fijian,Filipino,Finnish,French,Gabonese,Gambian,Georgian,German,Ghanaian,Gibraltarian,Greek,Greenlandic,Grenadian,Guamanian,Guatemalan,Citizen of Guinea-Bissau,Guinean,Guyanese,Haitian,Honduran,Hong Konger,Hungarian,Icelandic,Indian,Indonesian,Iranian,Iraqi,Irish,Israeli,Italian,Ivorian,Jamaican,Japanese,Jordanian,Kazakh,Kenyan,Kittitian,Citizen of Kiribati,Kosovan,Kuwaiti,Kyrgyz,Lao,Latvian,Lebanese,Liberian,Libyan,Liechtenstein citizen,Lithuanian,Luxembourger,Macanese,Macedonian,Malagasy,Malawian,Malaysian,Maldivian,Malian,Maltese,Marshallese,Martiniquais,Mauritanian,Mauritian,Mexican,Micronesian,Moldovan,Monegasque,Mongolian,Montenegrin,Montserratian,Moroccan,Mosotho,Mozambican,Namibian,Nauruan,Nepalese,New Zealander,Nicaraguan,Nigerian,Nigerien,Niuean,North Korean,Northern Irish,Norwegian,Omani,Pakistani,Palauan,Palestinian,Panamanian,Papua New Guinean,Paraguayan,Peruvian,Pitcairn Islander,Polish,Portuguese,Prydeinig,Puerto Rican,Qatari,Romanian,Russian,Rwandan,Salvadorean,Sammarinese,Samoan,Sao Tomean,Saudi Arabian,Scottish,Senegalese,Serbian,Citizen of Seychelles,Sierra Leonean,Singaporean,Slovak,Slovenian,Solomon Islander,Somali,South African,South Korean,South Sudanese,Spanish,Sri Lankan,St Helenian,St Lucian,Stateless,Sudanese,Surinamese,Swazi,Swedish,Swiss,Syrian,Taiwanese,Tajik,Tanzanian,Thai,Togolese,Tongan,Trinidadian,Tristanian,Tunisian,Turkish,Turkmen,Turks and Caicos Islander,Tuvaluan,Ugandan,Ukrainian,Uruguayan,Uzbek,Vatican citizen,Citizen of Vanuatu,Venezuelan,Vietnamese,Vincentian,Wallisian,Welsh,Yemeni,Zambian,Zimbabwean";

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
        officerAppointmentValidator = new OfficerAppointmentValidator(logger, companyProfileService, apiEnumerations, ALLOWED_NATIONALITIES, List.of("England", "Wales", "Scotland", "Northern Ireland", "France"), List.of("England", "Wales", "Scotland", "Northern Ireland"));
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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getDirectorAppliedToProtectDetails()).thenReturn(false);
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(apiEnumerations.getValidation(ValidationEnum.COMPANY_DISSOLVED)).thenReturn("You cannot add, remove or update a director from a company that has been dissolved or is in the process of being dissolved");
        officerAppointmentValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when dissolved date exists")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You cannot add, remove or update a director from a company that has been dissolved or is in the process of being dissolved");
    }

    @Test
    void validateCompanyNotDissolvedWhenStatusIsDissolved() {
        when(companyProfile.getCompanyStatus()).thenReturn("dissolved");
        when(apiEnumerations.getValidation(ValidationEnum.COMPANY_DISSOLVED)).thenReturn("You cannot add, remove or update a director from a company that has been dissolved or is in the process of being dissolved");
        officerAppointmentValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when the company has a status of 'dissolved'")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You cannot add, remove or update a director from a company that has been dissolved or is in the process of being dissolved");
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
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_BLANK)).thenReturn(
                "Enter the director’s full first name");
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(apiEnumerations.getValidation(ValidationEnum.LAST_NAME_BLANK)).thenReturn(
                "Enter the director’s last name");
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_LENGTH)).thenReturn(
                "First name can be no longer than 50 characters");
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(apiEnumerations.getValidation(ValidationEnum.MIDDLE_NAME_LENGTH)).thenReturn(
                "Middle name or names can be no longer than 50 characters");
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getConsentToAct()).thenReturn(true);
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
        when(apiEnumerations.getValidation(ValidationEnum.APPOINTMENT_DATE_UNDERAGE)).thenReturn(
                "You can only appoint a person as a director if they are at least 16 years old on their appointment date");
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(LocalDate.now().getYear() - 15, 1, 1));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getConsentToAct()).thenReturn(true);

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when a director's date of birth is under 16")
                .hasSize(2)
                .extracting(ApiError::getError)
                .contains("You can only appoint a person as a director if they are at least 16 years old")
                .contains("You can only appoint a person as a director if they are at least 16 years old on their appointment date");
    }

    @Test
    void validationWhenOverage() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(apiEnumerations.getValidation(ValidationEnum.DATE_OF_BIRTH_OVERAGE)).thenReturn(
                "You can only appoint a person as a director if they are under 110 years old");
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(LocalDate.now().getYear() - 110, 1, 1));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getNationality1()).thenReturn("BRITISH");
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getConsentToAct()).thenReturn(true);
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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    void validateNationality1Length(String nationality) {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("A very long nationality indeed so long in fact that it breaks the legal length for nationalities");
        when(dto.getNationality2()).thenReturn(nationality);
        when(dto.getNationality3()).thenReturn(nationality);
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    void validateNationality1PlusNationality2Length(String nationality3) {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getNationality2()).thenReturn("A very long nationality indeed so long in fact that it breaks the legal length for nationalities");
        when(dto.getNationality3()).thenReturn(nationality3);
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH49)).thenReturn(
                "For technical reasons, we are currently unable to accept dual nationalities with a total of more than 49 characters");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("For technical reasons, we are currently unable to accept dual nationalities with a total of more than 49 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("For technical reasons, we are currently unable to accept dual nationalities with a total of more than 49 characters");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    void validateNationality1PlusNationality2Equals50ButhasCommaToMakeIt51Length(String nationality3) {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("thisIs25Characterslongggg");
        when(dto.getNationality2()).thenReturn("thisIs25Characterslongggh");
        when(dto.getNationality3()).thenReturn(nationality3);
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH49)).thenReturn(
                "For technical reasons, we are currently unable to accept dual nationalities with a total of more than 49 characters");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("For technical reasons, we are currently unable to accept dual nationalities with a total of more than 49 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("For technical reasons, we are currently unable to accept dual nationalities with a total of more than 49 characters");
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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH48)).thenReturn(
                "For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 48 characters");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 48 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 48 characters");
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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

        when(apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH48)).thenReturn(
                "For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 48 characters");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 48 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("For technical reasons, we are currently unable to accept multiple nationalities with a total of more than 48 characters");
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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    void validateMissingNationalityNotDuplicates(String nationality) {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("French");
        when(dto.getNationality2()).thenReturn(nationality);
        when(dto.getNationality3()).thenReturn(nationality);
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("No validation errors should have been raised")
                .isEmpty();
    }

    @Test
    void validationWhenProtectedDetailsMissing() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getOccupation()).thenReturn("Engineer");
        when(dto.getNationality1()).thenReturn("French");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getDirectorAppliedToProtectDetails()).thenReturn(null);
        when(dto.getConsentToAct()).thenReturn(true);

        when(apiEnumerations.getValidation(ValidationEnum.PROTECTED_DETAILS_MISSING)).thenReturn(
                "Confirm if the director has ever applied to protect their details at Companies House");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when director applied to protect details is missing")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Confirm if the director has ever applied to protect their details at Companies House");
    }

    @Test
    void validationWhenConsentToActMissing() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(null);

        when(apiEnumerations.getValidation(ValidationEnum.CONSENT_TO_ACT_MISSING)).thenReturn(
                "Confirm that by submitting this information, the person named has consented to act as director");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when consent to act is missing")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Confirm that by submitting this information, the person named has consented to act as director");
    }

    @Test
    void validationWhenConsentToActFalse() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(false);

        when(apiEnumerations.getValidation(ValidationEnum.CONSENT_TO_ACT_FALSE)).thenReturn(
                "You will not be able to continue if the person named has not consented to act as director. Confirm consent to continue.");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when consent to act is false")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You will not be able to continue if the person named has not consented to act as director. Confirm consent to continue.");
    }

    @Test
    void validationWhenMissingResidentialAddress() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(null);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_BLANK)).thenReturn(
                "Enter a property name or number");
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_BLANK)).thenReturn(
                "Enter an address");
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_BLANK)).thenReturn(
                "Enter a city or town");
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_BLANK)).thenReturn(
                "Enter a postcode or ZIP");
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_BLANK)).thenReturn(
                "Enter a country");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_PREMISES_BLANK)).thenReturn(
                "Enter a property name or number");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_ONE_BLANK)).thenReturn(
                "Enter an address");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_LOCALITY_BLANK)).thenReturn(
                "Enter a city or town");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_BLANK)).thenReturn(
                "Enter a postcode or ZIP");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_BLANK)).thenReturn(
                "Enter a country");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Errors for mandatory fields should be produced when residential address is missing")
                .hasSize(5)
                .extracting(ApiError::getError)
                .contains("Enter a property name or number")
                .contains("Enter an address")
                .contains("Enter a city or town")
                .contains("Enter a postcode or ZIP")
                .contains("Enter a country");
    }

    @Test
    void validationWhenMissingResidentialPremises() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).premises(null).build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_BLANK)).thenReturn(
                "Enter a property name or number");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when premsies is blank")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a property name or number");
    }

    @Test
    void validateResidentialPremisesCharacters() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).premises("ゃ").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_CHARACTERS)).thenReturn(
                "Property name or number must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when premises contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Property name or number must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateResidentialPremisesLength() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).premises("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_LENGTH)).thenReturn(
                "Property name or number must be 200 characters or less");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when premises is over 200 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Property name or number must be 200 characters or less");
    }

    @Test
    void validationWhenMissingResidentialAddressLineOne() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).addressLine1(null).build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_BLANK)).thenReturn(
                "Enter an address");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when address line one is blank")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter an address");
    }

    @Test
    void validateResidentialAddressLineOneCharacters() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).addressLine1("ゃ").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_CHARACTERS)).thenReturn(
                "Address line 1 must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when address line 1 contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Address line 1 must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateResidentialAddressLineOneLength() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).addressLine1("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_LENGTH)).thenReturn(
                "Address line 1 must be 50 characters or less");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when address line 1 is over 50 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Address line 1 must be 50 characters or less");
    }

    @Test
    void validateResidentialAddressLineTwoCharacters() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).addressLine2("ゃ").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_TWO_CHARACTERS)).thenReturn(
                "Address line 2 must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when address line 2 contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Address line 2 must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateResidentialAddressLineTwoLength() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).addressLine2("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_TWO_LENGTH)).thenReturn(
                "Address line 2 must be 50 characters or less");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when address line 2 is over 50 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Address line 2 must be 50 characters or less");
    }

    @Test
    void validationWhenMissingResidentialLocality() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).locality(null).build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_BLANK)).thenReturn(
                "Enter a city or town");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when locality is blank")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a city or town");
    }

    @Test
    void validateResidentialLocalityCharacters() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).locality("ゃ").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_CHARACTERS)).thenReturn(
                "City or town must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when locality contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("City or town must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateResidentialLocalityLength() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).locality("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_LENGTH)).thenReturn(
                "City or town must be 50 characters or less");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when locality is over 50 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("City or town must be 50 characters or less");
    }

    @Test
    void validateResidentialRegionCharacters() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).region("ゃ").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_REGION_CHARACTERS)).thenReturn(
                "County, state, province or region must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when region contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("County, state, province or region must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateResidentialRegionLength() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).region("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_REGION_LENGTH)).thenReturn(
                "County, state, province or region must be 50 characters or less");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when region is over 50 characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("County, state, province or region must be 50 characters or less");
    }

    @Test
    void validationWhenMissingResidentialCountry() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).country(null).postalCode(null).build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_BLANK)).thenReturn(
                "Enter a postcode or ZIP");
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_BLANK)).thenReturn(
                "Enter a country");
        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when country is blank")
                .hasSize(2)
                .extracting(ApiError::getError)
                .contains("Enter a postcode or ZIP")
                .contains("Enter a country");
    }

    @Test
    void validateResidentialCountryCharacters() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).country("ゃ").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_CHARACTERS)).thenReturn(
                "Country must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_INVALID)).thenReturn(
                "Select a country from the list");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when country contains illegal characters, and it should be noted as an invalid country")
                .hasSize(2)
                .extracting(ApiError::getError)
                .contains("Country must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateResidentialCountryLength() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).country("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_INVALID)).thenReturn(
                "Select a country from the list");
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_LENGTH)).thenReturn(
                "Country must be 50 characters or less");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when country is over 50 characters, and it should be noted as an invalid country")
                .hasSize(2)
                .extracting(ApiError::getError)
                .contains("Country must be 50 characters or less")
                .contains("Select a country from the list");
    }

    @Test
    void validationWhenUKMissingResidentialPostalCode() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).country("England").postalCode(null).build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_BLANK)).thenReturn(
                "Enter a postcode or ZIP");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when postal code is blank for a UK country")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a postcode or ZIP");
    }

    @Test
    void validateResidentialPostalCodeCharacters() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).country("England").postalCode("ゃ").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressOutOfUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_CHARACTERS)).thenReturn(
                "Postal code must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTCODE_UK_INVALID)).thenReturn(
                "Enter a UK postcode. If the address is outside the UK, enter the address manually");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when Postal code contains illegal characters")
                .hasSize(2)
                .extracting(ApiError::getError)
                .contains("Postal code must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes")
                .contains("Enter a UK postcode. If the address is outside the UK, enter the address manually");
    }

    @Test
    void validateResidentialPostalCodeLength() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).postalCode("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_LENGTH)).thenReturn(
                "Postal code must be 20 characters or less");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when country is over 50 characters, and it should be noted as an invalid country")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Postal code must be 20 characters or less");
    }

    @Test
    void validateResidentialPostalCodeNoCountry() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).country(null).postalCode("ST631LJ").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_BLANK)).thenReturn(
                "Select a country from the list");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when a postcode is submitted without a country")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Select a country from the list");
    }

    @Test
    void validateResidentialPostalCodeWhenUkAndInvalidFormat() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).country("England").postalCode("S 12").build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTCODE_UK_INVALID)).thenReturn(
                "Enter a UK postcode. If the address is outside the UK, enter the address manually");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when a UK postcode is submitted that does not match the expected format")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a UK postcode. If the address is outside the UK, enter the address manually");
    }

    @Test
    void validateWhenMissingAppointmentDate() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("French");
        when(dto.getConsentToAct()).thenReturn(true);
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(apiEnumerations.getValidation(ValidationEnum.APPOINTMENT_DATE_MISSING)).thenReturn(
                "Enter the date the director was appointed");

        var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when appointment date is missing")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter the date the director was appointed");
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
        officerAppointmentValidator.validateAppointmentDateBeforeIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile);
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
        officerAppointmentValidator.validateAppointmentDateBeforeIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile);
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
        officerAppointmentValidator.validateAppointmentDateBeforeIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile);
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
        officerAppointmentValidator.validateAppointmentDateBeforeIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when incorporation date is null")
                .isEmpty();
    }

    @Test
    void validateAppointmentDateAfterIncorporationDateWhenAppointedOnNull() {
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2001, Month.JANUARY, 5));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .appointedOn(null)
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.APPOINTMENT_DATE_MISSING)).thenReturn(
                "Enter the date the director was appointed");
        officerAppointmentValidator.validateAppointmentDateBeforeIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when appointment date is missing")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter the date the director was appointed");
    }

    @Test
    void validateDirectorAgeAtAppointmentWhenValidAge() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .appointedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .dateOfBirth(LocalDate.of(1995, 1, 25))
                .build();

        officerAppointmentValidator.validateDirectorAgeAtAppointment(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should not be produced when directer is a valid age on appointment date")
                .isEmpty();
    }

    @Test
    void validateDirectorAgeAtAppointmentWhenUnderage() {
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .appointedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .dateOfBirth(LocalDate.of(2020, 1, 25))
                .build();

        when(apiEnumerations.getValidation(ValidationEnum.APPOINTMENT_DATE_UNDERAGE)).thenReturn("You can only appoint a person as a director if they are at least 16 years old on their appointment date");
        officerAppointmentValidator.validateDirectorAgeAtAppointment(request, apiErrorsList, officerFilingDto);
        assertThat(apiErrorsList)
                .as("An error should be produced when directer is underage on appointment date")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You can only appoint a person as a director if they are at least 16 years old on their appointment date");
    }

    @Test
    void validateDirectorAgeAtAppointmentWhenOverage() {
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .appointedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .dateOfBirth(LocalDate.of(1900, 1, 25))
                .build();

        when(apiEnumerations.getValidation(ValidationEnum.DATE_OF_BIRTH_OVERAGE)).thenReturn("You can only appoint a person as a director if they are under 110 years old");
        officerAppointmentValidator.validateDirectorAgeAtAppointment(request, apiErrorsList, officerFilingDto);
        assertThat(apiErrorsList)
                .as("An error should be produced when directer is underage on appointment date")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You can only appoint a person as a director if they are under 110 years old");
    }

    @Test
    void validationWhenResidentialAddressIsNonUKWithNullPostcode() {
        setupDefaultParamaters();
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).postalCode(null).build());
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        //there should NOT be a validation error
        assertThat(apiErrors.getErrors())
                .as("An error should not be produced when postal code is blank for a non UK country")
                .isEmpty();
    }

    //unit tests validating the corresponding address for the Officer Filing DTO.
    private void setupDefaultParamaters() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getConsentToAct()).thenReturn(true);
        lenient().when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
    }

    @Test
    void validationWhenCorrespondenceAddressDtoObjectIsNull() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(null);
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_PREMISES_BLANK)).thenReturn(
                "Enter a property name or number");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_ONE_BLANK)).thenReturn(
                "Enter an address");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_LOCALITY_BLANK)).thenReturn(
                "Enter a city or town");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_BLANK)).thenReturn(
                "Enter a country");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_BLANK)).thenReturn(
                "Enter a postcode or ZIP");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Errors for mandatory fields should be produced when correspondence address is missing")
                .hasSize(5)
                .extracting(ApiError::getError)
                .contains("Enter a property name or number")
                .contains("Enter an address")
                .contains("Enter a city or town")
                .contains("Enter a postcode or ZIP")
                .contains("Enter a country");
    }

    @Test
    void validationWhenCorrespondenceAddressMandatoryFieldsAreNull() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressOutOfUK)
                .premises(null)
                .addressLine1(null)
                .locality(null)
                .country(null)
                .postalCode(null)
                .build());
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_PREMISES_BLANK)).thenReturn(
                "Enter a property name or number");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_ONE_BLANK)).thenReturn(
                "Enter an address");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_LOCALITY_BLANK)).thenReturn(
                "Enter a city or town");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_BLANK)).thenReturn(
                "Enter a postcode or ZIP");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_BLANK)).thenReturn(
                "Enter a country");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Errors for mandatory fields should be produced when correspondence address is missing")
                .hasSize(5)
                .extracting(ApiError::getError)
                .contains("Enter a property name or number")
                .contains("Enter an address")
                .contains("Enter a city or town")
                .contains("Enter a postcode or ZIP")
                .contains("Enter a country");
    }

    @Test
    void validationWhenCorrespondenceAddressMandatoryFieldsAreBlank() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressOutOfUK)
                .premises("")
                .addressLine1("")
                .locality("")
                .country("")
                .postalCode("")
                .build());
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_PREMISES_BLANK)).thenReturn(
                "Enter a property name or number");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_ONE_BLANK)).thenReturn(
                "Enter an address");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_LOCALITY_BLANK)).thenReturn(
                "Enter a city or town");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_BLANK)).thenReturn(
                "Enter a postcode or ZIP");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_BLANK)).thenReturn(
                "Enter a country");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Errors for mandatory fields should be produced when correspondence address is missing")
                .hasSize(5)
                .extracting(ApiError::getError)
                .contains("Enter a property name or number")
                .contains("Enter an address")
                .contains("Enter a city or town")
                .contains("Enter a postcode or ZIP")
                .contains("Enter a country");
    }

    @Test
    void validationWhenCorrespondenceAddressInUKWithNullPostcode() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressInUK).postalCode(null).build());
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_BLANK)).thenReturn(
                "Enter a postcode or ZIP");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when postal code is blank for a UK country")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a postcode or ZIP");
    }

    @Test
    void validationWhenCorrespondenceAddressIsNonUKWithNullPostcode() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressOutOfUK).postalCode(null).build());
        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        //there should NOT be a validation error
        assertThat(apiErrors.getErrors())
                .as("An error should not be produced when postal code is blank for a non UK country")
                .isEmpty();
    }

    @Test
    void validateWhenCorrespondencePostalCodeNoCountry() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressOutOfUK).country(null).postalCode("AB12 3CD").build());
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_BLANK)).thenReturn(
                "Select a country from the list");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when a postcode is submitted without a country")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Select a country from the list");
    }

    @Test
    void validateCorrespondencePostalCodeCharacters() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressInUK).postalCode("§§§").build());
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_CHARACTERS)).thenReturn(
                "Postal code must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTCODE_UK_INVALID)).thenReturn(
                "Enter a UK postcode. If the address is outside the UK, enter the address manually");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when Postal code contains illegal characters")
                .hasSize(2)
                .extracting(ApiError::getError)
                .contains("Postal code must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes")
                .contains("Enter a UK postcode. If the address is outside the UK, enter the address manually");
    }

    @Test
    void validateWhenUkCorrespondencePostalCodeLowerCase() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressInUK).postalCode("ab12 3cd").build());

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("No errors should be produced when validating a real uk postcode that contains lowercase letters")
                .isEmpty();
    }

    @Test
    void validateCorrespondencePostalCodeWhenUkAndInvalidFormat() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressInUK)
                .postalCode("S 12")
                .build());
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTCODE_UK_INVALID)).thenReturn(
                "Enter a UK postcode. If the address is outside the UK, enter the address manually");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when a UK postcode is submitted that does not match the expected format")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a UK postcode. If the address is outside the UK, enter the address manually");
    }

    @Test
    void validateWhenCorrespondenceAddressLengthFieldsAreOverLimit() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressOutOfUK)
                .premises("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111")
                .addressLine1("long address line 1 long address line 1 long address line 1 long address line 1 long address line 1 long address line 1 long address line 1 long address line 1")
                .addressLine2("long address line 2 long address line 2 long address line 2 long address line 2 long address line 2 long address line 2 long address line 2 long address line 2")
                .locality("long locality long locality long locality long locality long locality long locality long locality long locality long locality long locality long locality long locality")
                .region("long region long region long region long region long region long region long region long region long region long region long region long region long region long region")
                .postalCode("LONGPOSTCODE LONGPOSTCODE LONGPOSTCODE LONGPOSTCODE LONGPOSTCODE")
                .build());
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_PREMISES_LENGTH)).thenReturn(
                "Property name or number must be 200 characters or less");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_ONE_LENGTH)).thenReturn(
                "Address line 1 must be 50 characters or less");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_TWO_LENGTH)).thenReturn(
                "Address line 2 must be 50 characters or less");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_LOCALITY_LENGTH)).thenReturn(
                "City or town must be 50 characters or less");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_REGION_LENGTH)).thenReturn(
                "County, state, province or region must be 50 characters or less");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_LENGTH)).thenReturn(
                "Postal code must be 20 characters or less");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when fields are over 50 characters")
                .hasSize(6)
                .extracting(ApiError::getError)
                .contains("Property name or number must be 200 characters or less")
                .contains("Address line 1 must be 50 characters or less")
                .contains("Address line 2 must be 50 characters or less")
                .contains("City or town must be 50 characters or less")
                .contains("County, state, province or region must be 50 characters or less")
                .contains("Postal code must be 20 characters or less");
    }

    @Test
    void validateWhenCorrespondenceAddressFieldsContainInvalidCharacters() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressInUK)
                .premises("ゃ")
                .addressLine1("ゃ")
                .addressLine2("ゃ")
                .locality("ゃ")
                .region("ゃ")
                .country("ゃ")
                .postalCode("ゃ")
                .build());
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_PREMISES_CHARACTERS)).thenReturn(
                "Property name or number must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_ONE_CHARACTERS)).thenReturn(
                "Address line 1 must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_TWO_CHARACTERS)).thenReturn(
                "Address line 2 must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_LOCALITY_CHARACTERS)).thenReturn(
                "Locality must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_REGION_CHARACTERS)).thenReturn(
                "Region must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_INVALID)).thenReturn(
                "Select a country from the list");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_CHARACTERS)).thenReturn(
                "Country must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_CHARACTERS)).thenReturn(
                "Postal code must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when fields contains illegal characters")
                .hasSize(8)
                .extracting(ApiError::getError)
                .contains("Property name or number must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes")
                .contains("Address line 1 must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes")
                .contains("Address line 2 must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes")
                .contains("Locality must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes")
                .contains("Region must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes")
                .contains("Select a country from the list")
                .contains("Country must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes")
                .contains("Postal code must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validationWhenCorrespondenceWithNullPostCodeAndCountry() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressInUK)
                .country(null)
                .postalCode(null)
                .build());

        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_BLANK)).thenReturn(
                "Enter a country");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_BLANK)).thenReturn(
                "Enter a postcode or ZIP");
        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Errors for mandatory fields should be produced when correspondence address is missing")
                .hasSize(2)
                .extracting(ApiError::getError)
                .contains("Enter a postcode or ZIP")
                .contains("Enter a country");
    }

    @Test
    void validationWhenCorrespondenceWithUKCountryAndNoPostCode() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressInUK)
                .postalCode(null)
                .build());

        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_BLANK)).thenReturn(
                "Enter a postcode or ZIP");
        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Errors for mandatory fields should be produced when correspondence address is missing")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a postcode or ZIP");
    }

    @Test
    void validationWhenCorrespondenceCountryIsNullButValidPostcode() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressInUK)
                .country(null)
                .build());

        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_BLANK)).thenReturn(
                "Select a country from the list");
        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Errors for mandatory fields should be produced when correspondence address is missing")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Select a country from the list");
    }

    @Test
    void validationWhenBothAddressFlagAreNotSentOrNullValues() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getIsServiceAddressSameAsRegisteredOfficeAddress()).thenReturn(null);
        when(dto.getIsHomeAddressSameAsServiceAddress()).thenReturn(null);

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        // validating null values to avoid unnecessary stubbing errors when running the tests.
        assertThat(dto.getIsServiceAddressSameAsRegisteredOfficeAddress()).isNull();
        assertThat(dto.getIsHomeAddressSameAsServiceAddress()).isNull();
        assertThat(apiErrors.getErrors())
                .as("No Errors when both address flags sent as null values")
                .isEmpty();
    }

    @Test
    void validationWhenOneAddressFlagIsSetAsTrueAndOtherIsNull() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);
        when(dto.getIsServiceAddressSameAsRegisteredOfficeAddress()).thenReturn(null);
        when(dto.getIsHomeAddressSameAsServiceAddress()).thenReturn(true);

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("No Errors when one flags sent as null values and other is sent as true")
                .isEmpty();
    }

    @Test
    void validationWhenBothAddressFlagAreSetAsTrue() {
        setupDefaultParamaters();
        when(dto.getIsServiceAddressSameAsRegisteredOfficeAddress()).thenReturn(true);
        when(dto.getIsHomeAddressSameAsServiceAddress()).thenReturn(true);

        when(apiEnumerations.getValidation(ValidationEnum.ADDRESS_LINKS_MULTIPLE_FLAGS)).thenReturn(
                "The maximum number of address links that can be established is one");
        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Errors when both address flags sent as true")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("The maximum number of address links that can be established is one");
    }

    @Test
    void validationShouldSkipHomeAddressValidationIfHomeSameAsCorrespondenceFlagSet() {
        setupDefaultParamaters();
        when(dto.getServiceAddress()).thenReturn(validCorrespondenceAddressInUK);

        when(dto.getIsServiceAddressSameAsRegisteredOfficeAddress()).thenReturn(false);
        when(dto.getIsHomeAddressSameAsServiceAddress()).thenReturn(true);

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("No errors for an invalid home address when HomeSameAsCorrespondenceFlag set")
                .isEmpty();
    }

    @Test
    void validationShouldSkipCorrespondenceAddressValidationIfCorrespondenceSameAsROAFlagSet() {
        setupDefaultParamaters();
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

        when(dto.getIsServiceAddressSameAsRegisteredOfficeAddress()).thenReturn(true);
        when(dto.getIsHomeAddressSameAsServiceAddress()).thenReturn(false);

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("No errors for an invalid correspondence address when ServiceAddressSameAsROAFlag set")
                .isEmpty();
        verify(dto, never()).getServiceAddress();
    }

    @Test
    void validationWithCaseInsensitivityForResidentialAndCorrespondenceCountry() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).country("FRANCE").build());
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressInUK).country("enGlaNd").build());
        when(dto.getConsentToAct()).thenReturn(true);
        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Error should not occur when country is passed in different cases to environment variables")
                .isEmpty();
    }

    /**
     * @see uk.gov.companieshouse.officerfiling.api.error.ApiErrors#ApiErrors()
     * return a HashSet of errors which removes the duplicate errors from the list of Errors, hence the size is expected to be 1.
     */
    @Test
    void validationWhenUKCountryCaseInsensitiveWithMissingResidentialAndOrCorrespondencePostalCode() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(LocalDate.of(1993, 1, 25));
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));
        when(dto.getNationality1()).thenReturn("British");
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).country("ENGLAND").postalCode(null).build());
        when(dto.getServiceAddress()).thenReturn(AddressDto.builder(validCorrespondenceAddressInUK).country("sCotLanD").postalCode(null).build());
        when(dto.getConsentToAct()).thenReturn(true);
        when(apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_BLANK)).thenReturn(
                "Enter a postcode or ZIP");
        when(apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_BLANK)).thenReturn(
                "Enter a postcode or ZIP");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when postal code is blank for a UK country")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a postcode or ZIP");
    }

}