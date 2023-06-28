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
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.officerfiling.api.model.dto.AddressDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.Date3TupleDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.FormerNameDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.IdentificationDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.Address;
import uk.gov.companieshouse.officerfiling.api.model.entity.Date3Tuple;
import uk.gov.companieshouse.officerfiling.api.model.entity.FormerName;
import uk.gov.companieshouse.officerfiling.api.model.entity.Identification;
import uk.gov.companieshouse.officerfiling.api.model.entity.Links;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;

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
        final var dto = OfficerFilingDto.builder()
                .address(addressDto)
                .addressSameAsRegisteredOfficeAddress(true)
                .appointedOn(localDate1)
                .countryOfResidence("countryOfResidence")
                .dateOfBirth(new Date3TupleDto(dob1.getDay(), dob1.getMonth(), dob1.getYear()))
                .formerNames(nameDtoList)
                .identification(identificationDto)
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

        final var filing = testMapper.map(dto);

        assertThat(filing.getData().getAddress(), is(equalTo(address)));
        assertThat(filing.getData().getAddressSameAsRegisteredOfficeAddress(), is(true));
        assertThat(filing.getData().getAppointedOn(),
                is(localDate1.atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertThat(filing.getData().getCountryOfResidence(), is("countryOfResidence"));
        assertThat(filing.getCreatedAt(), is(nullValue()));
        assertThat(filing.getData().getDateOfBirth(), is(dob1));
        assertThat(filing.getData().getFormerNames(), is(equalTo(nameList)));
        assertThat(filing.getIdentification(), is(equalTo(identification)));
        assertThat(filing.getKind(), is(nullValue()));
        assertThat(filing.getLinks(), is(nullValue()));
        assertThat(filing.getData().getName(), is("name"));
        assertThat(filing.getData().getOfficerRole(), is(nullValue()));
        assertThat(filing.getData().getReferenceEtag(), is("referenceEtag"));
        assertThat(filing.getData().getReferenceAppointmentId(), is("referenceAppointmentId"));
        assertThat(filing.getData().getNationality(), is("nation"));
        assertThat(filing.getData().getOccupation(), is("work"));
        assertThat(filing.getData().getReferenceEtag(), is("referenceEtag"));
        assertThat(filing.getData().getReferenceAppointmentId(), is("referenceAppointmentId"));
        assertThat(filing.getData().getReferenceOfficerListEtag(), is("list"));
        assertThat(filing.getData().getResignedOn(), is(localDate1.atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertThat(filing.getData().getResidentialAddress(), is(equalTo(address)));
        assertThat(filing.getData().getResidentialAddressSameAsCorrespondenceAddress(), is(true));
        assertThat(filing.getData().getStatus(), is(nullValue()));
        assertThat(filing.getUpdatedAt(), is(nullValue()));
    }

    @Test
    void nullAppointmentDtoToOfficerFiling() {
        final var filing = testMapper.map((OfficerFilingDto) null);

        assertThat(filing, is(nullValue()));
    }

    @Test
    void emptyAppointmentDtoToOfficerFiling() {
        final var dto = OfficerFilingDto.builder().formerNames(Collections.singletonList(null))
                .build();
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
        final var emptyFiling = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();

        final var filing = testMapper.map(dto);

        assertThat(filing, is(equalTo(emptyFiling)));
        assertThat(filing.getData().getAddressSameAsRegisteredOfficeAddress(), is(nullValue()));
        assertThat(filing.getData().getResidentialAddressSameAsCorrespondenceAddress(), is(nullValue()));

    }

    @Test
    void emptyAppointmentNullFormerNameDtoToOfficerFiling() {
        final var dto = OfficerFilingDto.builder().formerNames(Collections.singletonList(null))
                .build();
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
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
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
                "occupation",
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
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();

        final var dto = testMapper.map(filing);

        assertThat(dto.getAddress(), is(equalTo(addressDto)));
        assertThat(dto.getAddressSameAsRegisteredOfficeAddress(), is(true));
        assertThat(dto.getAppointedOn(), is(localDate1));
        assertThat(dto.getCountryOfResidence(), is("countryOfResidence"));
        assertThat(dto.getDateOfBirth(),
                is(new Date3TupleDto(dob1.getDay(), dob1.getMonth(), dob1.getYear())));
        assertThat(dto.getFormerNames(), is(equalTo(nameDtoList)));
        assertThat(dto.getIdentification(), is(equalTo(identificationDto)));
        assertThat(dto.getName(), is("name"));
        assertThat(dto.getReferenceEtag(), is("referenceEtag"));
        assertThat(dto.getReferenceAppointmentId(), is("referenceAppointmentId"));
        assertThat(dto.getNationality(), is("nation"));
        assertThat(dto.getOccupation(), is("work"));
        assertThat(dto.getReferenceOfficerListEtag(), is("list"));
        assertThat(dto.getResidentialAddress(), is(equalTo(addressDto)));
        assertThat(dto.getResidentialAddressSameAsCorrespondenceAddress(), is(true));
        assertThat(dto.getResignedOn(), is(localDate1));
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
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();

        final var emptyDto = OfficerFilingDto.builder().formerNames(Collections.singletonList(null))
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
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();
        final var emptyDto = OfficerFilingDto.builder().formerNames(Collections.emptyList())
                .build();

        final var dto = testMapper.map(filing);

        assertThat(dto, is(equalTo(emptyDto)));
        assertThat(dto.getAddressSameAsRegisteredOfficeAddress(), is(nullValue()));
        assertThat(dto.getResidentialAddressSameAsCorrespondenceAddress(), is(nullValue()));
    }

}