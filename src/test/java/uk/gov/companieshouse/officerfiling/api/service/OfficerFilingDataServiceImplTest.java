package uk.gov.companieshouse.officerfiling.api.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.repository.OfficerFilingRepository;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

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

}