package uk.gov.companieshouse.officerfiling.api.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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
import uk.gov.companieshouse.officerfiling.api.exception.CompanyAppointmentServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentServiceImpl;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileServiceImpl;
import uk.gov.companieshouse.officerfiling.api.service.TransactionServiceImpl;

@ExtendWith(MockitoExtension.class)
class OfficerTerminationValidatorTest {
    private static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String TRANS_ID = "12345-54321-76666";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final String ETAG = "etag";
    private static final String COMPANY_TYPE = "ltd";
    private static final String OFFICER_ROLE = "director";

    private OfficerTerminationValidator officerTerminationValidator;
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
    private CompanyAppointmentServiceImpl companyAppointmentService;
    @Mock
    private Transaction transaction;
    @Mock
    private CompanyProfileApi companyProfile;
    @Mock
    private AppointmentFullRecordAPI companyAppointment;
    @Mock
    private ApiEnumerations apiEnumerations;

    @BeforeEach
    void setUp() {
        officerTerminationValidator = new OfficerTerminationValidator(logger, companyProfileService, companyAppointmentService, apiEnumerations);
        apiErrorsList = new ArrayList<>();
    }

    @Test
    void validationWhenValid() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2021, 10, 3));
        when(companyProfile.getType()).thenReturn(COMPANY_TYPE);
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2021, 10, 5));
        when(companyAppointment.getEtag()).thenReturn(ETAG);
        when(companyAppointment.getOfficerRole()).thenReturn(OFFICER_ROLE);

        when(companyProfileService.getCompanyProfile(transaction.getId(), COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

        final var apiErrors = officerTerminationValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("No validation errors should have been raised")
                .isEmpty();
    }

    @Test
    void validateWhenTransactionCompanyNumberNull() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();
        when(transaction.getCompanyNumber()).thenReturn(null);

        final var apiErrors = officerTerminationValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Fail-early validation error should occur if transaction contains a null company number")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("The company number cannot be null or blank");
    }

    @Test
    void validateWhenTransactionCompanyNumberBlank() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();
        when(transaction.getCompanyNumber()).thenReturn(" ");

        final var apiErrors = officerTerminationValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Fail-early validation error should occur if transaction contains a blank company number")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("The company number cannot be null or blank");
    }

    @Test
    void validationWhenCompanyAppointmentServiceUnavailable() {
        final var dto = OfficerFilingDto.builder()
            .referenceEtag(ETAG)
            .referenceAppointmentId(FILING_ID)
            .resignedOn(LocalDate.of(2022, 9, 13))
            .build();
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(apiEnumerations.getValidation(ValidationEnum.SERVICE_UNAVAILABLE)).thenReturn("Sorry, this service is unavailable. You will be able to use the service later");
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenThrow(
            new ServiceUnavailableException());

        final var apiErrors = officerTerminationValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
            .as("An error should be produced when the Company Appointment Service is unavailable")
            .hasSize(1)
            .extracting(ApiError::getError)
            .contains("Sorry, this service is unavailable. You will be able to use the service later");
    }

    @Test
    void validationWhenCompanyProfileServiceUnavailable() {
        final var dto = OfficerFilingDto.builder()
            .referenceEtag(ETAG)
            .referenceAppointmentId(FILING_ID)
            .resignedOn(LocalDate.of(2022, 9, 13))
            .build();
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(apiEnumerations.getValidation(ValidationEnum.SERVICE_UNAVAILABLE)).thenReturn("Sorry, this service is unavailable. You will be able to use the service later");
        when(companyProfileService.getCompanyProfile(transaction.getId(), COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenThrow(
            new ServiceUnavailableException());

        final var apiErrors = officerTerminationValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
            .as("An error should be produced when the Company Profile Service is unavailable")
            .hasSize(1)
            .extracting(ApiError::getError)
            .contains("Sorry, this service is unavailable. You will be able to use the service later");
    }

    @Test
    void validateRequiredDtoFieldsWhenNullEtag() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(null)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.ETAG_BLANK)).thenReturn("ETag must be completed");

        officerTerminationValidator.validateRequiredDtoFields(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when no ETag is provided")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("ETag must be completed");
    }

    @Test
    void validateRequiredDtoFieldsWhenBlankEtag() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.ETAG_BLANK)).thenReturn("ETag must be completed");

        officerTerminationValidator.validateRequiredDtoFields(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when a blank ETag is provided")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("ETag must be completed");
    }

    @Test
    void validateRequiredDtoFieldsWhenNullTerminationDate() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(null)
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_MISSING, "Director")).thenReturn("Date Director was removed is missing");

        officerTerminationValidator.validateRequiredDtoFields(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when no Termination date is provided")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Date Director was removed is missing");
    }

    @Test
    void validateRequiredDtoFieldsWhenNullOfficerId() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId(null)
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.OFFICER_ID_BLANK)).thenReturn("The Officer ID must be completed");

        officerTerminationValidator.validateRequiredDtoFields(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when no Officer ID is provided")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("The Officer ID must be completed");
    }

    @Test
    void validateRequiredDtoFieldsWhenBlankOfficerId() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId("")
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.OFFICER_ID_BLANK)).thenReturn("The Officer ID must be completed");

        officerTerminationValidator.validateRequiredDtoFields(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when a blank Officer ID is provided")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("The Officer ID must be completed");
    }

    @Test
    void validateRequiredDtoFieldsWhenMultipleNullValuesInput() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(null)
                .referenceAppointmentId(null)
                .resignedOn(null)
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.ETAG_BLANK)).thenReturn("ETag must be completed");
        when(apiEnumerations.getValidation(ValidationEnum.OFFICER_ID_BLANK)).thenReturn("The Officer ID must be completed");
        when(apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_MISSING, "Director")).thenReturn("Date Director was removed is missing");

        officerTerminationValidator.validateRequiredDtoFields(request, apiErrorsList, dto);
        assertThat(apiErrorsList)
                .as("An error should be produced when no Termination date is provided")
                .hasSize(3)
                .extracting(ApiError::getError)
                .contains("ETag must be completed")
                .contains("The Officer ID must be completed")
                .contains("Date Director was removed is missing");
    }

    @Test
    void validationWhenOfficerNotIdentified() {
        final var dto = OfficerFilingDto.builder()
            .referenceEtag(ETAG)
            .referenceAppointmentId(FILING_ID)
            .resignedOn(LocalDate.of(2022, 9, 13))
            .build();
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfileService.getCompanyProfile(transaction.getId(), COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenThrow(
            new CompanyAppointmentServiceException("Error Retrieving appointment"));
        when(apiEnumerations.getValidation(ValidationEnum.DIRECTOR_NOT_FOUND, "Director")).thenReturn("Director cannot be found");


        final var apiErrors = officerTerminationValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
            .as("An error should be produced when an Officer cannot be identified")
            .hasSize(1)
            .extracting(ApiError::getError)
            .contains("Director cannot be found");
    }

    @Test
    void validationWhenCompanyNotFound() {
        final var dto = OfficerFilingDto.builder()
            .referenceEtag(ETAG)
            .referenceAppointmentId(FILING_ID)
            .resignedOn(LocalDate.of(2022, 9, 13))
            .build();
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfileService.getCompanyProfile(transaction.getId(), COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenThrow(
            new CompanyProfileServiceException("Error Retrieving company"));
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER))
            .thenReturn(companyAppointment);
        when(apiEnumerations.getValidation(ValidationEnum.CANNOT_FIND_COMPANY)).thenReturn("We cannot find the company");

        final var apiErrors = officerTerminationValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
            .as("An error should be produced when a Company cannot be found")
            .hasSize(1)
            .extracting(ApiError::getError)
            .contains("We cannot find the company");
    }

    @Test
    void validationWhenOfficerIdentifiedButFilingInvalid() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(1022, 9, 13))
                .build();

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2021, 10, 5));
        when(companyAppointment.getOfficerRole()).thenReturn(OFFICER_ROLE);
        when(companyAppointment.getEtag()).thenReturn("invalid-etag");
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2021, 10, 3));
        when(companyProfile.getType()).thenReturn("invalid-type");
        when(companyProfileService.getCompanyProfile(transaction.getId(), COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

        when(apiEnumerations.getCompanyType("invalid-type")).thenReturn("Invalid Company Type");
        when(apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_APPOINTMENT_DATE, "Director")).thenReturn("Date Director was removed must be on or after the date the director was appointed");
        when(apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_INCORPORATION_DATE)).thenReturn("The date you enter must be after the company's incorporation date");
        when(apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_2009)).thenReturn("Enter a date that is on or after 1 October 2009. If the director was removed before this date, you must file form 288b instead");
        when(apiEnumerations.getValidation(ValidationEnum.COMPANY_TYPE_NOT_PERMITTED, "Invalid Company Type")).thenReturn("Invalid Company Type not permitted");
        when(apiEnumerations.getValidation(ValidationEnum.ETAG_INVALID)).thenReturn("The Director’s information was updated before you sent this submission. You will need to start again");

        final var apiErrors = officerTerminationValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Each validation error should have been raised")
                .hasSize(5)
                .extracting(ApiError::getLocationType, ApiError::getType)
                .containsOnly(tuple("json-path", "ch:validation"));
    }

    @Test
    void validateMinResignationDateWhenValid() {
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2009, Month.OCTOBER, 2))
                .build();
        officerTerminationValidator.validateMinResignationDate(request, apiErrorsList, officerFilingDto);
        assertThat(apiErrorsList)
                .as("An error should not be produced when resignation date is after 1st October 2009")
                .isEmpty();
    }

    @Test
    void validateMinResignationDateWhenInvalid() {
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2009, Month.SEPTEMBER, 30))
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_2009)).thenReturn("Enter a date that is on or after 1 October 2009. If the director was removed before this date, you must file form 288b instead");
        officerTerminationValidator.validateMinResignationDate(request, apiErrorsList, officerFilingDto);
        assertThat(apiErrorsList)
                .as("An error should be produced when resignation date is before 1st October 2009")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Enter a date that is on or after 1 October 2009. If the director was removed before this date, you must file form 288b instead");
    }

    @Test
    void validateMinResignationDateWhenSameDay() {
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2009, Month.OCTOBER, 1))
                .build();
        officerTerminationValidator.validateMinResignationDate(request, apiErrorsList, officerFilingDto);
        assertThat(apiErrorsList)
                .as("An error should not be produced when resignation date is the 1st October 2009")
                .isEmpty();
    }

    @Test
    void validateTerminationDateAfterIncorporationDateWhenValid() {
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 4));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateTerminationDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should not be produced when resignation date is after creation date")
                .isEmpty();
    }

    @Test
    void validateTerminationDateAfterIncorporationDateWhenInvalid() {
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 6));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_INCORPORATION_DATE)).thenReturn("The date you enter must be after the company's incorporation date");
        officerTerminationValidator.validateTerminationDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should be produced when resignation date is before creation date")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("The date you enter must be after the company's incorporation date");
    }

    @Test
    void validateTerminationDateAfterIncorporationDateWhenSameDay() {
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 5));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateTerminationDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should not be produced when resignation date is the creation date")
                .isEmpty();
    }

    @Test
    void validateTerminationDateAfterIncorporationDateWhenCreationDateNull() {
        when(companyProfile.getDateOfCreation()).thenReturn(null);
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateTerminationDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile, companyAppointment);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when creation date is null")
                .isEmpty();
    }

    @Test
    void validationAlreadyResigned() {
        when(companyAppointment.getResignedOn()).thenReturn(LocalDate.of(2023, Month.JANUARY, 5));
        when(companyAppointment.getForename()).thenReturn("Vhagar");
        when(companyAppointment.getSurname()).thenReturn("Dragon");
        when(apiEnumerations.getValidation(ValidationEnum.DIRECTOR_ALREADY_REMOVED, "Vhagar Dragon")).thenReturn("Vhagar Dragon has already been removed from the company");
        officerTerminationValidator.validateOfficerIsNotTerminated(request, apiErrorsList,companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should be produced when an officer has already resigned")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Vhagar Dragon has already been removed from the company");
    }

    @Test
    void validationAlreadyResignedWhenNullDirectorName() {
        when(companyAppointment.getResignedOn()).thenReturn(LocalDate.of(2023, Month.JANUARY, 5));
        when(apiEnumerations.getValidation(ValidationEnum.DIRECTOR_ALREADY_REMOVED, "Director")).thenReturn("Director has already been removed from the company");
        officerTerminationValidator.validateOfficerIsNotTerminated(request, apiErrorsList,companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should be produced when an officer has already resigned")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Director has already been removed from the company");
    }

    @Test
    void validationNotAlreadyResigned() {
        when(companyAppointment.getResignedOn()).thenReturn(null);
        officerTerminationValidator.validateOfficerIsNotTerminated(request, apiErrorsList,companyAppointment);
        assertThat(apiErrorsList)
                .as("No error should be produced if an officer has not already resigned")
                .isEmpty();
    }

    @Test
    void validateCompanyNotDissolvedWhenDissolvedDateExists() {
        when(companyProfile.getCompanyStatus()).thenReturn("active");
        when(companyProfile.getDateOfCessation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 4));
        when(apiEnumerations.getValidation(ValidationEnum.COMPANY_DISSOLVED)).thenReturn("You cannot remove a director from a company that has been dissolved or is in the process of being dissolved");
        officerTerminationValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
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
        officerTerminationValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when the company has a status of 'dissolved'")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You cannot remove a director from a company that has been dissolved or is in the process of being dissolved");
    }

    @Test
    void validateCompanyNotDissolvedWhenValid() {
        when(companyProfile.getCompanyStatus()).thenReturn("active");
        officerTerminationValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should not be produced when the company is active")
                .isEmpty();
    }

    @Test
    void validateCompanyNotDissolvedWhenCompanyStatusNull() {
        when(companyProfile.getCompanyStatus()).thenReturn(null);
        officerTerminationValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when company status is null")
                .isEmpty();
    }

    @Test
    void validateTerminationDateAfterAppointmentDateWhenValidAndPost1992() {
        when(companyAppointment.getIsPre1992Appointment()).thenReturn(false);
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2023, Month.JANUARY, 4));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateTerminationDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should not be produced when resignation date is after appointment date")
                .isEmpty();
    }

    @Test
    void validateTerminationDateAfterAppointmentDateWhenValidAndPre1992() {
        when(companyAppointment.getIsPre1992Appointment()).thenReturn(true);
        when(companyAppointment.getAppointedBefore()).thenReturn(LocalDate.of(1990, Month.JANUARY, 4));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateTerminationDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should not be produced when resignation date is after appointment date")
                .isEmpty();
    }

    @Test
    void validateTerminationDateAfterAppointmentDateWhenInvalid() {
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2023, Month.JANUARY, 6));
        when(apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_AFTER_APPOINTMENT_DATE, "Director")).thenReturn("Date Director was removed must be on or after the date the director was appointed");
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateTerminationDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should be produced when resignation date is before appointment date")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Date Director was removed must be on or after the date the director was appointed");
    }

    @Test
    void validateTerminationDateAfterAppointmentDateWhenSameDay() {
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2023, Month.JANUARY, 5));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateTerminationDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should not be produced when resignation date is the same day as appointment date")
                .isEmpty();
    }

    @Test
    void validateTerminationDateAfterAppointmentDateWhenPre1992AppointmentNull() {
        when(companyAppointment.getIsPre1992Appointment()).thenReturn(null);
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateTerminationDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when isPre1992Appointment is null")
                .isEmpty();
    }

    @Test
    void validateTerminationDateAfterAppointmentDateWhenAppointedOnNull() {
        when(companyAppointment.getIsPre1992Appointment()).thenReturn(false);
        when(companyAppointment.getAppointedOn()).thenReturn(null);
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateTerminationDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when appointedOn is null")
                .isEmpty();
    }

    @Test
    void validateTerminationDateAfterAppointmentDateWhenAppointedBeforeNull() {
        when(companyAppointment.getIsPre1992Appointment()).thenReturn(true);
        when(companyAppointment.getAppointedBefore()).thenReturn(null);
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateTerminationDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when appointedBefore is null")
                .isEmpty();
    }

    @Test
    void validateSubmissionInformationInDateWhenValid() {
        when(companyAppointment.getEtag()).thenReturn(ETAG);
        final var officerFilingDto = OfficerFilingDto.builder()
            .referenceEtag(ETAG)
            .referenceAppointmentId(FILING_ID)
            .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
            .build();
        officerTerminationValidator.validateSubmissionInformationInDate(request, officerFilingDto, companyAppointment, apiErrorsList);
        assertThat(apiErrorsList)
            .as("An error should not be produced when the referenceEtag is valid/ in date")
            .isEmpty();
    }

    @Test
    void validateSubmissionInformationInDateWhenInvalid() {
        when(companyAppointment.getEtag()).thenReturn(ETAG);
        when(apiEnumerations.getValidation(ValidationEnum.ETAG_INVALID)).thenReturn("The Director’s information was updated before you sent this submission. You will need to start again");
        final var officerFilingDto = OfficerFilingDto.builder()
            .referenceEtag("invalid_etag")
            .referenceAppointmentId(FILING_ID)
            .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
            .build();
        officerTerminationValidator.validateSubmissionInformationInDate(request, officerFilingDto, companyAppointment, apiErrorsList);
        assertThat(apiErrorsList)
            .as("An error should be produced when the referenceEtag is invalid/ out of date")
            .hasSize(1)
            .extracting(ApiError::getError)
            .contains("The Director’s information was updated before you sent this submission. You will need to start again");
    }

    @Test
    void validateSubmissionInformationInDateWhenEtagNull() {
        when(companyAppointment.getEtag()).thenReturn(null);
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag("invalid_etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateSubmissionInformationInDate(request, officerFilingDto, companyAppointment, apiErrorsList);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when referenceEtag is null")
                .isEmpty();
    }

    @Test
    void validateAllowedCompanyTypeWhenValid() {
        when(companyProfile.getType()).thenReturn("ltd");
        officerTerminationValidator.validateAllowedCompanyType(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should not be produced when the company is of a valid type")
                .isEmpty();
    }

    @Test
    void validateAllowedCompanyTypeWhenInvalid() {
        when(companyProfile.getType()).thenReturn("invalid-type");
        when(apiEnumerations.getValidation(ValidationEnum.COMPANY_TYPE_NOT_PERMITTED, "Invalid Company Type")).thenReturn("Invalid Company Type not permitted");
        when(apiEnumerations.getCompanyType("invalid-type")).thenReturn("Invalid Company Type");
        officerTerminationValidator.validateAllowedCompanyType(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when the company does not have a valid type")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Invalid Company Type not permitted");
    }

    @Test
    void validateAllowedCompanyTypeWhenNull() {
        when(companyProfile.getType()).thenReturn(null);
        officerTerminationValidator.validateAllowedCompanyType(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when companyType is null")
                .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"corporate-director", "corporate-nominee-director", "director", "nominee-director"})
    void validateOfficerRoleWhenValid(String officerRole) {
        when(companyAppointment.getOfficerRole()).thenReturn(officerRole);
        officerTerminationValidator.validateOfficerRole(request, apiErrorsList, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should not be produced when officer role is of a valid type")
                .isEmpty();
    }

    @Test
    void validateOfficerRoleWhenInvalid() {
        when(companyAppointment.getOfficerRole()).thenReturn("invalid-role");
        when(apiEnumerations.getValidation(ValidationEnum.OFFICER_ROLE)).thenReturn("You can only remove directors");
        officerTerminationValidator.validateOfficerRole(request, apiErrorsList, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should be produced when officer role is not a valid type")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You can only remove directors");
    }

    @Test
    void validateOfficerRoleWhenNull() {
        when(companyAppointment.getOfficerRole()).thenReturn(null);
        officerTerminationValidator.validateOfficerRole(request, apiErrorsList, companyAppointment);
        assertThat(apiErrorsList)
                .as("Validation should be skipped when officerRole is null")
                .isEmpty();
    }

    @Test
    void validateResignationDatePastOrPresentWhenFuture() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.now().plusDays(1))
                .build();
        when(apiEnumerations.getValidation(ValidationEnum.REMOVAL_DATE_IN_PAST, "Director")).thenReturn("Date Director was removed must be today or in the past");
        officerTerminationValidator.validateResignationDatePastOrPresent(request, apiErrorsList, dto, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should be produced when resignation date is in the future")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Date Director was removed must be today or in the past");
    }

    @Test
    void validateResignationDatePastOrPresentWhenPresent() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.now())
                .build();
        officerTerminationValidator.validateResignationDatePastOrPresent(request, apiErrorsList, dto, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should not be produced when resignation date is in the present")
                .isEmpty();
    }

    @Test
    void validateResignationDatePastOrPresentWhenPast() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag(ETAG)
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.now().minusDays(1))
                .build();
        officerTerminationValidator.validateResignationDatePastOrPresent(request, apiErrorsList, dto, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should not be produced when resignation date is in the past")
                .isEmpty();
    }

}