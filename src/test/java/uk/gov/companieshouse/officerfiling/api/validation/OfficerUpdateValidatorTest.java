package uk.gov.companieshouse.officerfiling.api.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;
import uk.gov.companieshouse.officerfiling.api.model.dto.AddressDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfficerUpdateValidatorTest {

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

    @Mock
    private OfficerUpdateValidator officerUpdateValidator;
    private List<ApiError> apiErrorsList;

    @Mock
    private HttpServletRequest request;
    @Mock
    private Logger logger;
    @Mock
    private CompanyAppointmentService companyAppointmentService;
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
         officerUpdateValidator = new OfficerUpdateValidator(logger, companyAppointmentService, companyProfileService, apiEnumerations);
         apiErrorsList = new ArrayList<>();
    }

    @Test
    void validationWhenValid() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getDirectorsDetailsChangedDate()).thenReturn(LocalDate.now().minusDays(1));

        final var apiErrors = officerUpdateValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("No validation errors should have been raised")
                .isEmpty();
    }
    @Test
    void validateWhenMissingChangeDate() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_MISSING)).thenReturn(
                "Enter the date the director’s details changed");

        var apiErrors = officerUpdateValidator.validate(request, dto, transaction,
                PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("An error should be produced when appointment date is missing")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter the date the director’s details changed");
    }

    @Test
    void validateChangeDatePastOrPresentWhenFuture() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.now().plusDays(1))
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_IN_PAST)).thenReturn("Enter a date that is today or in the past");
        officerUpdateValidator.validateChangeDatePastOrPresent(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when appointment date is in the future")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a date that is today or in the past");
    }

    @Test
    void validateChangeDatePastOrPresentWhenPresent() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.now())
                .build();
        officerUpdateValidator.validateChangeDatePastOrPresent(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should not be produced when appointment date is in the present")
                .isEmpty();
    }

    @Test
    void validateChangeDatePastOrPresentWhenPast() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.now().minusDays(1))
                .build();
        officerUpdateValidator.validateChangeDatePastOrPresent(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should not be produced when appointment date is in the past")
                .isEmpty();
    }

    @Test
    void validateMinChangeDateWhenValid() {
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.of(2009, Month.OCTOBER, 2))
                .build();
        officerUpdateValidator.validateMinChangeDate(request, apiErrorsList, officerFilingDto);
        assertThat(apiErrorsList)
                .as("An error should not be produced when resignation date is after 1st October 2009")
                .isEmpty();
    }

    @Test
    void validateMinChangeDateWhenInvalid() {
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.of(2009, Month.SEPTEMBER, 30))
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_BEFORE_2009)).thenReturn("Date the director’s details changed must be on or after 1 October 2009.");
        officerUpdateValidator.validateMinChangeDate(request, apiErrorsList, officerFilingDto);
        assertThat(apiErrorsList)
                .as("An error should be produced when resignation date is before 1st October 2009")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Date the director’s details changed must be on or after 1 October 2009.");
    }

    @Test
    void validateChangeDateAfterIncorporationDateWhenValid() {
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 4));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerUpdateValidator.validateChangeDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should not be produced when change date is after incorporation date")
                .isEmpty();
    }

    @Test
    void validateChangeDateAfterIncorporationDateWhenInvalid() {
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 6));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_AFTER_INCORPORATION_DATE)).thenReturn("The date you enter must be after the company's incorporation date");
        officerUpdateValidator.validateChangeDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when change date is before incorporation date")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("The date you enter must be after the company's incorporation date");
    }

    @Test
    void validateChangeDateAfterIncorporationDateWhenSameDay() {
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 5));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerUpdateValidator.validateChangeDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should not be produced when change date is the incorporation date")
                .isEmpty();
    }

    @Test
    void validateChangeDateAfterAppointmentDateWhenValid() {
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2023, Month.JANUARY, 4));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerUpdateValidator.validateChangeDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should not be produced when change date is after appointment date")
                .isEmpty();
    }

    @Test
    void validateChangeDateAfterAppointmentDateWhenInvalid() {
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2023, Month.JANUARY, 6));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_BEFORE_OFFICER_APPOINTMENT_DATE)).thenReturn("Enter a date that is on or after the date the director was appointed");
        officerUpdateValidator.validateChangeDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should be produced when change date is before appointment date")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a date that is on or after the date the director was appointed");
    }

    @Test
    void validateChangeDateAndAppointmentDateWhenSameDay() {
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2023, Month.JANUARY, 5));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerUpdateValidator.validateChangeDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should not be produced when change date is same as appointment date")
                .isEmpty();
    }


    @Test
    void validateDateOfChangeWhenAppointedOnDateIsNull() {
        when(companyAppointment.getAppointedOn()).thenReturn(null);
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerUpdateValidator.validateChangeDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when appointment date is null")
                .isEmpty();
    }

    @Test
    void validateDateOfChangeWhenCompanyAppointmentHasPre1992SetToTrueWithAppointBeforeDate() {
        when(companyAppointment.getIsPre1992Appointment()).thenReturn(true);
        when(companyAppointment.getAppointedBefore()).thenReturn(LocalDate.of(1991, Month.JANUARY, 20));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.of(1990, Month.JANUARY, 5))
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_BEFORE_OFFICER_APPOINTMENT_DATE)).thenReturn("Enter a date that is on or after the date the director was appointed");
        officerUpdateValidator.validateChangeDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should be produced when change date is before appointment date - pre 1992 check")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a date that is on or after the date the director was appointed");
    }

    @Test
    void validateDateOfChangeWhenCompanyAppointmentHasPre1992SetToTrueWithoutAppointBeforeDate() {
        when(companyAppointment.getIsPre1992Appointment()).thenReturn(true);
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.of(1990, Month.JANUARY, 5))
                .build();
        officerUpdateValidator.validateChangeDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("Validation should be skipped isPre1992 set to true and has no appointment before date")
                .isEmpty();
    }


    @Test
    void validateAppointmentDateAfterIncorporationDateWhenCreationDateNull() {
        when(companyProfile.getDateOfCreation()).thenReturn(null);
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerUpdateValidator.validateChangeDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when incorporation date is null")
                .isEmpty();
    }

    @Test
    void validateChangeDateAfterIncorporationDateWhenAppointedOnNull() {
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2001, Month.JANUARY, 5));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .directorsDetailsChangedDate(null)
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_MISSING)).thenReturn(
                "Enter the date the director was updated");
        officerUpdateValidator.validateChangeDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when change date is missing")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter the date the director was updated");
    }

    @Test
    void validateCH01ValidationForDirectorNameMandatoryFieldsWhenNameHasBeenUpdatedIsTrue() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        OfficerUpdateValidator officerUpdateValidatorSpy = Mockito.spy(new OfficerUpdateValidator(logger, companyAppointmentService, companyProfileService, apiEnumerations));
    
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .nameHasBeenUpdated(true)
                .firstName("John")
                .lastName("Smith")
                .directorsDetailsChangedDate(LocalDate.of(2023, Month.JANUARY, 5))
                .build();

        final var apiErrors = officerUpdateValidatorSpy.validate(request, officerFilingDto, transaction, PASSTHROUGH_HEADER);

        //validate the methods to validate firstname and lastname are called.
        Mockito.verify(officerUpdateValidatorSpy).validateFirstName(any(), any(), any());
        Mockito.verify(officerUpdateValidatorSpy).validateLastName(any(), any(), any());

        assertThat(apiErrors.getErrors())
                .as("No validation errors should have been raised")
                .isEmpty();

    }

    @Test
    void validateCH01ValidationForDirectorNameMandatoryFieldsWhenNameHasBeenUpdatedIsNull() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        OfficerUpdateValidator officerUpdateValidatorSpy = Mockito.spy(new OfficerUpdateValidator(logger, companyAppointmentService, companyProfileService, apiEnumerations));

        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .nameHasBeenUpdated(null)
                .firstName("John")
                .lastName("Smith")
                .directorsDetailsChangedDate(LocalDate.of(2023, Month.JANUARY, 5))
                .build();

        final var apiErrors = officerUpdateValidatorSpy.validate(request, officerFilingDto, transaction, PASSTHROUGH_HEADER);

        //validate the methods to validate firstname and lastname are called.
        Mockito.verify(officerUpdateValidatorSpy).validateFirstName(any(), any(), any());
        Mockito.verify(officerUpdateValidatorSpy).validateLastName(any(), any(), any());

        assertThat(apiErrors.getErrors())
                .as("No validation errors should have been raised")
                .isEmpty();

    }


    @Test
    void validateCH01ValidationForDirectorNameOptionalFieldsWhenNameHasBeenUpdatedIsTrue() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        OfficerUpdateValidator officerUpdateValidatorSpy = Mockito.spy(new OfficerUpdateValidator(logger, companyAppointmentService, companyProfileService, apiEnumerations));

        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .nameHasBeenUpdated(true)
                .title("Excelsior")
                .firstName("John")
                .middleNames("James")
                .lastName("Doe")
                .directorsDetailsChangedDate(LocalDate.of(2023, Month.JANUARY, 5))
                .build();

        final var apiErrors = officerUpdateValidatorSpy.validate(request, officerFilingDto, transaction, PASSTHROUGH_HEADER);

        //validate the methods to validate middlename and title are called.
        Mockito.verify(officerUpdateValidatorSpy).validateTitle(any(), any(), any());
        Mockito.verify(officerUpdateValidatorSpy).validateMiddleNames(any(), any(), any());

        assertThat(apiErrors.getErrors())
                .as("No validation errors should have been raised")
                .isEmpty();

    }

    @Test
    void shouldNotValidateNameWhenNameHasBeenUpdatedIsFalseAndNoNameFieldsExistInFiling() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        OfficerUpdateValidator officerUpdateValidatorSpy = Mockito.spy(new OfficerUpdateValidator(logger, companyAppointmentService, companyProfileService, apiEnumerations));

        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .nameHasBeenUpdated(null)
                .directorsDetailsChangedDate(LocalDate.of(2023, Month.JANUARY, 5))
                .build();

        final var apiErrors = officerUpdateValidatorSpy.validate(request, officerFilingDto, transaction, PASSTHROUGH_HEADER);

        //validate the methods to validate the name are not called
        Mockito.verify(officerUpdateValidatorSpy, times(0)).validateTitle(any(), any(), any());
        Mockito.verify(officerUpdateValidatorSpy, times(0)).validateFirstName(any(), any(), any());
        Mockito.verify(officerUpdateValidatorSpy, times(0)).validateMiddleNames(any(), any(), any());
        Mockito.verify(officerUpdateValidatorSpy, times(0)).validateLastName(any(), any(), any());

        assertThat(apiErrors.getErrors())
                .as("No validation errors should have been raised")
                .isEmpty();

    }

}