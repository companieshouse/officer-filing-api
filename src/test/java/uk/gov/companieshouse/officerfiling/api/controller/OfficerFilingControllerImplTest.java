package uk.gov.companieshouse.officerfiling.api.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.officerfiling.api.controller.OfficerFilingControllerImpl.VALIDATION_STATUS;
import static uk.gov.companieshouse.officerfiling.api.model.entity.Links.PREFIX_PRIVATE;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
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
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
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
    private static final String ETAG = "etag";
    private static final String COMPANY_TYPE = "ltd";
    private static final String OFFICER_ROLE = "director";
    private static final String APPOINTMENT_ID = "12345678";
    private static final String FILING_DESCRIPTION = "xyz a company director";

    private OfficerFilingControllerImpl testController;
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
        testController = new OfficerFilingControllerImpl(transactionService, officerFilingService,
                filingMapper, clock, logger);
        ReflectionTestUtils.setField(testController, "isTm01Enabled", true);
        var offData = new OfficerFilingData(
                "etag",
                "off-id",
                Instant.parse("2022-09-13T00:00:00Z"));
        filing = OfficerFiling.builder().createdAt(FIRST_INSTANT).updatedAt(FIRST_INSTANT).data(offData)
                .build();
        final var builder = UriComponentsBuilder.fromUri(REQUEST_URI);
        links = new Links(builder.pathSegment(FILING_ID)
                .build().toUri(), builder.pathSegment("validation_status")
                .build().toUri());
        resourceMap = createResources();
    }

    @ParameterizedTest(name = "[{index}] null binding result={0}")
    @ValueSource(booleans = {true, false})
    void createFiling(final boolean nullBindingResult) {
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(request.getRequestURI()).thenReturn(REQUEST_URI.toString());
        when(clock.instant()).thenReturn(FIRST_INSTANT);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(filingMapper.map(dto)).thenReturn(filing);
        final var withFilingId = OfficerFiling.builder(filing).id(FILING_ID)
                .build();
        final var withLinks = OfficerFiling.builder(withFilingId).links(links)
                .build();
        when(officerFilingService.save(filing, TRANS_ID)).thenReturn(withFilingId);
        when(officerFilingService.save(withLinks, TRANS_ID)).thenReturn(withLinks);

        final var response =
                testController.createFiling(transaction, dto, nullBindingResult ? null : result,
                        request);

        // refEq needed to compare Map value objects; Resource does not override equals()
        verify(transaction).setResources(refEq(resourceMap));
        verify(transactionService).updateTransaction(transaction, PASSTHROUGH_HEADER);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    }

    @Test
    void createFilingWithExistingSubmission() {
        final var resources = new HashMap<String,Resource>();
        final var resource = new Resource();
        final Map <String,String> resourcesMap = new HashMap<>();
        resourcesMap.put("resource","/transactions/115025-478816-868338/officers/648b0b3a246067277f8dcb70");
        resource.setLinks(resourcesMap);
        resources.put("/transactions/115025-478816-868338/officers/648b0b3a246067277f8dcb70", resource);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(request.getRequestURI()).thenReturn(REQUEST_URI.toString());
        when(clock.instant()).thenReturn(FIRST_INSTANT);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(transaction.getResources()).thenReturn(resources);
        when(filingMapper.map(dto)).thenReturn(filing);
        final var withFilingId = OfficerFiling.builder(filing).id(FILING_ID)
                .build();
        final var withLinks = OfficerFiling.builder(withFilingId).links(links)
                .build();
        when(officerFilingService.save(filing, TRANS_ID)).thenReturn(withFilingId);
        when(officerFilingService.save(withLinks, TRANS_ID)).thenReturn(withLinks);

        final var response =
                testController.createFiling(transaction, dto, result,
                        request);

        // refEq needed to compare Map value objects; Resource does not override equals()
        verify(transaction).setResources(refEq(resourceMap));
        verify(transactionService, never()).updateTransaction(transaction, PASSTHROUGH_HEADER);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));

    }

    @Test
    void createFilingWithExistingSubmissionAndDescription() {
        final var resources = new HashMap<String,Resource>();
        final var resource = new Resource();
        final Map <String,String> resourcesMap = new HashMap<>();
        resourcesMap.put("resource","/transactions/115025-478816-868338/officers/648b0b3a246067277f8dcb70");
        resource.setLinks(resourcesMap);
        resources.put("/transactions/115025-478816-868338/officers/648b0b3a246067277f8dcb70", resource);
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(request.getRequestURI()).thenReturn(REQUEST_URI.toString());
        when(clock.instant()).thenReturn(FIRST_INSTANT);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(transaction.getResources()).thenReturn(resources);
        when(filingMapper.map(dto)).thenReturn(filing);
        when(dto.getDescription()).thenReturn(FILING_DESCRIPTION);
        final var withFilingId = OfficerFiling.builder(filing).id(FILING_ID)
                .build();
        final var withLinks = OfficerFiling.builder(withFilingId).links(links)
                .build();
        when(officerFilingService.save(filing, TRANS_ID)).thenReturn(withFilingId);
        when(officerFilingService.save(withLinks, TRANS_ID)).thenReturn(withLinks);

        final var response =
                testController.createFiling(transaction, dto, result,
                        request);

        // refEq needed to compare Map value objects; Resource does not override equals()
        verify(transaction).setResources(refEq(resourceMap));
        verify(transaction).setDescription(FILING_DESCRIPTION);
        verify(transactionService).updateTransaction(transaction, PASSTHROUGH_HEADER);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));

    }

    @Test
    void createFilingWhenReferenceAppointmentIdNull() {
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(request.getRequestURI()).thenReturn(REQUEST_URI.toString());
        when(clock.instant()).thenReturn(FIRST_INSTANT);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(filingMapper.map(dto)).thenReturn(filing);
        final var withFilingId = OfficerFiling.builder(filing).id(FILING_ID)
                .build();
        final var withLinks = OfficerFiling.builder(withFilingId).links(links)
                .build();
        when(officerFilingService.save(filing, TRANS_ID)).thenReturn(withFilingId);
        when(officerFilingService.save(withLinks, TRANS_ID)).thenReturn(withLinks);

        final var response = testController.createFiling(transaction, dto, result, request);

        // refEq needed to compare Map value objects; Resource does not override equals()
        verify(transaction).setResources(refEq(resourceMap));
        verify(transactionService).updateTransaction(transaction, PASSTHROUGH_HEADER);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        final OfficerFiling filingResponse = (OfficerFiling) response.getBody();
        assertThat(filingResponse.getId(), is(FILING_ID));
    }

    @Test
    void createFilingWhenReferenceAppointmentIdBlank() {
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(request.getRequestURI()).thenReturn(REQUEST_URI.toString());
        when(clock.instant()).thenReturn(FIRST_INSTANT);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(filingMapper.map(dto)).thenReturn(filing);
        final var withFilingId = OfficerFiling.builder(filing).id(FILING_ID)
                .build();
        final var withLinks = OfficerFiling.builder(withFilingId).links(links)
                .build();
        when(officerFilingService.save(filing, TRANS_ID)).thenReturn(withFilingId);
        when(officerFilingService.save(withLinks, TRANS_ID)).thenReturn(withLinks);

        final var response = testController.createFiling(transaction, dto, result, request);

        // refEq needed to compare Map value objects; Resource does not override equals()
        verify(transaction).setResources(refEq(resourceMap));
        verify(transactionService).updateTransaction(transaction, PASSTHROUGH_HEADER);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        final OfficerFiling filingResponse = (OfficerFiling) response.getBody();
        assertThat(filingResponse.getId(), is(FILING_ID));
    }


    @Test
    void createFilingWhenDescriptionNotBlank() {
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(request.getRequestURI()).thenReturn(REQUEST_URI.toString());
        when(clock.instant()).thenReturn(FIRST_INSTANT);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(filingMapper.map(dto)).thenReturn(filing);
        final var withFilingId = OfficerFiling.builder(filing).id(FILING_ID)
                .build();
        final var withLinks = OfficerFiling.builder(withFilingId).links(links)
                .build();
        when(officerFilingService.save(filing, TRANS_ID)).thenReturn(withFilingId);
        when(officerFilingService.save(withLinks, TRANS_ID)).thenReturn(withLinks);
        when(dto.getDescription()).thenReturn(FILING_DESCRIPTION);

        final var response = testController.createFiling(transaction, dto, result, request);

        verify(transaction).setDescription(FILING_DESCRIPTION);
        verify(transactionService).updateTransaction(transaction, PASSTHROUGH_HEADER);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        final OfficerFiling filingResponse = (OfficerFiling) response.getBody();
        assertThat(filingResponse.getId(), is(FILING_ID));
    }

    @Test
    void createFilingWhenDescriptionSameAsOnTransaction() {
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(request.getRequestURI()).thenReturn(REQUEST_URI.toString());
        when(clock.instant()).thenReturn(FIRST_INSTANT);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(filingMapper.map(dto)).thenReturn(filing);
        final var withFilingId = OfficerFiling.builder(filing).id(FILING_ID)
                .build();
        final var withLinks = OfficerFiling.builder(withFilingId).links(links)
                .build();
        when(officerFilingService.save(filing, TRANS_ID)).thenReturn(withFilingId);
        when(officerFilingService.save(withLinks, TRANS_ID)).thenReturn(withLinks);
        when(dto.getDescription()).thenReturn(FILING_DESCRIPTION);
        when(transaction.getDescription()).thenReturn(FILING_DESCRIPTION);

        final var response = testController.createFiling(transaction, dto, result, request);

        verify(transaction, never()).setDescription(FILING_DESCRIPTION);
        verify(transactionService).updateTransaction(transaction, PASSTHROUGH_HEADER);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        final OfficerFiling filingResponse = (OfficerFiling) response.getBody();
        assertThat(filingResponse.getId(), is(FILING_ID));
    }

    @ParameterizedTest(name = "[{index}] null binding result={0}")
    @ValueSource(booleans = {true, false})
    void patchFiling(final boolean nullBindingResult) {
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(request.getRequestURI()).thenReturn(REQUEST_URI.toString());
        when(clock.instant()).thenReturn(FIRST_INSTANT);
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(transaction.getResources()).thenReturn(getResourcesForFiling());
        when(filingMapper.map(dto)).thenReturn(filing);
        final var withFilingId = OfficerFiling.builder(filing).id(FILING_ID)
                .build();
        final var withLinks = OfficerFiling.builder(withFilingId).links(links)
                .build();
        when(officerFilingService.save(filing, TRANS_ID)).thenReturn(withFilingId);
        when(officerFilingService.save(withLinks, TRANS_ID)).thenReturn(withLinks);

        final var response =
                testController.patchFiling(transaction, dto, FILING_ID, nullBindingResult ? null : result,
                        request);

        // refEq needed to compare Map value objects; Resource does not override equals()
        verify(transaction).setResources(refEq(resourceMap));
        verify(transactionService).updateTransaction(transaction, PASSTHROUGH_HEADER);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));

        final var filingOptional = Optional.of(withFilingId);
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(filingOptional);
        when(officerFilingService.mergeFilings(withFilingId, filingOptional.get(), transaction)).thenReturn(filingOptional.get());
        final var mergeResponse =
                testController.patchFiling(transaction, dto, FILING_ID, nullBindingResult ? null : result,
                        request);
        assertThat(mergeResponse.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    void patchFilingWithInvalidSubmissionId() {
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(request.getRequestURI()).thenReturn(REQUEST_URI.toString());
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(transaction.getResources()).thenReturn(getResourcesForFiling("some-other-filing-id"));
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
       
        assertThrows(InvalidFilingException.class,
               () -> testController.patchFiling(transaction, dto, FILING_ID, null, request));
    }

    @Test
    void patchFilingWithSomeOtherFiling() {
        var resources = getResourcesForFiling();
        resources.get("resource").setKind("no-an-officer-filing");
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(request.getRequestURI()).thenReturn(REQUEST_URI.toString());
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(transaction.getResources()).thenReturn(resources);
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
       
        assertThrows(InvalidFilingException.class,
               () -> testController.patchFiling(transaction, dto, FILING_ID, null, request));
    }

    @Test
    void patchFilingWithNoResourceLink() {
        var resources = getResourcesForFiling();
        resources.get("resource").getLinks().remove("resource");
        when(request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())).thenReturn(PASSTHROUGH_HEADER);
        when(request.getRequestURI()).thenReturn(REQUEST_URI.toString());
        when(transaction.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(transaction.getResources()).thenReturn(resources);
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));
       
        assertThrows(InvalidFilingException.class,
               () -> testController.patchFiling(transaction, dto, FILING_ID, null, request));
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
                () -> testController.createFiling(transaction, dto, result, request));

        assertThat(exception.getFieldErrors(), contains(fieldErrorWithRejectedValue));

        final var patchException = assertThrows(InvalidFilingException.class,
                () -> testController.patchFiling(transaction, dto, null, result, request));

        assertThat(patchException.getFieldErrors(), contains(fieldErrorWithRejectedValue));
    }

    @Test
    void getFilingForReviewWhenFound() {
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(transaction.getResources()).thenReturn(getResourcesForFiling());
        when(filingMapper.map(filing)).thenReturn(dto);
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.of(filing));

        final var response =
            testController.getFilingForReview(transaction, FILING_ID);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(dto));
    }

    @Test
    void getFilingWhenNoTransaction() {
        assertThrows(InvalidFilingException.class,
            () -> testController.getFilingForReview(null, FILING_ID));
    }

    @Test
    void getFilingForReviewNotFound() {
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(transaction.getResources()).thenReturn(getResourcesForFiling());
        when(officerFilingService.get(FILING_ID, TRANS_ID)).thenReturn(Optional.empty());

        final var response =
            testController.getFilingForReview(transaction, FILING_ID);

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void getFilingWhenNoResource() {
        when(transaction.getResources()).thenReturn(null);
       
        assertThrows(InvalidFilingException.class,
            () -> testController.getFilingForReview(transaction, FILING_ID));
    }

    @Test
    void getFilingWhenEmptyResources() {
        when(transaction.getResources()).thenReturn(new HashMap<>());
       
        assertThrows(InvalidFilingException.class,
            () -> testController.getFilingForReview(transaction, FILING_ID));
    }

    @Test
    void getFilingForReviewWhenInvalidSubmissionId() {
        when(transaction.getId()).thenReturn(TRANS_ID);
        when(transaction.getResources()).thenReturn(getResourcesForFiling("some-other-filing-id"));
       
        assertThrows(InvalidFilingException.class,
        () -> testController.getFilingForReview(transaction, FILING_ID));
    }

    @Test
    void checkTm01FeatureFlagDisabled(){
        ReflectionTestUtils.setField(testController, "isTm01Enabled", false);
        assertThrows(FeatureNotEnabledException.class,
                () -> testController.createFiling(transaction, dto, result, request));
        assertThrows(FeatureNotEnabledException.class,
                () -> testController.patchFiling(transaction, dto, null, result, request));
        assertThrows(FeatureNotEnabledException.class,
            () -> testController.getFilingForReview(transaction, FILING_ID));
    }

    @Test
    void checkLinksBuild() throws URISyntaxException {
        // Check a patch scenario
        OfficerFiling idFiling = OfficerFiling.builder().id("63f4afcf5cd8192a09d6a9e8").build();
        when(request.getRequestURI()).thenReturn("/transactions/027314-549816-769801/officers/63f4afcf5cd8192a09d6a9e8");
        Links buildLinks = ReflectionTestUtils.invokeMethod(testController, "buildLinks", idFiling.getId(),
                request);
        assertThat(buildLinks.getSelf(), is(new URI("/transactions/027314-549816-769801/officers/63f4afcf5cd8192a09d6a9e8")));

        // Check a post scenario
        when(request.getRequestURI()).thenReturn("/transactions/027314-549816-769801/officers/");
        buildLinks = ReflectionTestUtils.invokeMethod(testController, "buildLinks", idFiling.getId(),
                request);
        assertThat(buildLinks.getSelf(), is(new URI("/transactions/027314-549816-769801/officers/63f4afcf5cd8192a09d6a9e8")));
    }

    private Map<String, Resource> getResourcesForFiling() {
        return getResourcesForFiling(FILING_ID);
    }

    private Map<String, Resource> getResourcesForFiling(String filingId) {
        var resource = new Resource();
        resource.setKind("officer-filing");
        Map<String, String> links = new HashMap<>();
        links.put("resource", "/transactions/" + TRANS_ID + "/officers/" + filingId);
        resource.setLinks(links);
        Map<String, Resource> resources = new HashMap<>();
        resources.put("resource", resource);
        return resources;
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
}