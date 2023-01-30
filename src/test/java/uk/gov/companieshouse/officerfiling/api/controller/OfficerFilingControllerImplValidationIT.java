package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.officerfiling.api.controller.OfficerFilingControllerImplIT.tokenPermissions;
import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
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
import uk.gov.companieshouse.officerfiling.api.exception.CompanyAppointmentServiceException;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;

@Tag("web")
@WebMvcTest(controllers = OfficerFilingControllerImpl.class)
class OfficerFilingControllerImplValidationIT {
    private static final String TRANS_ID = "4f56fdf78b357bfc";
    private static final String FILING_ID = "632c8e65105b1b4a9f0d1f5e";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String TM01_FRAGMENT = "\"reference_etag\": \"ETAG\","
            + "\"reference_appointment_id\": \"" + FILING_ID + "\","
            + "\"resigned_on\": \"2022-09-13\"";
    private static final URI REQUEST_URI = URI.create("/transactions/" + TRANS_ID + "/officers");
    private static final String COMPANY_NUMBER = "123456";
    public static final LocalDate INCORPORATION_DATE = LocalDate.of(2010, Month.OCTOBER, 20);
    public static final LocalDate APPOINTMENT_DATE = LocalDate.of(2010, Month.OCTOBER, 30);
    public static final String DIRECTOR_NAME = "Director name";
    private static final String ETAG = "ETAG";

    @MockBean
    private TransactionService transactionService;
    @MockBean
    private OfficerFilingService officerFilingService;
    @MockBean
    private CompanyProfileService companyProfileService;
    @MockBean
    private CompanyAppointmentService companyAppointmentService;
    @MockBean
    private OfficerFilingMapper filingMapper;
    @MockBean
    private Clock clock;
    @MockBean
    private Logger logger;
    @MockBean
    private ApiClientService apiClientService;

    @Mock
    private ApiClient apiClientMock;
    @Mock
    private TransactionsResourceHandler transactionResourceHandlerMock;
    @Mock
    private TransactionsGet transactionGetMock;
    @Mock
    private ApiResponse<Transaction> apiResponse;

    private HttpHeaders httpHeaders;
    private Transaction transaction;
    private CompanyProfileApi companyProfileApi;
    private AppointmentFullRecordAPI companyAppointment;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws IOException, URIValidationException {
        httpHeaders = new HttpHeaders();
        httpHeaders.add("ERIC-Access-Token", PASSTHROUGH_HEADER);

        transaction = new Transaction();
        transaction.setCompanyNumber(COMPANY_NUMBER);
        transaction.setId(TRANS_ID);
        transaction.setStatus(TransactionStatus.OPEN);
        companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setDateOfCreation(INCORPORATION_DATE);
        companyAppointment = new AppointmentFullRecordAPI();
        companyAppointment.setName(DIRECTOR_NAME);
        companyAppointment.setAppointedOn(APPOINTMENT_DATE);
        companyAppointment.setEtag(ETAG);
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClientMock);
        when(apiClientMock.transactions()).thenReturn(transactionResourceHandlerMock);
        when(transactionResourceHandlerMock.get(anyString())).thenReturn(transactionGetMock);
        when(transactionGetMock.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(transaction);
    }

