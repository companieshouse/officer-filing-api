package uk.gov.companieshouse.officerfiling.api.model.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.officerfiling.api.model.dto.AppointmentDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    @Mapping(target = "referenceETag", ignore = true)
    @Mapping(target = "referenceOfficerId", ignore = true)
    @Mapping(target = "resignedOn", ignore = true)
    OfficerFiling map(AppointmentDto appointmentDto);

    AppointmentDto map(OfficerFiling officerFiling);

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
