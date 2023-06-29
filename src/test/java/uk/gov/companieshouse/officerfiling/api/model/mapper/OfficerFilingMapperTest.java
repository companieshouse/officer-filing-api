package uk.gov.companieshouse.officerfiling.api.model.mapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.officerfiling.api.model.dto.AddressDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.Date3TupleDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.FormerNameDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.IdentificationDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDataDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.Address;
import uk.gov.companieshouse.officerfiling.api.model.entity.Date3Tuple;
import uk.gov.companieshouse.officerfiling.api.model.entity.FormerName;
import uk.gov.companieshouse.officerfiling.api.model.entity.Identification;
import uk.gov.companieshouse.officerfiling.api.model.entity.Links;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;

@ExtendWith(MockitoExtension.class)
class OfficerFilingMapperTest {

    public static final String SELF_URI =
            "/transactions/197315-203316-322377/officers/3AftpfAa8RAq7EC3jKC6l7YDJ88=";
    private Address address;
    private AddressDto addressDto;
    private LocalDate localDate1;
    private Date3Tuple dob1;
    private Instant instant1;
    private List<FormerNameDto> nameDtoList;
    private List<FormerName> nameList;
    private IdentificationDto identificationDto;
    private Identification identification;
    private OfficerFilingMapper testMapper;
    private Links links;
    @Mock
    private Clock clock;

    @BeforeEach
    void setUp() {
        testMapper = Mappers.getMapper(OfficerFilingMapper.class);
        address = Address.builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .careOf("careOf")
                .country("country")
                .locality("locality")
                .poBox("poBox")
                .postalCode("postalCode")
                .premises("premises")
                .region("region")
                .build();
        addressDto = AddressDto.builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .careOf("careOf")
                .country("country")
                .locality("locality")
                .poBox("poBox")
                .postalCode("postalCode")
                .premises("premises")
                .region("region")
                .build();
        localDate1 = LocalDate.of(2019, 11, 5);
        dob1 = new Date3Tuple(12, 9, 1970);
        instant1 = Instant.parse("2019-11-05T00:00:00Z");
        nameDtoList = List.of(new FormerNameDto("f1", "n1"), new FormerNameDto("f2", "n2"),
                new FormerNameDto("f3", "n3"));
        nameList = List.of(new FormerName("f1", "n1"), new FormerName("f2", "n2"),
                new FormerName("f3", "n3"));
        identification = new Identification("type", "auth", "legal", "place", "number");
        identificationDto = new IdentificationDto("type", "auth", "legal", "place", "number");
        links = new Links(URI.create(SELF_URI), URI.create(SELF_URI + "validation_status"));
    }

