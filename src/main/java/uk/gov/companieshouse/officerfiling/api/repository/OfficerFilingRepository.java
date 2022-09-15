package uk.gov.companieshouse.officerfiling.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;

public interface OfficerFilingRepository extends MongoRepository<OfficerFiling, String> {

}
