package uk.gov.companieshouse.officerfiling.api.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.officerfiling.api.model.entity.Address;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.model.entity.FormerName;
import uk.gov.companieshouse.officerfiling.api.model.entity.Identification;
import uk.gov.companieshouse.officerfiling.api.model.entity.Links;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
import uk.gov.companieshouse.officerfiling.api.repository.OfficerFilingRepository;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

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
        OfficerFilingData originalData = OfficerFilingData.builder()
                .referenceEtag("ETAG")
                .build();
        OfficerFiling original = OfficerFiling.builder()
                .data(originalData)
                .build();
        OfficerFilingData patchData = OfficerFilingData.builder()
                .referenceAppointmentId("Appoint")
                .build();
        OfficerFiling patch = OfficerFiling.builder().data(patchData).build();
        OfficerFiling updatedFiling = testService.mergeFilings(original, patch, transaction);
        assertThat(updatedFiling.getData().getReferenceEtag(), is("ETAG"));
        assertThat(updatedFiling.getData().getReferenceAppointmentId(), is("Appoint"));
    }

    @Test
    void testMergeFull(){
        List<FormerName> formerNames = new ArrayList<>(1);
        FormerName formerName = new FormerName("John", "Doe");
        formerNames.add(formerName);
        var address = Address.builder().locality("Margate").country("UK").build();
        var identification = new Identification("type", "authority",
                "form", "registered", "number");
        OfficerFilingData originalData = OfficerFilingData.builder()
                .referenceEtag("ETAG")
                .resignedOn(Instant.parse("2022-09-13T00:00:00Z"))
                .formerNames(formerNames)
                .address(address)
                .build();
        OfficerFiling original = OfficerFiling.builder()
                .data(originalData)
                .identification(identification)
                .build();
        OfficerFilingData patchData = OfficerFilingData.builder()
                .referenceAppointmentId("Appoint")
                .build();
        OfficerFiling patch = OfficerFiling.builder()
                .data(patchData)
                .build();
        OfficerFiling updatedFiling = testService.mergeFilings(original, patch, transaction);
        assertThat(updatedFiling.getData().getReferenceEtag(), is("ETAG"));
        assertThat(updatedFiling.getData().getReferenceAppointmentId(), is("Appoint"));
        assertThat(updatedFiling.getData().getResignedOn(), is(Instant.parse("2022-09-13T00:00:00Z")));
        assertThat(updatedFiling.getData().getFormerNames().get(0).getForenames(), is("John"));
        assertThat(updatedFiling.getData().getFormerNames().get(0).getSurname(), is("Doe"));
        assertThat(updatedFiling.getData().getAddress().getLocality(), is("Margate"));
        assertThat(updatedFiling.getData().getAddress().getCountry(), is("UK"));
        assertThat(updatedFiling.getIdentification().getIdentificationType(), is("type"));
    }

    @Test
    void testMergeOverwrite(){
        OfficerFilingData originalData = OfficerFilingData.builder()
                .referenceEtag("ETAG")
                .resignedOn(Instant.parse("2022-09-13T00:00:00Z"))
                .build();
        OfficerFiling original = OfficerFiling.builder().data(originalData).build();
        OfficerFilingData patchData = OfficerFilingData.builder()
                .referenceAppointmentId("Appoint")
                .referenceEtag("NewETAG")
                .build();
        OfficerFiling patch = OfficerFiling.builder().data(patchData).build();
        OfficerFiling updatedFiling = testService.mergeFilings(original, patch, transaction);
        assertThat(updatedFiling.getData().getReferenceEtag(), is("NewETAG"));
        assertThat(updatedFiling.getData().getReferenceAppointmentId(), is("Appoint"));
        assertThat(updatedFiling.getData().getResignedOn(), is(Instant.parse("2022-09-13T00:00:00Z")));
    }

    OfficerFiling setUpFiling() throws URISyntaxException {
        URI selfUri = new URI("/transactions/012345-67891-01112/officers/abcd");
        URI validationStatusURI = new URI("");
        Links links = new Links(selfUri, validationStatusURI);

        OfficerFiling filing = OfficerFiling.builder()
                .links(links)
                .build();

        return filing;
    }
    @Test
    void testRequestUriContainsFilingSelfLinkPassesWithValidTransaction() throws URISyntaxException {
        filing = setUpFiling();

        String validRequestURI = "/transactions/012345-67891-01112/officers/abcd";
        when(request.getRequestURI()).thenReturn(validRequestURI);

        Boolean result = testService.requestUriContainsFilingSelfLink(request, filing);
        assertThat(result, is(true));
    }

    @Test
    void testRequestUriContainsFilingSelfLinkPassesWithValidTransactionAndAppendedGetValidation() throws URISyntaxException {
        filing = setUpFiling();

        String validRequestURIWithAppendedPath = "/transactions/012345-67891-01112/officers/abcd/validation_status";
        when(request.getRequestURI()).thenReturn(validRequestURIWithAppendedPath);

        Boolean result = testService.requestUriContainsFilingSelfLink(request, filing);
        assertThat(result, is(true));
    }

    @Test
    void testRequestUriContainsFilingSelfLinkFailsWithInValidTransaction() throws URISyntaxException {
        filing = setUpFiling();

        String invalidRequestURI = "/transactions/98765-67891-12345/officers/abcd";
        when(request.getRequestURI()).thenReturn(invalidRequestURI);

        Boolean result = testService.requestUriContainsFilingSelfLink(request, filing);
        assertThat(result, is(false));
    }

    @Test
    void testRequestUriContainsFilingSelfLinkFailsWithInValidTransactionAndAppendedPath() throws URISyntaxException {
        filing = setUpFiling();

        String invalidRequestURIWithAppendedPath = "/transactions/98765-67891-12345/officers/abcd/validation_status";
        when(request.getRequestURI()).thenReturn(invalidRequestURIWithAppendedPath);

        Boolean result = testService.requestUriContainsFilingSelfLink(request, filing);
        assertThat(result, is(false));
    }
}
