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
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
import uk.gov.companieshouse.officerfiling.api.model.filing.FilingData;

@Mapper(componentModel = "spring")
public interface OfficerFilingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "kind", ignore = true)
    @Mapping(target = "links", ignore = true)
    @Mapping(source = "officerFilingData", target = "officerFilingData")
    @Mapping(target = "updatedAt", ignore = true)
    OfficerFiling map(OfficerFilingDto officerFilingDto);


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
    default String isoDateOfBirth(Date3Tuple tuple) {
        if (tuple == null) {
            return null;
        }
        return DateTimeFormatter.ISO_LOCAL_DATE.format(
                LocalDate.of(tuple.getYear(), tuple.getMonth(), tuple.getDay()));
    }
}
