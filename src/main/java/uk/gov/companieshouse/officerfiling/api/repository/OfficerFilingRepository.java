package uk.gov.companieshouse.officerfiling.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;

@RepositoryRestResource(path="filings", collectionResourceRel="filings")
public interface OfficerFilingRepository extends MongoRepository<OfficerFiling, String> {

}
