package uk.gov.companieshouse.officerfiling.api.model.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;

@Mapper(componentModel = "spring")
public interface OfficerFilingMapper {

    OfficerFiling map(OfficerFilingDto officerFilingDto);

    OfficerFilingDto map(OfficerFiling officerFiling);

    default Instant map(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    default LocalDate map(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDate.ofInstant(instant, ZoneId.of("UTC"));
    }

}
