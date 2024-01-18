package uk.gov.companieshouse.officerfiling.api.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfficerValidatorTest {

    private OfficerValidator officerValidator;
    private List<ApiError> apiErrorsList;

    @Mock
    private HttpServletRequest request;
    @Mock
    private Logger logger;
    @Mock
    private CompanyProfileServiceImpl companyProfileService;
    @Mock
    private CompanyAppointmentService companyAppointmentService;
    @Mock
    private ApiEnumerations apiEnumerations;
    @Mock
    private OfficerFilingDto dto;

    @BeforeEach
    void setUp() {
        officerValidator = new OfficerValidator(logger, companyProfileService, companyAppointmentService, apiEnumerations);
        apiErrorsList = new ArrayList<>();
    }

    @Test
    void validateTitleLength() {
        when(dto.getTitle()).thenReturn("MrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMrMr");
        when(apiEnumerations.getValidation(ValidationEnum.TITLE_LENGTH)).thenReturn(
                "Title can be no longer than 50 characters");

        officerValidator.validateTitle(request, apiErrorsList ,dto);
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
    void validateFirstNameBlank() {
        when(dto.getFirstName()).thenReturn("");
        when(apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_BLANK)).thenReturn(
                "Enter the director’s full first name");

        officerValidator.validateFirstName(request, apiErrorsList ,dto);
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

        officerValidator.validateFirstName(request, apiErrorsList ,dto);
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
    void validateLastNameBlank() {
        when(dto.getLastName()).thenReturn("");
        when(apiEnumerations.getValidation(ValidationEnum.LAST_NAME_BLANK)).thenReturn(
                "Enter the director’s full last name");

        officerValidator.validateLastName(request, apiErrorsList ,dto);
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

        officerValidator.validateLastName(request, apiErrorsList ,dto);
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
    void validateMiddleNameLength() {
        when(dto.getMiddleNames()).thenReturn("DoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoe");
        when(apiEnumerations.getValidation(ValidationEnum.MIDDLE_NAME_LENGTH)).thenReturn(
                "Middle name can be no longer than 50 characters");

        officerValidator.validateMiddleNames(request, apiErrorsList ,dto);
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

}
