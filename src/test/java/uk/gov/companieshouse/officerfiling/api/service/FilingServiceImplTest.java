package uk.gov.companieshouse.officerfiling.api.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.filing.FilingData;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;

@ExtendWith(MockitoExtension.class)
class FilingServiceImplTest {

    private static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private static final String REF_OFFICER_ID = "12345";
    private static final String REF_ETAG = "6789";
    private static final String RESIGNED_ON = "2022-10-05";
    private static final Instant RESIGNED_ON_INS = Instant.parse("2022-10-05T00:00:00Z");
    @Mock
    private OfficerFilingService officerFilingService;
    @Mock
    private OfficerFilingMapper officerFilingMapper;
    @Mock
    private Logger logger;
    private FilingService testService;

    @BeforeEach
    void setUp() {
        testService = new FilingServiceImpl(officerFilingService, officerFilingMapper, logger);
    }

    @Test
    void generateOfficerFilingWhenFound() {
        var filingData = new FilingData(REF_ETAG, REF_OFFICER_ID, RESIGNED_ON);
        var officerFiling = OfficerFiling.builder().referenceOfficerId(REF_OFFICER_ID).referenceEtag(REF_ETAG)
                .resignedOn(RESIGNED_ON_INS).build();

        when(officerFilingService.get(FILING_ID)).thenReturn(Optional.of(officerFiling));
        when(officerFilingMapper.mapFiling(officerFiling)).thenReturn(filingData);

        final var filingApi = testService.generateOfficerFiling(FILING_ID);
        assertThat(filingApi.getKind(), is("officer-filing#termination"));
        assertThat(filingApi.getData(), hasEntry(is("referenceEtag"), is(REF_ETAG)));
        assertThat(filingApi.getData(), hasEntry(is("referenceOfficerId"), is(REF_OFFICER_ID)));
        assertThat(filingApi.getData(), hasEntry(is("resignedOn"), is(RESIGNED_ON)));
    }

    @Test
    void generateOfficerFilingWhenNotFound() {
        var filingData = new FilingData(REF_ETAG, REF_OFFICER_ID, RESIGNED_ON);
        var officerFiling = OfficerFiling.builder().referenceOfficerId(REF_OFFICER_ID).referenceEtag(REF_ETAG)
                .resignedOn(RESIGNED_ON_INS).build();

        when(officerFilingService.get(FILING_ID)).thenReturn(Optional.empty());

        final var exception = assertThrows(ResourceNotFoundException.class, () -> testService.generateOfficerFiling(FILING_ID));
        assertThat(exception.getMessage(), is("Officer not found when generating filing for " + FILING_ID));
    }
}