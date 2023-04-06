package uk.gov.companieshouse.officerfiling.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.service.OfficerService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@ExtendWith(MockitoExtension.class)
class DirectorsControllerImplTest {

  private static final String TRANS_ID = "117524-754816-491724";
  private static final String PASSTHROUGH_HEADER = "passthrough";
  private static final String COMPANY_NUMBER = "123456";
  @Mock
  private Logger logger;
  @Mock
  private OfficerService officerService;
  @Mock
  private HttpServletRequest request;
  private DirectorsController testService;
  private Transaction transaction;

  @BeforeEach
  void setUp() {
    testService = new DirectorsControllerImpl(officerService, logger);
    transaction = new Transaction();
    transaction.setId(TRANS_ID);
    transaction.setCompanyNumber(COMPANY_NUMBER);

    when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
  }

  @Test
  void getListOfActiveDirectorsDetails() throws OfficerServiceException {
    var officers = Arrays.asList(new CompanyOfficerApi(), new CompanyOfficerApi());
    when(officerService.getListOfActiveDirectorsDetails(request, TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(officers);
    var response = testService.getListActiveDirectorsDetails(transaction, request);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(officers, response.getBody());
  }

  @Test
  void getListOfActiveDirectorsDetailsThrowsExceptionWhenNotFound() throws OfficerServiceException {
    when(officerService.getListOfActiveDirectorsDetails(request, TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenThrow(OfficerServiceException.class);
    var response = testService.getListActiveDirectorsDetails(transaction, request);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

}