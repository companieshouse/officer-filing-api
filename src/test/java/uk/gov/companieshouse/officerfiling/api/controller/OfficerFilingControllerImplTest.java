package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.officerfiling.api.controller.OfficerFilingControllerImpl.VALIDATION_STATUS;
import static uk.gov.companieshouse.officerfiling.api.model.entity.Links.PREFIX_PRIVATE;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.error.InvalidFilingException;
import uk.gov.companieshouse.officerfiling.api.exception.FeatureNotEnabledException;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.Links;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentServiceImpl;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileServiceImpl;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@ExtendWith(MockitoExtension.class)
class OfficerFilingControllerImplTest {
    public static final String TRANS_ID = "117524-754816-491724";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    public static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final URI REQUEST_URI = URI.create("/transactions/" + TRANS_ID + "/officers");
    private static final Instant FIRST_INSTANT = Instant.parse("2022-10-15T09:44:08.108Z");
    public static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    public static final String DIRECTOR_NAME = "director name";

    private OfficerFilingController testController;
    @Mock
    private OfficerFilingService officerFilingService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private CompanyProfileServiceImpl companyProfileService;
    @Mock
    private CompanyAppointmentServiceImpl companyAppointmentService;
    @Mock
    private CompanyProfileApi companyProfile;
    @Mock
    private AppointmentFullRecordAPI companyAppointment;
    @Mock
    private Clock clock;
    @Mock
    private Logger logger;
    @Mock
    private OfficerFilingMapper filingMapper;
    @Mock
    private OfficerFilingDto dto;
    @Mock
    private BindingResult result;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Transaction transaction;

    private OfficerFiling filing;
    private Links links;
    private Map<String, Resource> resourceMap;

    @BeforeEach
    void setUp() {
        testController = new OfficerFilingControllerImpl(transactionService, officerFilingService, companyProfileService, companyAppointmentService,
                filingMapper, clock, logger);
        ReflectionTestUtils.setField(testController, "isTm01Enabled", true);
        filing = OfficerFiling.builder()
                .referenceAppointmentId("off-id")
                .referenceEtag("etag")
                .resignedOn(Instant.parse("2022-09-13T00:00:00Z"))
                .build();
        final var builder = UriComponentsBuilder.fromUri(REQUEST_URI);
        final var privateBuilder = UriComponentsBuilder.fromUri(URI.create(PREFIX_PRIVATE + "/" + REQUEST_URI));
        links = new Links(builder.pathSegment(FILING_ID)
                .build().toUri(), privateBuilder.pathSegment(FILING_ID).pathSegment("validation_status")
                .build().toUri());
        resourceMap = createResources();
    }

    @ParameterizedTest(name = "[{index}] null binding result={0}")
    @ValueSource(booleans = {true, false})
    void createFiling(final boolean nullBindingResult) {
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(request.getRequestURI()).thenReturn(REQUEST_URI.toString());
        when(clock.instant()).thenReturn(FIRST_INSTANT);
        when(dto.getReferenceAppointmentId()).thenReturn(FILING_ID);
        when(dto.getResignedOn()).thenReturn(LocalDate.of(2009, 10, 1));
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2005, 10, 3));
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2007, 10, 5));
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(filingMapper.map(dto)).thenReturn(filing);
        final var withFilingId = OfficerFiling.builder(filing).id(FILING_ID)
                .build();
        final var withLinks = OfficerFiling.builder(withFilingId).links(links)
                .build();
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointmentService.getCompanyAppointment(COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(officerFilingService.save(filing, TRANS_ID)).thenReturn(withFilingId);
        when(officerFilingService.save(withLinks, TRANS_ID)).thenReturn(withLinks);
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);

        final var response =
                testController.createFiling(TRANS_ID, dto, nullBindingResult ? null : result,
                        request);

        // refEq needed to compare Map value objects; Resource does not override equals()
        verify(transaction).setResources(refEq(resourceMap));
        verify(transactionService).updateTransaction(transaction, PASSTHROUGH_HEADER);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    }

    @Test
    void createFilingWhenRequestHasBindingError() {
        final var codes = new String[]{"code1", "code2.name", "code3"};
        final var fieldErrorWithRejectedValue =
                new FieldError("object", "field", "rejectedValue", false, codes, null,
                        "errorWithRejectedValue");
        final var errorList = List.of(fieldErrorWithRejectedValue);

        when(result.hasErrors()).thenReturn(true);
        when(result.getFieldErrors()).thenReturn(errorList);

        final var exception = assertThrows(InvalidFilingException.class,
                () -> testController.createFiling(TRANS_ID, dto, result, request));

        assertThat(exception.getFieldErrors(), contains(fieldErrorWithRejectedValue));
    }


    private Map<String, Resource> createResources() {
        final Map<String, Resource> resourceMap = new HashMap<>();
        final var resource = new Resource();
        final var self = REQUEST_URI + "/" + FILING_ID;
        final var linksMap = Map.of("resource", self, VALIDATION_STATUS,
                PREFIX_PRIVATE + "/" + REQUEST_URI + "/" + FILING_ID + "/" + VALIDATION_STATUS);

        resource.setKind("officer-filing");
        resource.setLinks(linksMap);
        resource.setUpdatedAt(FIRST_INSTANT.atZone(ZoneId.systemDefault()).toLocalDateTime());
        resourceMap.put(self, resource);

        return resourceMap;
    }

    @Test
    void getFilingForReviewWhenFound() {
        when(filingMapper.map(filing)).thenReturn(dto);
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));

        final var response =
            testController.getFilingForReview(TRANS_ID, FILING_ID);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(dto));
    }

    @Test
    void getFilingForReviewNotFound() {
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.empty());

        final var response =
            testController.getFilingForReview(TRANS_ID, FILING_ID);

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void checkTm01FeatureFlagDisabled(){
        ReflectionTestUtils.setField(testController, "isTm01Enabled", false);
        assertThrows(FeatureNotEnabledException.class,
                () -> testController.createFiling(TRANS_ID, dto, result, request));
    }
    
    void doNotCreateFilingWhenRequestHasTooOldDate() {
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(companyProfile.getDateOfCreation()).thenReturn(LocalDate.of(2021, 10, 3));
        when(companyAppointment.getAppointedOn()).thenReturn(LocalDate.of(2021, 10, 5));
        when(transactionService.getTransaction(TRANS_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
        when(companyProfileService.getCompanyProfile(TRANS_ID, COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyAppointmentService.getCompanyAppointment(COMPANY_NUMBER, FILING_ID, PASSTHROUGH_HEADER)).thenReturn(companyAppointment);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        final var officerFilingDto = OfficerFilingDto.builder()
                .referenceEtag("etag")
                .referenceAppointmentId(FILING_ID)
                .resignedOn(LocalDate.of(1022, 9, 13))
                .build();

        ResponseEntity<Object> responseEntity = testController.createFiling(TRANS_ID, officerFilingDto, result, request);

        assertEquals( 400, responseEntity.getStatusCodeValue());
    }
}