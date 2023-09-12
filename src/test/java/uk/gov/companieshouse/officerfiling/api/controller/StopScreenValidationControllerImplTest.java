package uk.gov.companieshouse.officerfiling.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.officerfiling.api.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.officerfiling.api.exception.FeatureNotEnabledException;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

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
        ReflectionTestUtils.setField(testService, "isTm01Enabled", true);
        companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyStatus("active"); ;
        companyProfileApi.setDateOfCessation(null);
    }

    @Test
    void getCurrentOrFutureDissolvedReturnsFalse() throws CompanyProfileServiceException {
        getMockCompanyProfile(companyProfileApi);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        var response = testService.getCurrentOrFutureDissolved(COMPANY_NUMBER, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(false, response.getBody());
    }

    @Test
    void getCurrentOrFutureDissolvedWithDissolvedStatusReturnsTrue() throws CompanyProfileServiceException {
        companyProfileApi.setCompanyStatus("dissolved");
        getMockCompanyProfile(companyProfileApi);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        var response = testService.getCurrentOrFutureDissolved(COMPANY_NUMBER, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    void getCurrentOrFutureDissolvedWithDateOfCessationReturnsTrue() throws CompanyProfileServiceException {
        companyProfileApi.setDateOfCessation(LocalDate.of(2023, 1, 1));
        getMockCompanyProfile(companyProfileApi);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        var response = testService.getCurrentOrFutureDissolved(COMPANY_NUMBER, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    void getCurrentOrFutureDissolvedWithDissolvedStatusAndDateOfCessationReturnsTrue() throws CompanyProfileServiceException {
        companyProfileApi.setCompanyStatus("dissolved");
        companyProfileApi.setDateOfCessation(LocalDate.of(2023, 1, 1));
        getMockCompanyProfile(companyProfileApi);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        var response = testService.getCurrentOrFutureDissolved(COMPANY_NUMBER, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    void getCurrentOrFutureDissolvedThrowsExceptionWhenNotFound() throws CompanyProfileServiceException {
        when(companyProfileService.getCompanyProfile(anyString(), eq(COMPANY_NUMBER), eq(PASSTHROUGH_HEADER)))
                .thenThrow(CompanyProfileServiceException.class);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        var response = testService.getCurrentOrFutureDissolved(COMPANY_NUMBER, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    private void getMockCompanyProfile(CompanyProfileApi companyProfileApi) {
        when(companyProfileService.getCompanyProfile(anyString(), eq(COMPANY_NUMBER), eq(PASSTHROUGH_HEADER)))
                .thenReturn(companyProfileApi);
    }

    @Test
    void checkTm01FeatureFlagDisabled(){
        ReflectionTestUtils.setField(testService, "isTm01Enabled", false);
        assertThrows(FeatureNotEnabledException.class,
            () -> testService.getCurrentOrFutureDissolved(COMPANY_NUMBER, request));
    }
}
