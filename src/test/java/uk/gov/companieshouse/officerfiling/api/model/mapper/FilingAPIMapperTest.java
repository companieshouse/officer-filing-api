package uk.gov.companieshouse.officerfiling.api.model.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;

@ExtendWith(MockitoExtension.class)
class FilingAPIMapperTest {

    private LocalDate localDate1;
    private Instant dob1;
    private String dob1String;
    private Instant instant1;
    private FilingAPIMapper testMapper;
    @Mock
    private Clock clock;

    @BeforeEach
    void setUp() {
        testMapper = Mappers.getMapper(FilingAPIMapper.class);
        localDate1 = LocalDate.of(2019, 11, 5);
        dob1 = Instant.parse("1970-09-12T00:00:00Z");
        dob1String = "1970-09-12";
        instant1 = Instant.parse("2019-11-05T00:00:00Z");
    }

    @Test
    void officerFilingToFilingAPI() {
        var offData = OfficerFilingData.builder()
                .isServiceAddressSameAsRegisteredOfficeAddress(true)
                .appointedOn(localDate1.atStartOfDay().toInstant(ZoneOffset.UTC))
                .countryOfResidence("countryOfResidence")
                .dateOfBirth(dob1)
                .name("name")
                .firstName("firstName")
                .lastName("lastName")
                .nationality1("nation")
                .occupation("work")
                .officerRole("role")
                .referenceEtag("referenceEtag")
                .referenceAppointmentId("referenceAppointmentId")
                .referenceOfficerListEtag("list")
                .resignedOn(instant1)
                .status("status")
                .isServiceAddressSameAsRegisteredOfficeAddress(true)
                .corporateDirector(false)
                .build();
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();

        final var filingApi = testMapper.map(filing);

        assertThat(filingApi.getDateOfBirth(), is("1970-09-12T00:00:00Z"));
        assertThat(filing.getData().getCorporateDirector(), is(false));
        assertThat(filing.getData().getResignedOn(), is(localDate1.atStartOfDay().toInstant(ZoneOffset.UTC)));
    }
}