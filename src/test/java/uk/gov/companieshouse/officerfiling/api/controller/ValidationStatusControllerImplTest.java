package uk.gov.companieshouse.officerfiling.api.controller;

import java.time.Clock;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;
import uk.gov.companieshouse.officerfiling.api.exception.FeatureNotEnabledException;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.dto.Date3TupleDto;
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

@ExtendWith(MockitoExtension.class)
class ValidationStatusControllerImplTest {

    private static final String TRANS_ID = "117524-754816-491724";
    private static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String ETAG = "etag";
    private static final String COMPANY_TYPE = "ltd";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final String OFFICER_ROLE = "director";
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
        List<ApiError> errorList = new ArrayList<ApiError>();
        when(companyAppointmentService.getCompanyAppointment( TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(dto.getReferenceEtag()).thenReturn("etag");
        when(dto.getReferenceAppointmentId()).thenReturn(FILING_ID);
        when(dto.getResignedOn()).thenReturn(LocalDate.of(1022, 9, 13));
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2021, 10, 3));
        when(companyProfile.getType()).thenReturn("invalid-type");
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2021, 10, 5));;
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
        validationStatusControllerMocks();
        when(companyProfile.getType()).thenReturn(COMPANY_TYPE);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Smith");
        when(dto.getDateOfBirth()).thenReturn(new Date3TupleDto(25,1,1993));
        when(dto.getAppointedOn()).thenReturn(LocalDate.of(2023, 5, 14));

        final var response = testController.validate(transaction, FILING_ID, request);
        assertThat(response.getValidationStatusError(), is(nullValue()));
        assertThat(response.isValid(), is(true));
    }

    @Test
    void validateWhenFilingAP01FoundAndInvalidDataWithEtagAndNoRemoveDate() {
        ReflectionTestUtils.setField(testController, "isAp01Enabled", true);
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(officerFilingMapper.map(filing)).thenReturn(dto);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getReferenceEtag()).thenReturn("ETAG");
        when(dto.getResignedOn()).thenReturn(null);

        assertThrows(
                NotImplementedException.class, () -> testController.validate(transaction, FILING_ID, request));
    }
}