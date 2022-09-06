package uk.gov.companieshouse.officerfiling.api.model.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.officerfiling.api.model.dto.TerminationDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TerminationMapper {

    TerminationMapper INSTANCE = Mappers.getMapper(TerminationMapper.class);

    OfficerFiling terminationDtoToOfficerFiling(TerminationDto terminationDto);

    TerminationDto officerFilingToTerminationDto(OfficerFiling officerFiling);

    default Instant map(LocalDate date) {

        return date.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant();
    }

    default LocalDate map(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.of("UTC"));
    }
}
