package uk.gov.companieshouse.officerfiling.api.model.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import uk.gov.companieshouse.officerfiling.api.model.dto.AddressDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.AppointmentDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.Address;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    OfficerFiling map(AppointmentDto appointmentDto);

    AppointmentDto map(OfficerFiling officerFiling);

    default Instant map(LocalDate date) {
        return date.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant();
    }

    default LocalDate map(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.of("UTC"));
    }

    Address map(AddressDto addressDto);

    AddressDto map(Address address);
}
