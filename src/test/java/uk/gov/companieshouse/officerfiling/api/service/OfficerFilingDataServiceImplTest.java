package uk.gov.companieshouse.officerfiling.api.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.Officer;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.entity.Links;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.repository.OfficerFilingRepository;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import javax.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class OfficerFilingDataServiceImplTest {
    public static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    public static final String TRANS_ID = "12345-54321-76666";
    private OfficerFilingService testService;

    @Mock
    private OfficerFilingRepository repository;
    @Mock
    private OfficerFiling filing;
    @Mock
    private Logger logger;
    @Mock
    private LogHelper logHelper;
    @Mock
    private Transaction transaction;
    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        testService = new OfficerFilingServiceImpl(repository, logger);
    }

    @Test
    void save() {
        testService.save(filing, TRANS_ID);

        verify(repository).save(filing);
    }

    @Test
    void getWhenFound() {
        var filing = OfficerFiling.builder().build();
        when(repository.findById(FILING_ID)).thenReturn(Optional.of(filing));
        final var officerFiling = testService.get(FILING_ID, TRANS_ID);

        assertThat(officerFiling.isPresent(), is(true));
    }

    @Test
    void getWhenNotFound() {
        when(repository.findById(FILING_ID)).thenReturn(Optional.empty());
        final var officerFiling = testService.get(FILING_ID, TRANS_ID);

        assertThat(officerFiling.isPresent(), is(false));
    }

    @Test
    void testMergePartial(){
        OfficerFiling original = OfficerFiling.builder().referenceEtag("ETAG").build();
        OfficerFiling patch = OfficerFiling.builder().referenceAppointmentId("Appoint").build();
        OfficerFiling updatedFiling = testService.mergeFilings(original, patch, transaction);
        assertThat(updatedFiling.getReferenceEtag(), is("ETAG"));
        assertThat(updatedFiling.getReferenceAppointmentId(), is("Appoint"));
    }

    @Test
    void testMergeFull(){
        OfficerFiling original = OfficerFiling.builder().referenceEtag("ETAG")
                .resignedOn(Instant.parse("2022-09-13T00:00:00Z")).build();
        OfficerFiling patch = OfficerFiling.builder().referenceAppointmentId("Appoint").build();
        OfficerFiling updatedFiling = testService.mergeFilings(original, patch, transaction);
        assertThat(updatedFiling.getReferenceEtag(), is("ETAG"));
        assertThat(updatedFiling.getReferenceAppointmentId(), is("Appoint"));
        assertThat(updatedFiling.getResignedOn(), is(Instant.parse("2022-09-13T00:00:00Z")));
    }

    @Test
    void testMergeOverwrite(){
        OfficerFiling original = OfficerFiling.builder().referenceEtag("ETAG")
                .resignedOn(Instant.parse("2022-09-13T00:00:00Z")).build();
        OfficerFiling patch = OfficerFiling.builder().referenceAppointmentId("Appoint")
                .referenceEtag("NewETAG").build();
        OfficerFiling updatedFiling = testService.mergeFilings(original, patch, transaction);
        assertThat(updatedFiling.getReferenceEtag(), is("NewETAG"));
        assertThat(updatedFiling.getReferenceAppointmentId(), is("Appoint"));
        assertThat(updatedFiling.getResignedOn(), is(Instant.parse("2022-09-13T00:00:00Z")));
    }

    @Test
    void testRequestUriContainsFilingSelfLinkPassesWithValidTransaction() throws URISyntaxException {
        URI selfUri = new URI("/transactions/012345-67891-01112/officers/abcdefghijklmnopqrstuvwx");
        URI validationStatusURI = new URI("");
        Links links = new Links(selfUri, validationStatusURI);

        OfficerFiling filing = OfficerFiling.builder()
                .links(links)
                .build();

        String matchingRequestURI= "/transactions/012345-67891-01112/officers/abcdefghijklmnopqrstuvwx";
        when(request.getRequestURI()).thenReturn(matchingRequestURI);

        Boolean result = testService.requestUriContainsFilingSelfLink(request, filing);
        assertThat(result, is(true));
    }

    @Test
    void testRequestUriContainsFilingSelfLinkPassesWithValidTransactionAndAppendedGetValidation() throws URISyntaxException {
        URI selfUri = new URI("/transactions/012345-67891-01112/officers/abcdefghijklmnopqrstuvwx");
        URI validationStatusURI = new URI("");
        Links links = new Links(selfUri, validationStatusURI);

        OfficerFiling filing = OfficerFiling.builder()
                .links(links)
                .build();

        String matchingRequestURIWithAppendedPath = "/transactions/012345-67891-01112/officers/abcdefghijklmnopqrstuvwx/validation_status";
        when(request.getRequestURI()).thenReturn(matchingRequestURIWithAppendedPath);

        Boolean result = testService.requestUriContainsFilingSelfLink(request, filing);
        assertThat(result, is(true));
    }

    @Test
    void testRequestUriContainsFilingSelfLinkFailsWithInValidTransaction() throws URISyntaxException {
        URI selfUri = new URI("/transactions/012345-67891-01112/officers/abcdefghijklmnopqrstuvwx");
        URI validationStatusURI = new URI("");
        Links links = new Links(selfUri, validationStatusURI);

        OfficerFiling filing = OfficerFiling.builder()
                .links(links)
                .build();

        String matchingRequestURI = "/transactions/012345-67891-12345/officers/abcdefghijklmnopqrstuvwx";
        when(request.getRequestURI()).thenReturn(matchingRequestURI);

        Boolean result = testService.requestUriContainsFilingSelfLink(request, filing);
        assertThat(result, is(false));
    }

    @Test
    void testRequestUriContainsFilingSelfLinkFailsWithInValidTransactionAndAppendedPath() throws URISyntaxException {
        URI selfUri = new URI("/transactions/012345-67891-01112/officers/abcdefghijklmnopqrstuvwx");
        URI validationStatusURI = new URI("");
        Links links = new Links(selfUri, validationStatusURI);

        OfficerFiling filing = OfficerFiling.builder()
                .links(links)
                .build();

        String matchingRequestURIWithAppendedPath = "/transactions/012345-67891-12345/officers/abcdefghijklmnopqrstuvwx/validation_status";
        when(request.getRequestURI()).thenReturn(matchingRequestURIWithAppendedPath);

        Boolean result = testService.requestUriContainsFilingSelfLink(request, filing);
        assertThat(result, is(false));
    }
}