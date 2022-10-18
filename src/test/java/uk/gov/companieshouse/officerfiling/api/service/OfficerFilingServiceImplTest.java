package uk.gov.companieshouse.officerfiling.api.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.repository.OfficerFilingRepository;

@ExtendWith(MockitoExtension.class)
class OfficerFilingServiceImplTest {
    public static final String FILING_ID = "6332aa6ed28ad2333c3a520a";
    private OfficerFilingService testService;

    @Mock
    private OfficerFilingRepository repository;
    @Mock
    private OfficerFiling filing;

    @BeforeEach
    void setUp() {
        testService = new OfficerFilingServiceImpl(repository);
    }

    @Test
    void save() {
        testService.save(filing);

        verify(repository).save(filing);
    }

    @Test
    void getWhenFound() {
        var filing = OfficerFiling.builder().build();
        when(repository.findById(FILING_ID)).thenReturn(Optional.of(filing));
        final var officerFiling = testService.get(FILING_ID);

        assertThat(officerFiling.isPresent(), is(true));
    }

    @Test
    void getWhenNotFound() {
        when(repository.findById(FILING_ID)).thenReturn(Optional.empty());
        final var officerFiling = testService.get(FILING_ID);

        assertThat(officerFiling.isPresent(), is(false));
    }

}