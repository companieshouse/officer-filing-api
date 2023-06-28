package uk.gov.companieshouse.officerfiling.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.FeatureNotEnabledException;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@ExtendWith(MockitoExtension.class)
class DirectorsControllerImplTest {

  private static final String TRANS_ID = "117524-754816-491724";
  private static final String SUBMISSION_ID = "645d1188c794645afe15f5cc";
  private static final String PASSTHROUGH_HEADER = "passthrough";
  private static final String COMPANY_NUMBER = "123456";
  Instant resignedOn = Instant.parse("2021-12-03T10:15:30.00Z");
  @Mock
  private Logger logger;
  @Mock
  private OfficerService officerService;
  @Mock
  private OfficerFilingService officerFilingService;
  @Mock
  private CompanyAppointmentService companyAppointmentService;
  @Mock
  private HttpServletRequest request;
  @Mock
  Optional<OfficerFiling> officerFilingOptional;
  @Mock
  AppointmentFullRecordAPI appointmentFullRecordAPI;
  @Mock
  OfficerFiling officerFiling;
  @Mock
  OfficerServiceException serviceException;
  private DirectorsController testService;
  private Transaction transaction;


  @BeforeEach
  void setUp() {
    testService = new DirectorsControllerImpl(officerService, companyAppointmentService, officerFilingService, logger);
    ReflectionTestUtils.setField(testService, "isTm01Enabled", true);
    transaction = new Transaction();
    transaction.setId(TRANS_ID);
    transaction.setCompanyNumber(COMPANY_NUMBER);
  }

  @Test
  void getListOfActiveDirectorsDetails() throws OfficerServiceException {
    when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
    var officers = Arrays.asList(new CompanyOfficerApi(), new CompanyOfficerApi());
    when(officerService.getListOfActiveDirectorsDetails(request, TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(officers);
    var response = testService.getListActiveDirectorsDetails(transaction, request);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(officers, response.getBody());
  }

  @Test
  void getListOfActiveDirectorsDetailsThrowsExceptionWhenNotFound() throws OfficerServiceException {
    when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
    when(officerService.getListOfActiveDirectorsDetails(request, TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER))
            .thenThrow(serviceException);
    when(serviceException.getCause()).thenReturn(serviceException);
    when(serviceException.getMessage()).thenReturn("404 not found\n{}");
    var response = testService.getListActiveDirectorsDetails(transaction, request);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void getListOfActiveDirectorsDetailsThrowsExceptionWhenError() throws OfficerServiceException {
    when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
    when(officerService.getListOfActiveDirectorsDetails(request, TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER))
            .thenThrow(serviceException);
    when(serviceException.getCause()).thenReturn(serviceException);
    when(serviceException.getMessage()).thenReturn("Internal Server Error");
    var response = testService.getListActiveDirectorsDetails(transaction, request);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
  
  @Test
  void getRemoveCheckAnswersDirectorDetailsWhenFound() throws Exception {
    when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
    when(officerFilingService.get(SUBMISSION_ID, TRANS_ID)).thenReturn(officerFilingOptional);
    when(officerFilingOptional.isPresent()).thenReturn(true);
    when(officerFilingOptional.get()).thenReturn(officerFiling);
    when(officerFiling.getData().getResignedOn()).thenReturn(resignedOn);
    when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, null, PASSTHROUGH_HEADER)).thenReturn(appointmentFullRecordAPI);
    var response = testService.getRemoveCheckAnswersDirectorDetails(transaction, SUBMISSION_ID, request);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(appointmentFullRecordAPI, times(1)).setResignedOn(
            LocalDate.ofInstant(resignedOn,ZoneId.systemDefault()));
  }

  @Test
  void getRemoveCheckAnswersDirectorDetailsWhenNotFound() throws Exception {
    when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
    when(officerFilingService.get(SUBMISSION_ID, TRANS_ID)).thenReturn(officerFilingOptional);
    when(officerFilingOptional.isPresent()).thenReturn(true);
    when(officerFilingOptional.get()).thenReturn(officerFiling);
    when(officerFiling.getData().getResignedOn()).thenReturn(resignedOn);
    when(officerFiling.getData().getResignedOn()).thenReturn(null);
    var response = testService.getRemoveCheckAnswersDirectorDetails(transaction, SUBMISSION_ID, request);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    when(officerFilingOptional.isPresent()).thenReturn(false);
    response = testService.getRemoveCheckAnswersDirectorDetails(transaction, SUBMISSION_ID, request);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void checkTm01FeatureFlagDisabled() {
    ReflectionTestUtils.setField(testService, "isTm01Enabled", false);
    assertThrows(FeatureNotEnabledException.class,
        () -> testService.getListActiveDirectorsDetails(transaction, request));
  }

}