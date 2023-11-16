package uk.gov.companieshouse.officerfiling.api.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.officerfiling.api.model.entity.Date3Tuple;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.filing.FilingData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface FilingAPIMapper {

    @Mapping(target = "title", source = "data.title")
    @Mapping(target = "firstName", source = "data.firstName")
    @Mapping(target = "middleNames", source = "data.middleNames")
    @Mapping(target = "lastName", source = "data.lastName")
    @Mapping(target = "formerNames", source = "data.formerNames")
    @Mapping(target = "dateOfBirth", source = "data.dateOfBirth")
    @Mapping(target = "appointedOn", source = "data.appointedOn")
    @Mapping(target = "resignedOn", source = "data.resignedOn")
    @Mapping(target = "nationality1", source = "data.nationality1")
    @Mapping(target = "nationality2", source = "data.nationality2")
    @Mapping(target = "nationality3", source = "data.nationality3")
    @Mapping(target = "occupation", source = "data.occupation")
    @Mapping(target = "serviceAddress", source = "data.serviceAddress")
    @Mapping(target = "isServiceAddressSameAsRegisteredOfficeAddress", source = "data.isServiceAddressSameAsRegisteredOfficeAddress")
    @Mapping(target = "residentialAddress", source = "data.residentialAddress")
    @Mapping(target = "isServiceAddressSameAsHomeAddress", source = "data.isServiceAddressSameAsHomeAddress")
    @Mapping(target = "directorAppliedToProtectDetails", source = "data.directorAppliedToProtectDetails")
    @Mapping(target = "consentToAct", source = "data.consentToAct")
    @Mapping(target = "corporateDirector", source = "data.corporateDirector")
    FilingData map(OfficerFiling officerFiling);

    @Mapping(target = "data.dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "dateOfBirth", source = "data.dateOfBirth")
    @Mapping(target = "data.appointedOn", source = "appointedOn")
    @Mapping(target = "appointedOn", source = "data.appointedOn")
    @Mapping(target = "data.resignedOn", source = "resignedOn")
    @Mapping(target = "resignedOn", source = "data.resignedOn")
    default String isoDateOfBirth(Date3Tuple tuple) {
        if (tuple == null) {
            return null;
        }
        return DateTimeFormatter.ISO_LOCAL_DATE.format(
                LocalDate.of(tuple.getYear(), tuple.getMonth(), tuple.getDay()));
    }

}
