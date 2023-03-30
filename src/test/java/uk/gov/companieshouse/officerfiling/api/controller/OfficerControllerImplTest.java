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
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.model.entity.ActiveOfficerDetails;
import uk.gov.companieshouse.officerfiling.api.service.OfficerService;

@ExtendWith(MockitoExtension.class)
class OfficerControllerImplTest {

    private static final String TRANS_ID = "117524-754816-491724";
    @Mock
    private Logger logger;
    @Mock
    private Transaction transaction;
    @Mock
    private OfficerService officerService;
    @Mock
    private HttpServletRequest request;
    private OfficerController testService;

    @BeforeEach
    void setUp() {
        testService = new OfficerControllerImpl(officerService, logger);
        transaction.setId(TRANS_ID);
    }

    @Test
    void getListOfActiveDirectorsDetailsDetails() throws OfficerServiceException {
        var officers = Arrays.asList(new ActiveOfficerDetails(), new ActiveOfficerDetails());
        when(officerService.getListActiveDirectorsDetails(request, transaction.getCompanyNumber())).thenReturn(officers);
        var response = testService.getListActiveDirectorsDetails(transaction, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getListOfActiveDirectorsDetailsDetailsThrowsExceptionWhenNotFound() throws OfficerServiceException {
        when(officerService.getListActiveDirectorsDetails(request, transaction.getCompanyNumber())).thenThrow(OfficerServiceException.class);
        var response = testService.getListActiveDirectorsDetails(transaction, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}