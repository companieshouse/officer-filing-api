package uk.gov.companieshouse.officerfiling.api.model.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.officerfiling.api.model.dto.TerminationDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;

@ExtendWith(MockitoExtension.class)
class TerminationMapperImplTest {

    static TerminationDto testTerminationDto;
    static OfficerFiling officerFiling;
    static TerminationMapper testTerminationMapper;

    @BeforeAll
    static void setUp() {
        testTerminationDto = new TerminationDto("123456", "234567", LocalDate.of(2022, 8, 14));
        officerFiling =
            OfficerFiling.builder().referenceETag("123456").referenceOfficerId("234567")
                .resignedOn(Instant.parse("2022-08-14T00:00:00Z")).build();
        testTerminationMapper = TerminationMapper.INSTANCE;
    }

    @Test
    void terminationDtoToOfficerFiling() {

        OfficerFiling actual =
            testTerminationMapper.terminationDtoToOfficerFiling(testTerminationDto);

        assertThat(actual.getReferenceETag(), is("123456"));
        assertThat(actual.getReferenceOfficerId(), is("234567"));
        assertThat(actual.getResignedOn(), is(Instant.parse("2022-08-14T00:00:00Z")));
    }

    @Test
    void officerFilingToTerminationDto() {

        TerminationDto actual =
            testTerminationMapper.officerFilingToTerminationDto(officerFiling);

        assertThat(actual.getReferenceETag(), is("123456"));
        assertThat(actual.getReferenceOfficerId(), is("234567"));
        assertThat(actual.getResignedOn(), is(LocalDate.of(2022, 8, 14)));
    }
}