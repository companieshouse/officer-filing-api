package uk.gov.companieshouse.officerfiling.api.validation;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.error.ApiErrors;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyAppointmentServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.ServiceUnavailableException;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentServiceImpl;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileServiceImpl;
import uk.gov.companieshouse.officerfiling.api.service.TransactionServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfficerTerminationValidatorTest {
    private static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String TRANS_ID = "12345-54321-76666";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final String DIRECTOR_NAME = "director name";
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

    @BeforeEach
    void setUp() {
        officerTerminationValidator = new OfficerTerminationValidator(logger, transactionService, companyProfileService, companyAppointmentService);
        apiErrorsList = new ArrayList<>();
    }

    @Test
    void validationWhenValid() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();

        when(companyAppointment.getEtag()).thenReturn(ETAG);
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2021, 10, 3));
        when(companyProfile.getType()).thenReturn(COMPANY_TYPE);
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2021, 10, 5));
        when(companyAppointment.getEtag()).thenReturn("etag");
        when(companyAppointment.getOfficerRole()).thenReturn(OFFICER_ROLE);

        when(companyProfileService.getCompanyProfile(transaction.getId(), COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

        final var apiErrors = officerTerminationValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("No validation errors should have been raised")
                .isEmpty();
    }

    @Test
    void validationWhenCompanyAppointmentServiceUnavailable() {
        final var dto = OfficerFilingDto.builder()
            .referenceEtag("etag")
            .referenceAppointmentId(FILING_ID)
            .resignedOn(LocalDate.of(2022, 9, 13))
            .build();
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenThrow(
            new ServiceUnavailableException("The service is down. Try again later"));

        final var apiErrors = officerTerminationValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
            .as("An error should be produced when the Company Appointment Service is unavailable")
            .hasSize(1)
            .extracting(ApiError::getError)
            .contains("The service is down. Try again later");
    }

    @Test
    void validationWhenCompanyProfileServiceUnavailable() {
        final var dto = OfficerFilingDto.builder()
            .referenceEtag("etag")
            .referenceAppointmentId(FILING_ID)
            .resignedOn(LocalDate.of(2022, 9, 13))
            .build();
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfileService.getCompanyProfile(transaction.getId(), COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenThrow(
            new ServiceUnavailableException("The service is down. Try again later"));

        final var apiErrors = officerTerminationValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
            .as("An error should be produced when the Company Profile Service is unavailable")
            .hasSize(1)
            .extracting(ApiError::getError)
            .contains("The service is down. Try again later");
    }

    @Test
    void validationWhenOfficerNotIdentified() {
        final var dto = OfficerFilingDto.builder()
            .referenceEtag("etag")
            .referenceAppointmentId(FILING_ID)
            .resignedOn(LocalDate.of(2022, 9, 13))
            .build();
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfileService.getCompanyProfile(transaction.getId(), COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenThrow(
            new CompanyAppointmentServiceException("Error Retrieving appointment"));

        final var apiErrors = officerTerminationValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
            .as("An error should be produced when an Officer cannot be identified")
            .hasSize(1)
            .extracting(ApiError::getError)
            .contains("Officer not found. Please confirm the details and resubmit");
    }

    @Test
    void validationWhenCompanyNotFound() {
        final var dto = OfficerFilingDto.builder()
            .referenceEtag("etag")
            .referenceAppointmentId(FILING_ID)
            .resignedOn(LocalDate.of(2022, 9, 13))
            .build();
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfileService.getCompanyProfile(transaction.getId(), COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenThrow(
            new CompanyProfileServiceException("Error Retrieving company"));
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER))
            .thenReturn(companyAppointment);
        final var apiErrors = officerTerminationValidator.validate(request, dto, transaction, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
            .as("An error should be produced when a Company cannot be found")
            .hasSize(1)
            .extracting(ApiError::getError)
            .contains("Company not found. Please confirm the details and resubmit");
    }
    @Test
    void validationWhenOfficerIdentifiedButFilingInvalid() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(1022, 9, 13))
                .build();

        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2021, 10, 5));
        when(companyAppointment.getOfficerRole()).thenReturn(OFFICER_ROLE);
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2021, 10, 3));
        when(companyProfile.getType()).thenReturn("invalid-type");
        when(companyProfileService.getCompanyProfile(transaction.getId(), COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

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
                .referenceEtag("etag")
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
                .referenceEtag("etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2009, Month.SEPTEMBER, 30))
                .build();
        officerTerminationValidator.validateMinResignationDate(request, apiErrorsList, officerFilingDto);
        assertThat(apiErrorsList)
                .as("An error should be produced when resignation date is before 1st October 2009")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You have entered a date too far in the past. Please check the date and resubmit");
    }

    @Test
    void validateMinResignationDateWhenSameDay() {
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag("etag")
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
                .referenceEtag("etag")
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
        when(companyAppointment.getName()).thenReturn(DIRECTOR_NAME);
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateTerminationDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should be produced when resignation date is before creation date")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("director name has not been found");
    }

    @Test
    void validateTerminationDateAfterIncorporationDateWhenSameDay() {
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 5));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateTerminationDateAfterIncorporationDate(request, apiErrorsList, officerFilingDto, companyProfile, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should not be produced when resignation date is the creation date")
                .isEmpty();
    }

    @Test
    void validationAlreadyResigned() {
        when(companyAppointment.getResignedOn()).thenReturn(LocalDate.of(2023, Month.JANUARY, 5));
        when(companyAppointment.getName()).thenReturn("Vhagar Dragon");
        officerTerminationValidator.validateOfficerIsNotTerminated(request, apiErrorsList,companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should be produced when an officer has already resigned")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("An application to remove Vhagar Dragon has already been submitted");
    }

    @Test
    void validationNotAlreadyResigned() {
        when(companyAppointment.getResignedOn()).thenReturn(null);
        officerTerminationValidator.validateOfficerIsNotTerminated(request, apiErrorsList,companyAppointment);
        assertThat(apiErrorsList)
                .as("No error should be produced if an officer has not already resigned")
                .isEmpty();
    }

    void validateCompanyNotDissolvedWhenDissolvedDateExists() {
        when(companyProfile.getDateOfCessation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 4));
        officerTerminationValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when dissolved date exists")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You cannot remove an officer from a company that is about to be dissolved");
    }

    @Test
    void validateCompanyNotDissolvedWhenStatusIsDissolved() {
        when(companyProfile.getCompanyStatus()).thenReturn("dissolved");
        officerTerminationValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when the company has a status of 'dissolved'")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You cannot remove an officer from a company that has been dissolved");
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
    void validateTerminationDateAfterAppointmentDateWhenValid() {
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2023, Month.JANUARY, 4));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag("etag")
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
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateTerminationDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should be produced when resignation date is before appointment date")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("Date director was removed must be on or after the date the director was appointed");
    }

    @Test
    void validateTerminationDateAfterAppointmentDateWhenSameDay() {
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2023, Month.JANUARY, 5));
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2023, Month.JANUARY, 5))
                .build();
        officerTerminationValidator.validateTerminationDateAfterAppointmentDate(request, apiErrorsList, officerFilingDto, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should not be produced when resignation date is the same day as appointment date")
                .isEmpty();
    }

    @Test
    void validateSubmissionInformationInDateWhenValid() {
        when(companyAppointment.getEtag()).thenReturn(ETAG);
        final var officerFilingDto = OfficerFilingDto.builder()
            .referenceEtag("etag")
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
            .contains("The Officers information is out of date. Please start the process again and make a new submission");
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
        officerTerminationValidator.validateAllowedCompanyType(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when the company does not have a valid type")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You cannot remove an officer from a invalid-type using this service");
    }

    @Test
    void validateOfficerRoleWhenValid() {
        when(companyAppointment.getOfficerRole()).thenReturn("corporate-director");
        officerTerminationValidator.validateOfficerRole(request, apiErrorsList, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should not be produced when officer role is of a valid type")
                .isEmpty();
    }

    @Test
    void validateOfficerRoleWhenInvalid() {
        when(companyAppointment.getOfficerRole()).thenReturn("invalid-role");
        officerTerminationValidator.validateOfficerRole(request, apiErrorsList, companyAppointment);
        assertThat(apiErrorsList)
                .as("An error should be produced when officer role is not a valid type")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You can only remove directors");
    }
}