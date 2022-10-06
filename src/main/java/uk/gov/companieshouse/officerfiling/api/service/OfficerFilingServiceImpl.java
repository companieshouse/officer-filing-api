package uk.gov.companieshouse.officerfiling.api.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.repository.OfficerFilingRepository;

@Service
public class OfficerFilingServiceImpl implements OfficerFilingService {
    private final OfficerFilingRepository repository;

    public OfficerFilingServiceImpl(final OfficerFilingRepository repository) {
        this.repository = repository;
    }

    @Override
    public OfficerFiling save(final OfficerFiling filing) {
        return repository.save(filing);
    }

    @Override
    public Optional<OfficerFiling> get(String officerFilingId) {

        return repository.findById(officerFilingId);
    }

    @Override
    public List<OfficerFiling> getFilingsData(String officerFilingId) {

        return repository.findById(officerFilingId).stream().collect(
            Collectors.toList());
    }
}
