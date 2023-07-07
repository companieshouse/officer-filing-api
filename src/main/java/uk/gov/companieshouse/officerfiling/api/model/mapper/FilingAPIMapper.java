package uk.gov.companieshouse.officerfiling.api.model.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.officerfiling.api.model.entity.Date3Tuple;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.filing.FilingData;

@Mapper(componentModel = "spring")
public interface FilingAPIMapper {

    @Mapping(target = "dateOfBirth", source = "data.dateOfBirth")
    @Mapping(target = "firstName", source = "data.firstName")
    @Mapping(target = "lastName", source = "data.lastName")
    @Mapping(target = "resignedOn", source = "data.resignedOn")
    @Mapping(target = "corporateDirector", source = "data.corporateDirector")
    FilingData map(OfficerFiling officerFiling);

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
