package uk.gov.companieshouse.officerfiling.api.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
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
    private static final String RESIGNED_ON_STR = "2022-10-05";
    private static final Instant RESIGNED_ON_INS = Instant.parse("2022-10-05T00:00:00Z");
    public static final String FIRSTNAME = "FIRSTNAME";
    public static final String LASTNAME = "LASTNAME";
    public static final String DATE_OF_BIRTH_STR = "2000-01-01";
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
        final var filingData = new FilingData(FIRSTNAME, LASTNAME, DATE_OF_BIRTH_STR, RESIGNED_ON_STR);
        final var officerFiling = OfficerFiling.builder()
                .referenceOfficerId(REF_OFFICER_ID)
                .referenceEtag(REF_ETAG)
                .resignedOn(RESIGNED_ON_INS)
                .build();

        when(officerFilingService.get(FILING_ID)).thenReturn(Optional.of(officerFiling));
        when(officerFilingMapper.mapFiling(officerFiling)).thenReturn(filingData);

        final var filingApi = testService.generateOfficerFiling(FILING_ID);

        final Map<String, Object> expectedMap =
                Map.of("first_name", FIRSTNAME, "last_name", LASTNAME,
                        "date_of_birth", DATE_OF_BIRTH_STR,
                        "resigned_on", RESIGNED_ON_STR);

        assertThat(filingApi.getData(), is(equalTo(expectedMap)));
        assertThat(filingApi.getKind(), is("officer-filing#termination"));
    }

    @Test
    void generateOfficerFilingWhenNotFound() {
        when(officerFilingService.get(FILING_ID)).thenReturn(Optional.empty());

        final var exception = assertThrows(ResourceNotFoundException.class,
                () -> testService.generateOfficerFiling(FILING_ID));

        assertThat(exception.getMessage(),
                is("Officer not found when generating filing for " + FILING_ID));
    }
}