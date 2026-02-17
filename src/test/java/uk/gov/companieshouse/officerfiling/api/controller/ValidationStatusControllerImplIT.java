package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.transaction.TransactionsResourceHandler;
import uk.gov.companieshouse.api.handler.transaction.request.TransactionsGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.entity.Links;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

@Tag("web")
@WebMvcTest(controllers = ValidationStatusControllerImpl.class)
class ValidationStatusControllerImplIT {
    private static final String TRANS_ID = "4f56fdf78b357bfc";
    private static final String FILING_ID = "632c8e65105b1b4a9f0d1f5e";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = "123456";
    private static final LocalDate INCORPORATION_DATE = LocalDate.of(2010, Month.OCTOBER, 20);
    private static final LocalDate APPOINTMENT_DATE = LocalDate.of(2010, Month.OCTOBER, 30);
    private static final String DIRECTOR_NAME = "Director name";
    private static final String ETAG = "etag";
    private static final String COMPANY_TYPE = "ltd";
    private static final String OFFICER_ROLE = "director";
    @MockitoBean
    private OfficerFilingService officerFilingService;
    @MockitoBean
    private HttpServletRequest request;
    @MockitoBean
    private Logger logger;
    @MockitoBean
    private LogHelper logHelper;
    @MockitoBean
    private ApiClientService apiClientService;
    @MockitoBean
    private CompanyProfileService companyProfileService;
    @MockitoBean
    private CompanyAppointmentService companyAppointmentService;
    @Mock
    private ApiClient apiClientMock;
    @Mock
    private TransactionsResourceHandler transactionResourceHandlerMock;
    @Mock
    private TransactionsGet transactionGetMock;
    @Mock
    private ApiResponse<Transaction> apiResponse;

    private HttpHeaders httpHeaders;
    private CompanyProfileApi companyProfileApi;
    private AppointmentFullRecordAPI companyAppointment;

    @Mock
    private Clock clock;

    @Autowired
    private MockMvc mockMvc;

    private Links links;

    @BeforeEach
    void setUp() throws IOException, URIValidationException {
        links = new Links(URI.create("/transactions/"+TRANS_ID+"/officers/"+FILING_ID), null);

        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", PASSTHROUGH_HEADER);
        httpHeaders.add("ERIC-Authorised-Token-Permissions", "company_number="+COMPANY_NUMBER+" company_officers=readprotected,delete,create,update");

        Transaction transaction = new Transaction();
        transaction.setCompanyNumber(COMPANY_NUMBER);
        transaction.setId(TRANS_ID);
        transaction.setStatus(TransactionStatus.OPEN);
        companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setDateOfCreation(INCORPORATION_DATE);
        companyProfileApi.setType(COMPANY_TYPE);
        companyProfileApi.setCompanyStatus("active");
        companyAppointment = new AppointmentFullRecordAPI();
        companyAppointment.setName(DIRECTOR_NAME);
        companyAppointment.setAppointedOn(APPOINTMENT_DATE);
        companyAppointment.setIsPre1992Appointment(false);
        companyAppointment.setEtag(ETAG);
        companyAppointment.setOfficerRole(OFFICER_ROLE);

        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClientMock);
        when(apiClientMock.transactions()).thenReturn(transactionResourceHandlerMock);
        when(transactionResourceHandlerMock.get(anyString())).thenReturn(transactionGetMock);
        when(transactionGetMock.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(transaction);

    }

