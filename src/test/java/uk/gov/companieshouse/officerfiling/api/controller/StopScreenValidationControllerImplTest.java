package uk.gov.companieshouse.officerfiling.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
 class StopScreenValidationControllerImplTest {

    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = "123456";

    private CompanyProfileApi companyProfileApi;
    @Mock
    private CompanyProfileService companyProfileService;
    @Mock
    private HttpServletRequest request;
    private StopScreenValidationController testService;

    @BeforeEach
    void setUp() {
        testService = new StopScreenValidationControllerImpl(companyProfileService);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyStatus("active"); ;
        companyProfileApi.setDateOfCessation(null);
    }

    @Test
    void getCurrentOrFutureDissolvedReturnsFalse() throws CompanyProfileServiceException {
        getMockCompanyProfile(companyProfileApi);
        var response = testService.getCurrentOrFutureDissolved(COMPANY_NUMBER, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(false, response.getBody());
    }

    @Test
    void getCurrentOrFutureDissolvedWithDissolvedStatusReturnsTrue() throws CompanyProfileServiceException {
        companyProfileApi.setCompanyStatus("dissolved");
        getMockCompanyProfile(companyProfileApi);
        var response = testService.getCurrentOrFutureDissolved(COMPANY_NUMBER, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    void getCurrentOrFutureDissolvedWithDateOfCessationReturnsTrue() throws CompanyProfileServiceException {
        companyProfileApi.setDateOfCessation(LocalDate.of(2023, 1, 1));
        getMockCompanyProfile(companyProfileApi);
        var response = testService.getCurrentOrFutureDissolved(COMPANY_NUMBER, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    void getCurrentOrFutureDissolvedWithDissolvedStatusAndDateOfCessationReturnsTrue() throws CompanyProfileServiceException {
        companyProfileApi.setCompanyStatus("dissolved");
        companyProfileApi.setDateOfCessation(LocalDate.of(2023, 1, 1));
        getMockCompanyProfile(companyProfileApi);
        var response = testService.getCurrentOrFutureDissolved(COMPANY_NUMBER, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    void getCurrentOrFutureDissolvedThrowsExceptionWhenNotFound() throws CompanyProfileServiceException {
        when(companyProfileService.getCompanyProfile(anyString(), eq(COMPANY_NUMBER), eq(PASSTHROUGH_HEADER)))
                .thenThrow(CompanyProfileServiceException.class);
        var response = testService.getCurrentOrFutureDissolved(COMPANY_NUMBER, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    private void getMockCompanyProfile(CompanyProfileApi companyProfileApi) {
        when(companyProfileService.getCompanyProfile(anyString(), eq(COMPANY_NUMBER), eq(PASSTHROUGH_HEADER)))
                .thenReturn(companyProfileApi);
    }
}
