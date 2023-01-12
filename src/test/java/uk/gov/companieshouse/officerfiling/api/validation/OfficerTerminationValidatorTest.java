package uk.gov.companieshouse.officerfiling.api.validation;

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
import uk.gov.companieshouse.officerfiling.api.exception.CompanyAppointmentServiceException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfficerTerminationValidatorTest {
    private static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String TRANS_ID = "12345-54321-76666";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final String DIRECTOR_NAME = "director name";

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
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2021, 10, 3));
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2021, 10, 5));

        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointmentService.getCompanyAppointment(COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

        final var apiErrors = officerTerminationValidator.validate(request, dto, TRANS_ID, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("No validation errors should have been raised")
                .isEmpty();
    }

    @Test
    void validationWhenOfficerNotIdentified() {
        final var dto = OfficerFilingDto.builder()
            .referenceEtag("etag")
            .referenceAppointmentId(FILING_ID)
            .resignedOn(LocalDate.of(2022, 9, 13))
            .build();
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointmentService.getCompanyAppointment(COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenThrow(
            new CompanyAppointmentServiceException("Error Retrieving appointment"));

        final var apiErrors = officerTerminationValidator.validate(request, dto, TRANS_ID, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
            .as("An error should be produced when an Officer cannot be identified")
            .hasSize(1)
            .extracting(ApiError::getError)
            .contains("Officer not found. Please confirm the details and resubmit");
    }

    @Test
    void validationWhenOfficerIdentifiedButFilingInvalid() {
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(1022, 9, 13))
                .build();
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2021, 10, 5));

        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2021, 10, 3));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointmentService.getCompanyAppointment(COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

        final var apiErrors = officerTerminationValidator.validate(request, dto, TRANS_ID, PASSTHROUGH_HEADER);
        assertThat(apiErrors.getErrors())
                .as("Each validation error should have been raised")
                .hasSize(3)
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
    void validateCompanyNotDissolvedWhenDissolvedDateExists() {
        when(companyProfile.getDateOfCessation()).thenReturn(LocalDate.of(2023, Month.JANUARY, 4));
        officerTerminationValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when dissolved date exists")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You cannot remove a director from a company that's been dissolved");
    }

    @Test
    void validateCompanyNotDissolvedWhenStatusIsDissolved() {
        when(companyProfile.getCompanyStatus()).thenReturn("dissolved");
        officerTerminationValidator.validateCompanyNotDissolved(request, apiErrorsList, companyProfile);
        assertThat(apiErrorsList)
                .as("An error should be produced when the company has a status of 'dissolved'")
                .hasSize(1)
                .extracting(ApiError::getError)
                .contains("You cannot remove a director from a company that's been dissolved or is about to be dissolved");
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
    
}