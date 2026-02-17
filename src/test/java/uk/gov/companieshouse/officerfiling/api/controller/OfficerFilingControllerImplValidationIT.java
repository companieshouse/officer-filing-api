package uk.gov.companieshouse.officerfiling.api.controller;

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
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("web")
@WebMvcTest(controllers = OfficerFilingControllerImpl.class)
class OfficerFilingControllerImplValidationIT {
    private static final String TRANS_ID = "4f56fdf78b357bfc";
    private static final String FILING_ID = "632c8e65105b1b4a9f0d1f5e";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final Instant FIRST_INSTANT = Instant.parse("2022-10-15T09:44:08.108Z");
    private static final String TM01_FRAGMENT = "\"reference_etag\": \"ETAG\","
            + "\"reference_appointment_id\": \"" + FILING_ID + "\","
            + "\"resigned_on\": \"2022-09-13\"";
    private static final String COMPANY_NUMBER = "123456";
    public static final LocalDate INCORPORATION_DATE = LocalDate.of(2010, Month.OCTOBER, 20);
    public static final LocalDate APPOINTMENT_DATE = LocalDate.of(2010, Month.OCTOBER, 30);
    public static final String DIRECTOR_NAME = "Director name";
    private static final String ETAG = "ETAG";
    private static final String COMPANY_TYPE = "ltd";
    private static final String OFFICER_ROLE = "director";

    @MockitoBean
    private TransactionService transactionService;
    @MockitoBean
    private OfficerFilingService officerFilingService;
    @MockitoBean
    private CompanyProfileService companyProfileService;
    @MockitoBean
    private CompanyAppointmentService companyAppointmentService;
    @MockitoBean
    private OfficerFilingMapper filingMapper;
    @MockitoBean
    private Clock clock;
    @MockitoBean
    private Logger logger;
    @MockitoBean
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
        httpHeaders.add("ERIC-Authorised-Token-Permissions", "company_number="+COMPANY_NUMBER+" company_officers=readprotected,delete,create,update");

        transaction = new Transaction();
        transaction.setCompanyNumber(COMPANY_NUMBER);
        transaction.setId(TRANS_ID);
        transaction.setStatus(TransactionStatus.OPEN);
        companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setDateOfCreation(INCORPORATION_DATE);
        companyProfileApi.setType(COMPANY_TYPE);
        companyAppointment = new AppointmentFullRecordAPI();
        companyAppointment.setName(DIRECTOR_NAME);
        companyAppointment.setAppointedOn(APPOINTMENT_DATE);
        companyAppointment.setEtag(ETAG);
        companyAppointment.setOfficerRole(OFFICER_ROLE);
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClientMock);
        when(apiClientMock.transactions()).thenReturn(transactionResourceHandlerMock);
        when(transactionResourceHandlerMock.get(anyString())).thenReturn(transactionGetMock);
        when(transactionGetMock.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(transaction);
    }

    @Test
    void createFilingWhenReferenceEtagBlankThenResponse201() throws Exception {
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        final var body = "{" + TM01_FRAGMENT.replace("ETAG", "") + "}";
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();
        var offData = new OfficerFilingData(
                "",
                FILING_ID,
                Instant.parse("2022-09-13T00:00:00Z"));
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
        when(filingMapper.map(dto)).thenReturn(filing);
        when(clock.instant()).thenReturn(FIRST_INSTANT);
        when(officerFilingService.save(any(OfficerFiling.class), eq(TRANS_ID))).thenReturn(
                        OfficerFiling.builder(filing).id(FILING_ID)
                                .build()) // copy of 'filing' with id=FILING_ID
                .thenAnswer(i -> OfficerFiling.builder(i.getArgument(0))
                        .build()); // copy of first argument
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void createFilingWhenReferenceAppointmentIdBlankThenResponse201() throws Exception {
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        final var body = "{" + TM01_FRAGMENT.replace(FILING_ID, "") + "}";
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("ETAG")
                .referenceAppointmentId("")
                .resignedOn(LocalDate.of(2022, 9, 13))
                .build();
        var offData = new OfficerFilingData(
                "ETAG",
                "",
                Instant.parse("2022-09-13T00:00:00Z"));
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();
        when(filingMapper.map(dto)).thenReturn(filing);
        when(clock.instant()).thenReturn(FIRST_INSTANT);
        when(officerFilingService.save(any(OfficerFiling.class), eq(TRANS_ID))).thenReturn(
                        OfficerFiling.builder(filing).id(FILING_ID)
                                .build()) // copy of 'filing' with id=FILING_ID
                .thenAnswer(i -> OfficerFiling.builder(i.getArgument(0))
                        .build()); // copy of first argument
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                .contentType("application/json")
                .headers(httpHeaders))
            .andDo(print())
            .andExpect(status().isCreated());
    }

    /**
     * An empty string is converted to null for a date, hence the 201
     * @throws Exception
     */
    @Test
    void createFilingWhenReferenceResignedOnBlankThenResponse201() throws Exception {
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        final var body = "{" + TM01_FRAGMENT.replace("2022-09-13", "") + "}";
        final var dto = OfficerFilingDto.builder()
                .referenceEtag("ETAG")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(null)
                .build();
        var offData = new OfficerFilingData(
                "ETAG",
                FILING_ID,
                null);
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();
        when(filingMapper.map(dto)).thenReturn(filing);
        when(clock.instant()).thenReturn(FIRST_INSTANT);
        when(officerFilingService.save(any(OfficerFiling.class), eq(TRANS_ID))).thenReturn(
                        OfficerFiling.builder(filing).id(FILING_ID)
                                .build()) // copy of 'filing' with id=FILING_ID
                .thenAnswer(i -> OfficerFiling.builder(i.getArgument(0))
                        .build()); // copy of first argument
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);

        mockMvc.perform(post("/transactions/{id}/officers", TRANS_ID).content(body)
                        .contentType("application/json")
                        .headers(httpHeaders))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void createFilingWhenResignedOnInvalidThenResponse400() throws Exception {
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyAppointmentService.getCompanyAppointment(TRANS_ID, COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);

        final var body = "{"
                + TM01_FRAGMENT.replace("2022-09-13", "2022-09-33")
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
                        containsString("DateTimeParseException")));
    }
}