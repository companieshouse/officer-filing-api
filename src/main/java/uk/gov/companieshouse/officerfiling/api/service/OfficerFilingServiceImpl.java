package uk.gov.companieshouse.officerfiling.api.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.repository.OfficerFilingRepository;

@Service
public class OfficerFilingServiceImpl implements OfficerFilingService {
    private OfficerFilingRepository repository;

    public OfficerFilingServiceImpl(final OfficerFilingRepository repository) {
        this.repository = repository;
    }

    @Override
    public OfficerFiling save(final OfficerFiling filing) {
        return repository.save(filing);
    }
}
