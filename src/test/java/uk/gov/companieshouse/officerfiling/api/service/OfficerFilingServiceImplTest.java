package uk.gov.companieshouse.officerfiling.api.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.repository.OfficerFilingRepository;

@ExtendWith(MockitoExtension.class)
class OfficerFilingServiceImplTest {
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
}