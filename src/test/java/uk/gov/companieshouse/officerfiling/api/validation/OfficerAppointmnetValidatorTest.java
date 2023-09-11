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
import uk.gov.companieshouse.officerfiling.api.model.dto.AddressDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.Date3TupleDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileServiceImpl;
import uk.gov.companieshouse.officerfiling.api.service.TransactionServiceImpl;

@ExtendWith(MockitoExtension.class)
class OfficerAppointmnetValidatorTest {
    private static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String TRANS_ID = "12345-54321-76666";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final String ETAG = "etag";
    private static final String COMPANY_TYPE = "ltd";
    private static final String OFFICER_ROLE = "director";
    private static final AddressDto validResidentialAddress = AddressDto.builder().premises("9")
            .addressLine1("Road").locality("Margate").country("France").build();

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
        officerAppointmentValidator = new OfficerAppointmentValidator(logger, companyProfileService,
                apiEnumerations,List.of ("England", "Wales", "Scotland", "Northern Ireland", "France"), List.of ("England", "Wales", "Scotland", "Northern Ireland"));
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_BLANK)).thenReturn(
                "Enter the director’s full first name");
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getTitle()).thenReturn("MrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMr");
        when(dto.getFormerNames()).thenReturn("Anton,Doe");
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(apiEnumerations.getValidation(ValidationEnum.FORMER_NAMES_LENGTH)).thenReturn(
                "Previous names can be no longer than 160 characters");
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_CHARACTERS)).thenReturn(
                "First name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(apiEnumerations.getValidation(ValidationEnum.LAST_NAME_CHARACTERS)).thenReturn(
                "Last name must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(apiEnumerations.getValidation(ValidationEnum.MIDDLE_NAME_CHARACTERS)).thenReturn(
                "Middle name or names must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(1,1,LocalDate.now().getYear()-15));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(1,1,LocalDate.now().getYear()-110));
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getOccupation()).thenReturn("Engineerゃ");
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getOccupation()).thenReturn("EngineerEngineerEngineerEngineerEngineerEngineerEngineerEngineerEngineerEngineerEngineerEngineerEngineer");
        when(dto.getResidentialAddress()).thenReturn(validResidentialAddress);

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
    void validationWhenMissingResidentialPremises() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).premises(null).build());
        when(apiEnumerations.getValidation(ValidationEnum.PREMISES_BLANK)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).premises("ゃ").build());
        when(apiEnumerations.getValidation(ValidationEnum.PREMISES_CHARACTERS)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).premises("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111").build());
        when(apiEnumerations.getValidation(ValidationEnum.PREMISES_LENGTH)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).addressLine1(null).build());
        when(apiEnumerations.getValidation(ValidationEnum.ADDRESS_LINE_ONE_BLANK)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).addressLine1("ゃ").build());
        when(apiEnumerations.getValidation(ValidationEnum.ADDRESS_LINE_ONE_CHARACTERS)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).addressLine1("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111").build());
        when(apiEnumerations.getValidation(ValidationEnum.ADDRESS_LINE_ONE_LENGTH)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).addressLine2("ゃ").build());
        when(apiEnumerations.getValidation(ValidationEnum.ADDRESS_LINE_TWO_CHARACTERS)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).addressLine2("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111").build());
        when(apiEnumerations.getValidation(ValidationEnum.ADDRESS_LINE_TWO_LENGTH)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).locality(null).build());
        when(apiEnumerations.getValidation(ValidationEnum.LOCALITY_BLANK)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).locality("ゃ").build());
        when(apiEnumerations.getValidation(ValidationEnum.LOCALITY_CHARACTERS)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).locality("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111").build());
        when(apiEnumerations.getValidation(ValidationEnum.LOCALITY_LENGTH)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).region("ゃ").build());
        when(apiEnumerations.getValidation(ValidationEnum.REGION_CHARACTERS)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).region("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111").build());
        when(apiEnumerations.getValidation(ValidationEnum.REGION_LENGTH)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).country(null).postalCode(null).build());
        when(apiEnumerations.getValidation(ValidationEnum.COUNTRY_BLANK)).thenReturn(
                "Enter a country");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when country is blank")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a country");
    }

    @Test
    void validateResidentialCountryCharacters() {

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);

        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).country("ゃ").build());
        when(apiEnumerations.getValidation(ValidationEnum.COUNTRY_CHARACTERS)).thenReturn(
                "Country must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
        when(apiEnumerations.getValidation(ValidationEnum.COUNTRY_INVALID)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).country("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111").build());
        when(apiEnumerations.getValidation(ValidationEnum.COUNTRY_INVALID)).thenReturn(
                "Select a country from the list");
        when(apiEnumerations.getValidation(ValidationEnum.COUNTRY_LENGTH)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).country("England").postalCode(null).build());
        when(apiEnumerations.getValidation(ValidationEnum.POSTAL_CODE_BLANK)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).postalCode("ゃ").build());
        when(apiEnumerations.getValidation(ValidationEnum.POSTAL_CODE_CHARACTERS)).thenReturn(
                "Postal code must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when Postal code contains illegal characters")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Postal code must only include letters a to z, and common special characters such as hyphens, spaces and apostrophes");
    }

    @Test
    void validateResidentialPostalCodeLength() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).postalCode("111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111").build());
        when(apiEnumerations.getValidation(ValidationEnum.POSTAL_CODE_LENGTH)).thenReturn(
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
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getResidentialAddress()).thenReturn(AddressDto.builder(validResidentialAddress).country(null).postalCode("11111").build());
        when(apiEnumerations.getValidation(ValidationEnum.POSTAL_CODE_WITHOUT_COUNTRY)).thenReturn(
                "Select a country from the list before entering a postcode");
        when(apiEnumerations.getValidation(ValidationEnum.COUNTRY_BLANK)).thenReturn(
                "Select a country from the list");

        final var apiErrors = officerAppointmentValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when a postcode is submitted without a country")
                .hasSize(2)
                .extracting(ApiError::getError)
                .contains("Select a country from the list before entering a postcode")
                .contains("Select a country from the list");;
    }




}