    @Test
    void createFilingWhenReferenceEtagBlankThenResponse400() throws Exception {
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        final var body = "{" + TM01_FRAGMENT.replace("ETAG", "") + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .requestAttr("token_permissions",tokenPermissions)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].location", is("$.reference_etag")))
                .andExpect(jsonPath("$.errors[0].error_values", is(nullValue())))
                .andExpect(jsonPath("$.errors[0].error", is("must not be blank")));
    }

    @Test
    void createFilingWhenReferenceReferenceAppointmentIdBlankThenResponse400() throws Exception {
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        final var body = "{" + TM01_FRAGMENT.replace(FILING_ID, "") + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                .requestAttr("token_permissions",tokenPermissions)
                .contentType("application/json")
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
            .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
            .andExpect(jsonPath("$.errors[0].location", is("$.reference_appointment_id")))
            .andExpect(jsonPath("$.errors[0].error_values", is(nullValue())))
            .andExpect(jsonPath("$.errors[0].error", is("must not be blank")));
    }

    @Test
    void createFilingWhenReferenceResignedOnBlankThenResponse400() throws Exception {
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        final var body = "{" + TM01_FRAGMENT.replace("2022-09-13", "") + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .requestAttr("token_permissions",tokenPermissions)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].location", is("$.resigned_on")))
                .andExpect(jsonPath("$.errors[0].error_values", is(nullValue())))
                .andExpect(jsonPath("$.errors[0].error", is("must not be null")));
    }

    @Test
    void createFilingWhenReferenceResignedOnInFutureThenResponse400() throws Exception {
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        final var tomorrow = LocalDate.now().plusDays(1);
        final var body = "{"
                + TM01_FRAGMENT.replace("2022-09-13", tomorrow.toString())
                + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .requestAttr("token_permissions",tokenPermissions)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].location", is("$.resigned_on")))
                .andExpect(jsonPath("$.errors[0].error_values",
                        is(Map.of("rejected", tomorrow.toString()))))
                .andExpect(jsonPath("$.errors[0].error",
                        is("must be a date in the past or in the present")));
    }

    @Test
    void createFilingWhenResignedOnPriorTo01092009ThenResponse400() throws Exception {
        companyProfileApi.setDateOfCreation(LocalDate.of(2009, 1, 1));
        companyAppointment.setAppointedOn(LocalDate.of(2009, 1, 2));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointmentService.getCompanyAppointment(COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        final var body = "{"
                + TM01_FRAGMENT.replace("2022-09-13", "2009-09-13")
                + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .requestAttr("token_permissions",tokenPermissions)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].error",
                        is("You have entered a date too far in the past. Please check the date and resubmit")));
    }

    @Test
    void createFilingWhenResignedOnBeforeIncorporationDateThenResponse400() throws Exception {
        companyAppointment.setAppointedOn(LocalDate.of(2009, 10, 1));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointmentService.getCompanyAppointment(COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        final var body = "{"
                + TM01_FRAGMENT.replace("2022-09-13", "2009-10-05")
                + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .requestAttr("token_permissions",tokenPermissions)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].error",
                        is("Director name has not been found")));
    }

    @Test
    void createFilingWhenResignedOnInvalidThenResponse400() throws Exception {
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointmentService.getCompanyAppointment(COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        final var body = "{"
                + TM01_FRAGMENT.replace("2022-09-13", "2022-09-33")
                + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .requestAttr("token_permissions",tokenPermissions)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].error",
                        containsString("DateTimeParseException")));
    }

    @Test
    void createFilingWhenReferenceEtagInvalid() throws Exception {
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointmentService.getCompanyAppointment(COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        final var body = "{" + TM01_FRAGMENT.replace("ETAG", "invalid") + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                .requestAttr("token_permissions",tokenPermissions)
                            .contentType("application/json")
                            .headers(httpHeaders))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasSize(1)))
                    .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                    .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                    .andExpect(jsonPath("$.errors[0].error",
                            containsString("The Officers information is out of date. Please start the process again and make a new submission")));
    }

    @Test
    void createFilingWhenDissolvedDateExistsThenResponse400() throws Exception {
        companyProfileApi.setDateOfCessation(LocalDate.of(2022, Month.JANUARY, 1));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointmentService.getCompanyAppointment(COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        final var body = "{"
                + TM01_FRAGMENT
                + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].error",
                        containsString("You cannot remove an officer from a company that is about to be dissolved")));
    }

    @Test
    void createFilingWhenStatusIsDissolvedThenResponse400() throws Exception {
        companyProfileApi.setCompanyStatus("dissolved");
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointmentService.getCompanyAppointment(COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        final var body = "{"
                + TM01_FRAGMENT
                + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
                .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
                .andExpect(jsonPath("$.errors[0].error",
                        containsString("You cannot remove an officer from a company that has been dissolved")));
    }

    @Test
    void createFilingWhenOfficerNotIdentifiedThenResponse400() throws Exception {
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);
        when(companyAppointmentService.getCompanyAppointment(COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenThrow(
            new CompanyAppointmentServiceException("Error Retrieving appointment"));

        final var body = "{"
            + TM01_FRAGMENT
            + "}";

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                .contentType("application/json")
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].type", is("ch:validation")))
            .andExpect(jsonPath("$.errors[0].location_type", is("json-path")))
            .andExpect(jsonPath("$.errors[0].error",
                containsString("The Officers information is out of date. Please start the process again and make a new submission")));
    }
}