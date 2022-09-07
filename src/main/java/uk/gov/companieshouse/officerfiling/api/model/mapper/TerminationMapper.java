package uk.gov.companieshouse.officerfiling.api.model.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.officerfiling.api.model.dto.TerminationDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;

@Mapper(componentModel = "spring")
public interface TerminationMapper {

    @Mapping(target = "address", ignore = true)
    @Mapping(target = "addressSameAsRegisteredOfficeAddress", ignore = true)
    @Mapping(target = "appointedOn", ignore = true)
    @Mapping(target = "countryOfResidence", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "eTag", ignore = true)
    @Mapping(target = "formerNames", ignore = true)
    @Mapping(target = "identification", ignore = true)
    @Mapping(target = "kind", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "nationality", ignore = true)
    @Mapping(target = "occupation", ignore = true)
    @Mapping(target = "officerRole", ignore = true)
    @Mapping(target = "referenceOfficerListETag", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "residentialAddress", ignore = true)
    @Mapping(target = "residentialAddressSameAsCorrespondenceAddress", ignore = true)
    OfficerFiling map(TerminationDto terminationDto);

    TerminationDto map(OfficerFiling officerFiling);

    default Instant map(LocalDate date) {

        return date.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant();
    }

    default LocalDate map(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.of("UTC"));
    }
}
