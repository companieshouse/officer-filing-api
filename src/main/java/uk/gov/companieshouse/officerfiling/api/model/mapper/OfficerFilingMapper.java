package uk.gov.companieshouse.officerfiling.api.model.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.Date3Tuple;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.filing.FilingData;

@Mapper(componentModel = "spring")
public interface OfficerFilingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "kind", ignore = true)
    @Mapping(target = "links", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "data.serviceAddress", source = "serviceAddress")
    @Mapping(target = "data.addressSameAsRegisteredOfficeAddress", source = "addressSameAsRegisteredOfficeAddress")
    @Mapping(target = "data.appointedOn", source = "appointedOn")
    @Mapping(target = "data.countryOfResidence", source = "countryOfResidence")
    @Mapping(target = "data.dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "data.nationality1", source = "nationality1")
    @Mapping(target = "data.nationality2", source = "nationality2")
    @Mapping(target = "data.nationality3", source = "nationality3")
    @Mapping(target = "data.nationality2Link", source = "nationality2Link")
    @Mapping(target = "data.nationality3Link", source = "nationality3Link")
    @Mapping(target = "data.occupation", source = "occupation")
    @Mapping(target = "data.title", source = "title")
    @Mapping(target = "data.formerNames", source = "formerNames")
    @Mapping(target = "data.name", source = "name")
    @Mapping(target = "data.firstName", source = "firstName")
    @Mapping(target = "data.middleNames", source = "middleNames")
    @Mapping(target = "data.lastName", source = "lastName")
    @Mapping(target = "data.referenceEtag", source = "referenceEtag")
    @Mapping(target = "data.referenceAppointmentId", source = "referenceAppointmentId")
    @Mapping(target = "data.referenceOfficerListEtag", source = "referenceOfficerListEtag")
    @Mapping(target = "data.resignedOn", source = "resignedOn")
    @Mapping(target = "data.residentialAddress", source = "residentialAddress")
    @Mapping(target = "data.residentialAddressBackLink", source = "residentialAddressBackLink")
    @Mapping(target = "data.residentialAddressSameAsCorrespondenceAddress", source = "residentialAddressSameAsCorrespondenceAddress")
    @Mapping(target = "identification.identificationType", source = "identification.identificationType")
    @Mapping(target = "identification.legalAuthority", source = "identification.legalAuthority")
    @Mapping(target = "identification.legalForm", source = "identification.legalForm")
    @Mapping(target = "identification.placeRegistered", source = "identification.placeRegistered")
    @Mapping(target = "identification.registrationNumber", source = "identification.registrationNumber")
    OfficerFiling map(OfficerFilingDto officerFilingDto);


    @Mapping(target = "serviceAddress", source = "data.serviceAddress")
    @Mapping(target = "addressSameAsRegisteredOfficeAddress", source = "data.addressSameAsRegisteredOfficeAddress")
    @Mapping(target = "appointedOn", source = "data.appointedOn")
    @Mapping(target = "countryOfResidence", source = "data.countryOfResidence")
    @Mapping(target = "dateOfBirth", source = "data.dateOfBirth")
    @Mapping(target = "nationality1", source = "data.nationality1")
    @Mapping(target = "nationality2", source = "data.nationality2")
    @Mapping(target = "nationality3", source = "data.nationality3")
    @Mapping(target = "nationality2Link", source = "data.nationality2Link")
    @Mapping(target = "nationality3Link", source = "data.nationality3Link")
    @Mapping(target = "occupation", source = "data.occupation")
    @Mapping(target = "title", source = "data.title")
    @Mapping(target = "name", source = "data.name")
    @Mapping(target = "formerNames", source = "data.formerNames")
    @Mapping(target = "firstName", source = "data.firstName")
    @Mapping(target = "middleNames", source = "data.middleNames")
    @Mapping(target = "lastName", source = "data.lastName")
    @Mapping(target = "referenceEtag", source = "data.referenceEtag")
    @Mapping(target = "referenceAppointmentId", source = "data.referenceAppointmentId")
    @Mapping(target = "referenceOfficerListEtag", source = "data.referenceOfficerListEtag")
    @Mapping(target = "resignedOn", source = "data.resignedOn")
    @Mapping(target = "residentialAddress", source = "data.residentialAddress")
    @Mapping(target = "residentialAddressBackLink", source = "data.residentialAddressBackLink")
    @Mapping(target = "residentialAddressSameAsCorrespondenceAddress", source = "data.residentialAddressSameAsCorrespondenceAddress")
    @Mapping(target = "identification.identificationType", source = "identification.identificationType")
    @Mapping(target = "identification.legalAuthority", source = "identification.legalAuthority")
    @Mapping(target = "identification.legalForm", source = "identification.legalForm")
    @Mapping(target = "identification.placeRegistered", source = "identification.placeRegistered")
    @Mapping(target = "identification.registrationNumber", source = "identification.registrationNumber")
    OfficerFilingDto map(OfficerFiling officerFiling);

    default Instant map(final LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    default LocalDate map(final Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDate.ofInstant(instant, ZoneId.of("UTC"));
    }

    FilingData mapFiling(final OfficerFiling entity);

    @Mapping(target = "data.resignedOn", source = "resignedOn")
    default String isoDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return DateTimeFormatter.ISO_LOCAL_DATE.format(instant.atOffset(ZoneOffset.UTC));
    }

    @Mapping(target = "data.dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "dateOfBirth", source = "data.dateOfBirth")
    default String isoDateOfBirth(Date3Tuple tuple) {
        if (tuple == null) {
            return null;
        }
        return DateTimeFormatter.ISO_LOCAL_DATE.format(
                LocalDate.of(tuple.getYear(), tuple.getMonth(), tuple.getDay()));
    }
}
