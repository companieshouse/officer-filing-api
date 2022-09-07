package uk.gov.companieshouse.officerfiling.api.model.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.officerfiling.api.model.dto.TerminationDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;

class TerminationMapperTest {

    private TerminationMapper testMapper;

    @BeforeEach
    void setUp() {
        testMapper = Mappers.getMapper(TerminationMapper.class);
    }

    @Test
    void terminationDtoToOfficerFiling() {
        var dto = new TerminationDto("123456", "234567", LocalDate.of(2022, 8, 14));

        OfficerFiling actual = testMapper.map(dto);

        assertThat(actual.getReferenceETag(), is("123456"));
        assertThat(actual.getReferenceOfficerId(), is("234567"));
        assertThat(actual.getResignedOn(), is(Instant.parse("2022-08-14T00:00:00Z")));
    }

    @Test
    void officerFilingToTerminationDto() {
        var filing = OfficerFiling.builder()
                .referenceETag("123456")
                .referenceOfficerId("234567")
                .resignedOn(Instant.parse("2022-08-14T00:00:00Z"))
                .build();

        TerminationDto actual = testMapper.map(filing);

        assertThat(actual.getReferenceETag(), is("123456"));
        assertThat(actual.getReferenceOfficerId(), is("234567"));
        assertThat(actual.getResignedOn(), is(LocalDate.of(2022, 8, 14)));
    }
}