    @Test
    void appointmentDtoToOfficerFiling() {
        final var dataDto = OfficerFilingDataDto.builder().address(addressDto)
                .addressSameAsRegisteredOfficeAddress(true)
                .appointedOn(localDate1)
                .countryOfResidence("countryOfResidence")
                .dateOfBirth(new Date3TupleDto(dob1.getDay(), dob1.getMonth(), dob1.getYear()))
                .formerNames(nameDtoList)
                .name("name")
                .referenceEtag("referenceEtag")
                .referenceAppointmentId("referenceAppointmentId")
                .nationality("nation")
                .occupation("work")
                .referenceOfficerListEtag("list")
                .residentialAddress(addressDto)
                .residentialAddressSameAsCorrespondenceAddress(true)
                .resignedOn(localDate1)
                .build();

        final var dto = OfficerFilingDto.builder()
                .identification(identificationDto)
                .officerFilingData(dataDto)
                .build();

        final var filing = testMapper.map(dto);

        assertThat(filing.getOfficerFilingData().getAddress(), is(equalTo(address)));
        assertThat(filing.getOfficerFilingData().getAddressSameAsRegisteredOfficeAddress(), is(true));
        assertThat(filing.getOfficerFilingData().getAppointedOn(),
                is(localDate1.atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertThat(filing.getOfficerFilingData().getCountryOfResidence(), is("countryOfResidence"));
        assertThat(filing.getCreatedAt(), is(nullValue()));
        assertThat(filing.getOfficerFilingData().getDateOfBirth(), is(dob1));
        assertThat(filing.getOfficerFilingData().getFormerNames(), is(equalTo(nameList)));
        assertThat(filing.getIdentification(), is(equalTo(identification)));
        assertThat(filing.getKind(), is(nullValue()));
        assertThat(filing.getLinks(), is(nullValue()));
        assertThat(filing.getOfficerFilingData().getName(), is("name"));
        assertThat(filing.getOfficerFilingData().getOfficerRole(), is(nullValue()));
        assertThat(filing.getOfficerFilingData().getReferenceEtag(), is("referenceEtag"));
        assertThat(filing.getOfficerFilingData().getReferenceAppointmentId(), is("referenceAppointmentId"));
        assertThat(filing.getOfficerFilingData().getNationality(), is("nation"));
        assertThat(filing.getOfficerFilingData().getOccupation(), is("work"));
        assertThat(filing.getOfficerFilingData().getReferenceEtag(), is("referenceEtag"));
        assertThat(filing.getOfficerFilingData().getReferenceAppointmentId(), is("referenceAppointmentId"));
        assertThat(filing.getOfficerFilingData().getReferenceOfficerListEtag(), is("list"));
        assertThat(filing.getOfficerFilingData().getResignedOn(), is(localDate1.atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertThat(filing.getOfficerFilingData().getResidentialAddress(), is(equalTo(address)));
        assertThat(filing.getOfficerFilingData().getResidentialAddressSameAsCorrespondenceAddress(), is(true));
        assertThat(filing.getOfficerFilingData().getStatus(), is(nullValue()));
        assertThat(filing.getUpdatedAt(), is(nullValue()));
    }

    @Test
    void nullAppointmentDtoToOfficerFiling() {
        final var filing = testMapper.map((OfficerFilingDto) null);

        assertThat(filing, is(nullValue()));
    }

    @Test
    void emptyAppointmentDtoToOfficerFiling() {
        final var dtoData = OfficerFilingDataDto.builder().formerNames(Collections.singletonList(null)).build();
        final var dto = OfficerFilingDto.builder().officerFilingData(dtoData)
                .build();
        var offData = new OfficerFilingData(
                null,
                null,
                null,
                null,
                null,
                Collections.emptyList(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        final var now = clock.instant();
        final var emptyFiling = OfficerFiling.builder().createdAt(now).updatedAt(now).officerFilingData(offData)
                .build();

        final var filing = testMapper.map(dto);

        assertThat(filing, is(equalTo(emptyFiling)));
        assertThat(filing.getOfficerFilingData().getAddressSameAsRegisteredOfficeAddress(), is(nullValue()));
        assertThat(filing.getOfficerFilingData().getResidentialAddressSameAsCorrespondenceAddress(), is(nullValue()));

    }

    @Test
    void emptyAppointmentNullFormerNameDtoToOfficerFiling() {
        final var dtoData = OfficerFilingDataDto.builder().formerNames(Collections.singletonList(null)).build();
        final var dto = OfficerFilingDto.builder().officerFilingData(dtoData)
                .build();
        var offData = new OfficerFilingData(
                null,
                null,
                null,
                null,
                null,
                Collections.emptyList(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).officerFilingData(offData)
                .build();
        final var emptyFiling = testMapper.map(dto);

        assertThat(filing, is(equalTo(emptyFiling)));

    }

    @Test
    void officerFilingToOfficerFilingDto() {
        var offData = new OfficerFilingData(
                address,
                true,
                localDate1.atStartOfDay().toInstant(ZoneOffset.UTC),
                "countryOfResidence",
                dob1,
                nameList,
                "name",
                "firstName",
                "lastName",
                "nation",
                "work",
                "role",
                "referenceEtag",
                "referenceAppointmentId",
                "list",
                instant1,
                "status",
                address,
                true,
false
                );
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).officerFilingData(offData)
                .identification(identification).build();

        final var dto = testMapper.map(filing);

        assertThat(dto.getOfficerFilingData().getAddress(), is(equalTo(addressDto)));
        assertThat(dto.getOfficerFilingData().getAddressSameAsRegisteredOfficeAddress(), is(true));
        assertThat(dto.getOfficerFilingData().getAppointedOn(), is(localDate1));
        assertThat(dto.getOfficerFilingData().getCountryOfResidence(), is("countryOfResidence"));
        assertThat(dto.getOfficerFilingData().getDateOfBirth(),
                is(new Date3TupleDto(dob1.getDay(), dob1.getMonth(), dob1.getYear())));
        assertThat(dto.getOfficerFilingData().getFormerNames(), is(equalTo(nameDtoList)));
        assertThat(dto.getIdentification(), is(equalTo(identificationDto)));
        assertThat(dto.getOfficerFilingData().getName(), is("name"));
        assertThat(dto.getOfficerFilingData().getReferenceEtag(), is("referenceEtag"));
        assertThat(dto.getOfficerFilingData().getReferenceAppointmentId(), is("referenceAppointmentId"));
        assertThat(dto.getOfficerFilingData().getNationality(), is("nation"));
        assertThat(dto.getOfficerFilingData().getOccupation(), is("work"));
        assertThat(dto.getOfficerFilingData().getReferenceOfficerListEtag(), is("list"));
        assertThat(dto.getOfficerFilingData().getResidentialAddress(), is(equalTo(addressDto)));
        assertThat(dto.getOfficerFilingData().getResidentialAddressSameAsCorrespondenceAddress(), is(true));
        assertThat(dto.getOfficerFilingData().getResignedOn(), is(localDate1));
    }

    @Test
    void nullOfficerFilingFormerNamesToAppointmentDto() {
        final var dto = testMapper.map((OfficerFiling) null);

        assertThat(dto, is(nullValue()));
    }

    @Test
    void emptyOfficerFilingNullFormerNamesToAppointmentDto() {
        final var filing = OfficerFiling.builder()
                .build();
        final var emptyDto = OfficerFilingDto.builder()
                .build();

        final var dto = testMapper.map(filing);

        assertThat(dto, is(equalTo(emptyDto)));
    }

    @Test
    void emptyOfficerFilingNullFormerNameToAppointmentDto() {
        var offData = new OfficerFilingData(
                null,
                null,
                null,
                null,
                null,
                Collections.singletonList(null),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).officerFilingData(offData)
                .build();

        final var dtoData = OfficerFilingDataDto.builder().formerNames(Collections.singletonList(null)).build();
        final var emptyDto = OfficerFilingDto.builder().officerFilingData(dtoData)
                .build();

        final var dto = testMapper.map(filing);

        assertThat(dto, is(equalTo(emptyDto)));
    }

    @Test
    void emptyOfficerFilingToAppointmentDto() {
        var offData = new OfficerFilingData(
                null,
                null,
                null,
                null,
                null,
                Collections.singletonList(null),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).officerFilingData(offData)
                .build();
        final var dtoData = OfficerFilingDataDto.builder().formerNames(Collections.emptyList()).build();
        final var emptyDto = OfficerFilingDto.builder().officerFilingData(dtoData)
                .build();

        final var dto = testMapper.map(filing);

        assertThat(dto, is(equalTo(emptyDto)));
        assertThat(dto.getOfficerFilingData().getAddressSameAsRegisteredOfficeAddress(), is(nullValue()));
        assertThat(dto.getOfficerFilingData().getResidentialAddressSameAsCorrespondenceAddress(), is(nullValue()));
    }

}