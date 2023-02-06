package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.mapper.ErrorMapper;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@ExtendWith(MockitoExtension.class)
class ValidationStatusControllerImplTest {

    public static final String TRANS_ID = "117524-754816-491724";
    public static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String ETAG = "etag";
    private static final String COMPANY_TYPE = "ltd";
    public static final String COMPANY_NUMBER = "COMPANY_NUMBER";
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
    private ErrorMapper errorMapper;
    @Mock
    private Transaction transaction;
    @Mock
    private AppointmentFullRecordAPI companyAppointment;
    @Mock
    private CompanyProfileApi companyProfile;
    @Mock
    private OfficerFilingDto dto;
    private OfficerFiling filing;
    private ValidationStatusControllerImpl testController;

    @BeforeEach
    void setUp() {
        testController = new ValidationStatusControllerImpl(officerFilingService, logger,
            transactionService, companyProfileService, companyAppointmentService, officerFilingMapper,
            errorMapper);
        filing = OfficerFiling.builder()
            .referenceAppointmentId("off-id")
            .referenceEtag("etag")
            .resignedOn(Instant.parse("2022-09-13T00:00:00Z"))
            .build();
    }

    @Test
    void validateWhenFilingFoundAndNoValidationErrors() {

        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(officerFilingMapper.map(filing)).thenReturn(dto);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getReferenceEtag()).thenReturn(ETAG);
        when(dto.getReferenceAppointmentId()).thenReturn(FILING_ID);
        when(dto.getResignedOn()).thenReturn(LocalDate.of(2009, 10, 1));
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2005, 10, 3));
        when(companyProfile.getType()).thenReturn(COMPANY_TYPE);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2007, 10, 5));
        when(companyAppointment.getEtag()).thenReturn(ETAG);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

        final var response= testController.validate(request, FILING_ID, transaction, PASSTHROUGH_HEADER);
        assertThat(response.getValidationStatusError(), is(nullValue()));
        assertThat(response.isValid(), is(true));
    }

    @Test
    void validatePublicWhenFilingFoundAndNoValidationErrors() {

        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(officerFilingMapper.map(filing)).thenReturn(dto);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(dto.getReferenceEtag()).thenReturn(ETAG);
        when(dto.getReferenceAppointmentId()).thenReturn(FILING_ID);
        when(dto.getResignedOn()).thenReturn(LocalDate.of(2009, 10, 1));
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2005, 10, 3));
        when(companyProfile.getType()).thenReturn(COMPANY_TYPE);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2007, 10, 5));
        when(companyAppointment.getEtag()).thenReturn(ETAG);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

        final var response= testController.validatePublic(transaction, FILING_ID, request);
        assertThat(response.getValidationStatusError(), is(nullValue()));
        assertThat(response.isValid(), is(true));
    }

    @Test
    void validatePrivateWhenFilingFoundAndNoValidationErrors() {

        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(officerFilingMapper.map(filing)).thenReturn(dto);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(dto.getReferenceEtag()).thenReturn(ETAG);
        when(dto.getReferenceAppointmentId()).thenReturn(FILING_ID);
        when(dto.getResignedOn()).thenReturn(LocalDate.of(2009, 10, 1));
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2005, 10, 3));
        when(companyProfile.getType()).thenReturn(COMPANY_TYPE);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2007, 10, 5));
        when(companyAppointment.getEtag()).thenReturn(ETAG);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

        final var response= testController.validatePrivate(transaction, FILING_ID, request);
        assertThat(response.getValidationStatusError(), is(nullValue()));
        assertThat(response.isValid(), is(true));
    }

    @Test
    void validateWhenFilingFoundAndValidationErrors() {

        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(officerFilingMapper.map(filing)).thenReturn(dto);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(dto.getReferenceEtag()).thenReturn("etag");
        when(dto.getReferenceAppointmentId()).thenReturn(FILING_ID);
        when(dto.getResignedOn()).thenReturn(LocalDate.of(1022, 9, 13));
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2021, 10, 3));
        when(companyProfile.getType()).thenReturn("invalid-type");
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2021, 10, 5));
        when(companyAppointment.getEtag()).thenReturn(ETAG);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

        final var response= testController.validate(request, FILING_ID, transaction, PASSTHROUGH_HEADER);
        assertThat(response.isValid(), is(false));
    }

    @Test
    void validatePublicWhenFilingFoundAndValidationErrors() {

        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(officerFilingMapper.map(filing)).thenReturn(dto);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(dto.getReferenceEtag()).thenReturn("etag");
        when(dto.getReferenceAppointmentId()).thenReturn(FILING_ID);
        when(dto.getResignedOn()).thenReturn(LocalDate.of(1022, 9, 13));
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2021, 10, 3));
        when(companyProfile.getType()).thenReturn("invalid-type");
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2021, 10, 5));
        when(companyAppointment.getEtag()).thenReturn(ETAG);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

        final var response= testController.validatePublic(transaction, FILING_ID, request);
        assertThat(response.isValid(), is(false));
    }

    @Test
    void validatePrivateWhenFilingFoundAndValidationErrors() {

        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(officerFilingMapper.map(filing)).thenReturn(dto);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(dto.getReferenceEtag()).thenReturn("etag");
        when(dto.getReferenceAppointmentId()).thenReturn(FILING_ID);
        when(dto.getResignedOn()).thenReturn(LocalDate.of(1022, 9, 13));
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2021, 10, 3));
        when(companyProfile.getType()).thenReturn("invalid-type");
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2021, 10, 5));
        when(companyAppointment.getEtag()).thenReturn(ETAG);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

        final var response= testController.validatePrivate(transaction, FILING_ID, request);
        assertThat(response.isValid(), is(false));
    }

    @Test
    void validateWhenFilingNotFound() {
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.empty());
        when(transaction.getId()).thenReturn(TRANS_ID);

        assertThrows(ResourceNotFoundException.class, () -> testController.validate(request, FILING_ID, transaction, PASSTHROUGH_HEADER));
    }

    @Test
    void validatePublicWhenFilingNotFound() {
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.empty());
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);

        assertThrows(ResourceNotFoundException.class, () -> testController.validatePublic(transaction, FILING_ID, request));
    }

    @Test
    void validatePrivateWhenFilingNotFound() {
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.empty());
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);

        assertThrows(ResourceNotFoundException.class, () -> testController.validatePrivate(transaction, FILING_ID, request));
    }


}