    @Test
    void validationStatusWhenNotFound() throws Exception {
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/transactions/{id}/officers/{filingId}/validation_status", TRANS_ID, FILING_ID, request)
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void validationStatusWhenFoundAndNoValidationErrors() throws Exception {
        var offData = new OfficerFilingData(
                "etag",
                FILING_ID,
                Instant.parse("2022-09-13T00:00:00Z"));
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData).links(links)
                .build();

        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        mockMvc.perform(get("/transactions/{id}/officers/{filingId}/validation_status", TRANS_ID, FILING_ID)
            .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.is_valid", is(true)));
    }

    @Test
    void validationStatusWhenFoundAndEtagValidationError() throws Exception {
        var offData = new OfficerFilingData(
                "invalid_etag",
                FILING_ID,
                Instant.parse("2022-09-13T00:00:00Z"));
        final var now = clock.instant();

        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData).links(links)
                .build();

        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        mockMvc.perform(get("/transactions/{id}/officers/{filingId}/validation_status", TRANS_ID, FILING_ID)
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.is_valid", is(false)))
            .andExpect(jsonPath("$.errors[0].error", containsString(
                "The Director’s information was updated before you sent this submission. You will need to start again")));
    }

    @Test
    void validationStatusWhenTransactionNotFound() throws Exception {
        var offData = new OfficerFilingData(
                "invalid_etag",
                FILING_ID,
                Instant.parse("2022-09-13T00:00:00Z"));
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();

        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClientMock);
        when(apiClientMock.transactions()).thenReturn(transactionResourceHandlerMock);
        when(transactionResourceHandlerMock.get(anyString())).thenReturn(transactionGetMock);
        when(transactionGetMock.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(null);

        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        mockMvc.perform(get("/transactions/{id}/officers/{filingId}/validation_status", TRANS_ID, FILING_ID)
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    void validationStatusWhenDate300yearsAgo() throws Exception {
        var offData = new OfficerFilingData(
                "etag",
                FILING_ID,
                Instant.parse("1722-09-13T00:00:00Z"));
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData).id("12345").links(links)
                .build();

        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID,
                PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER,
                PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        mockMvc.perform(
                        get("/transactions/{id}/officers/{filingId}/validation_status", TRANS_ID, FILING_ID)
                                .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", hasSize(3)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].location", is("$./transactions/4f56fdf78b357bfc/officers/632c8e65105b1b4a9f0d1f5e/validation_status")))
                .andExpect(jsonPath("$.errors[0].error",
                        is("The date you enter must be after the company's incorporation date")));
    }

    @Test
    void validationStatusWhenResignedOnPriorTo01092009() throws Exception {
        var offData = new OfficerFilingData(
                "etag",
                FILING_ID,
                Instant.parse("2008-09-13T00:00:00Z"));
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData).links(links)
                .build();
        companyProfileApi.setDateOfCreation(LocalDate.of(2000, 1, 1));
        companyAppointment.setAppointedOn(LocalDate.of(2008, 1, 2));
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID,
                PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER,
                PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        mockMvc.perform(
                        get("/transactions/{id}/officers/{filingId}/validation_status", TRANS_ID, FILING_ID)
                                .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].location", is("$./transactions/4f56fdf78b357bfc/officers/632c8e65105b1b4a9f0d1f5e/validation_status")))
                .andExpect(jsonPath("$.errors[0].error",
                        is("Enter a date that is on or after 1 October 2009. If the director was removed before this date, you must file form 288b instead")));
    }

    @Test
    void validationStatusWhenResignedOnBeforeIncorporationDate() throws Exception {
        var offData = new OfficerFilingData(
                "etag",
                FILING_ID,
                Instant.parse("2018-10-05T00:00:00Z"));
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData).links(links)
                .build();
        companyProfileApi.setDateOfCreation(LocalDate.of(2020, 1, 1));
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID,
                PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER,
                PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        mockMvc.perform(
                        get("/transactions/{id}/officers/{filingId}/validation_status", TRANS_ID, FILING_ID)
                                .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].location", is("$./transactions/4f56fdf78b357bfc/officers/632c8e65105b1b4a9f0d1f5e/validation_status")))
                .andExpect(jsonPath("$.errors[0].error",
                        is("The date you enter must be after the company's incorporation date")));
    }

    @Test
    void validationStatusWhenDissolvedDateExists() throws Exception {
        companyProfileApi.setDateOfCessation(LocalDate.of(2022, Month.JANUARY, 1));
        var offData = new OfficerFilingData(
                "etag",
                FILING_ID,
                Instant.parse("2022-09-13T00:00:00Z"));
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData).links(links)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID,
                PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER,
                PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        mockMvc.perform(
                        get("/transactions/{id}/officers/{filingId}/validation_status", TRANS_ID, FILING_ID)
                                .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].location", is("$./transactions/4f56fdf78b357bfc/officers/632c8e65105b1b4a9f0d1f5e/validation_status")))
                .andExpect(jsonPath("$.errors[0].error",
                        is("You cannot add, remove or update a director from a company that has been dissolved or is in the process of being dissolved")));
    }

    @Test
    void validationStatusWhenResignedOnInFuture() throws Exception {
        var offData = new OfficerFilingData(
                "etag",
                FILING_ID,
                Instant.parse("3022-09-13T00:00:00Z"));
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData).id(FILING_ID).links(links)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID,
                PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER,
                PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        mockMvc.perform(
                        get("/transactions/{id}/officers/{filingId}/validation_status", TRANS_ID, FILING_ID)
                                .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].location", is("$./transactions/4f56fdf78b357bfc/officers/632c8e65105b1b4a9f0d1f5e/validation_status")))
                .andExpect(jsonPath("$.errors[0].error",
                        is("Date Director was removed must be today or in the past")));
    }
}