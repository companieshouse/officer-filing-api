package uk.gov.companieshouse.officerfiling.api.model.mapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.officerfiling.api.model.dto.Date3TupleDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.AddressDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.LinksDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.FormerNameDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.IdentificationDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.Address;
import uk.gov.companieshouse.officerfiling.api.model.entity.Date3Tuple;
import uk.gov.companieshouse.officerfiling.api.model.entity.FormerName;
import uk.gov.companieshouse.officerfiling.api.model.entity.Identification;
import uk.gov.companieshouse.officerfiling.api.model.entity.Links;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;

class OfficerFilingMapperTest {

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
        links = new Links(URI.create(
                "/transactions/197315-203316-322377/officers/3AftpfAa8RAq7EC3jKC6l7YDJ88="),
                "status");
    }

    @Test
    void appointmentDtoToOfficerFiling() {
        var dto = OfficerFilingDto.builder()
                .address(addressDto)
                .addressSameAsRegisteredOfficeAddress(true)
                .appointedOn(localDate1)
                .countryOfResidence("countryOfResidence")
                .createdAt(instant1)
                .dateOfBirth(new Date3TupleDto(dob1.getDay(), dob1.getMonth(), dob1.getYear()))
                .formerNames(nameDtoList)
                .identification(identificationDto)
                .kind("kind")
                .links(new LinksDto(links.getSelf(), links.getValidationStatus()))
                .name("name")
                .officerRole("role")
                .referenceETag("referenceETag")
                .referenceOfficerId("referenceOfficerId")
                .nationality("nation")
                .occupation("work")
                .referenceOfficerListETag("list")
                .residentialAddress(addressDto)
                .residentialAddressSameAsCorrespondenceAddress(true)
                .resignedOn(localDate1)
                .status("status")
                .updatedAt(instant1)
                .build();

        final var filing = testMapper.map(dto);

        assertThat(filing.getAddress(), is(equalTo(address)));
        assertThat(filing.getAddressSameAsRegisteredOfficeAddress(), is(true));
        assertThat(filing.getAppointedOn(),
                is(localDate1.atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertThat(filing.getCountryOfResidence(), is("countryOfResidence"));
        assertThat(filing.getCreatedAt(), is(instant1));
        assertThat(filing.getDateOfBirth(), is(dob1));
        assertThat(filing.getFormerNames(), is(equalTo(nameList)));
        assertThat(filing.getIdentification(), is(equalTo(identification)));
        assertThat(filing.getKind(), is("kind"));
        assertThat(filing.getLinks(), is(equalTo(links)));
        assertThat(filing.getName(), is("name"));
        assertThat(filing.getOfficerRole(), is("role"));
        assertThat(filing.getReferenceETag(), is("referenceETag"));
        assertThat(filing.getReferenceOfficerId(), is("referenceOfficerId"));
        assertThat(filing.getNationality(), is("nation"));
        assertThat(filing.getOccupation(), is("work"));
        assertThat(filing.getReferenceETag(), is("referenceETag"));
        assertThat(filing.getReferenceOfficerId(), is("referenceOfficerId"));
        assertThat(filing.getReferenceOfficerListETag(), is("list"));
        assertThat(filing.getResignedOn(), is(localDate1.atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertThat(filing.getResidentialAddress(), is(equalTo(address)));
        assertThat(filing.getResidentialAddressSameAsCorrespondenceAddress(), is(true));
        assertThat(filing.getStatus(), is("status"));
        assertThat(filing.getUpdatedAt(), is(instant1));
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
        final var emptyFiling = OfficerFiling.builder().formerNames(Collections.singletonList(null))
                .build();

        final var filing = testMapper.map(dto);

        assertThat(filing, is(equalTo(emptyFiling)));
        assertThat(filing.getAddressSameAsRegisteredOfficeAddress(), is(nullValue()));
        assertThat(filing.getResidentialAddressSameAsCorrespondenceAddress(), is(nullValue()));

    }

    @Test
    void emptyAppointmentNullFormerNameDtoToOfficerFiling() {
        final var dto = OfficerFilingDto.builder().formerNames(Collections.singletonList(null))
                .build();
        final var emptyFiling = OfficerFiling.builder().formerNames(Collections.singletonList(null))
                .build();

        final var filing = testMapper.map(dto);

        assertThat(filing, is(equalTo(emptyFiling)));

    }

    @Test
    void officerFilingToOfficerFilingDto() {
        var filing = OfficerFiling.builder()
                .address(address)
                .addressSameAsRegisteredOfficeAddress(true)
                .appointedOn(localDate1.atStartOfDay().toInstant(ZoneOffset.UTC))
                .countryOfResidence("countryOfResidence")
                .createdAt(instant1)
                .dateOfBirth(dob1)
                .formerNames(nameList)
                .identification(identification)
                .kind("kind")
                .links(links)
                .name("name")
                .officerRole("role")
                .nationality("nation")
                .occupation("work")
                .referenceETag("referenceETag")
                .referenceOfficerId("referenceOfficerId")
                .referenceOfficerListETag("list")
                .residentialAddress(address)
                .residentialAddressSameAsCorrespondenceAddress(true)
                .resignedOn(instant1)
                .status("status")
                .updatedAt(instant1)
                .build();

        final var dto = testMapper.map(filing);

        assertThat(dto.getAddress(), is(equalTo(addressDto)));
        assertThat(dto.getAddressSameAsRegisteredOfficeAddress(), is(true));
        assertThat(dto.getAppointedOn(), is(localDate1));
        assertThat(dto.getCountryOfResidence(), is("countryOfResidence"));
        assertThat(dto.getCreatedAt(), is(instant1));
        assertThat(dto.getDateOfBirth(),
                is(new Date3TupleDto(dob1.getDay(), dob1.getMonth(), dob1.getYear())));
        assertThat(dto.getFormerNames(), is(equalTo(nameDtoList)));
        assertThat(dto.getIdentification(), is(equalTo(identificationDto)));
        assertThat(dto.getKind(), is("kind"));
        assertThat(dto.getLinks(), is(new LinksDto(links.getSelf(), links.getValidationStatus())));
        assertThat(dto.getName(), is("name"));
        assertThat(dto.getOfficerRole(), is("role"));
        assertThat(dto.getReferenceETag(), is("referenceETag"));
        assertThat(dto.getReferenceOfficerId(), is("referenceOfficerId"));
        assertThat(dto.getNationality(), is("nation"));
        assertThat(dto.getOccupation(), is("work"));
        assertThat(dto.getReferenceOfficerListETag(), is("list"));
        assertThat(dto.getResidentialAddress(), is(equalTo(addressDto)));
        assertThat(dto.getResidentialAddressSameAsCorrespondenceAddress(), is(true));
        assertThat(dto.getResignedOn(), is(localDate1));
        assertThat(dto.getStatus(), is("status"));
        assertThat(dto.getUpdatedAt(), is(instant1));
    }

    @Test
    void nullOfficerFilingFormerNamesToAppointmentDto() {
        final var dto = testMapper.map((OfficerFiling) null);

        assertThat(dto, is(nullValue()));
    }

    @Test
    void emptyOfficerFilingNullFormerNamesToAppointmentDto() {
        var filing = OfficerFiling.builder()
                .build();
        var emptyDto = OfficerFilingDto.builder()
                .build();

        final var dto = testMapper.map(filing);

        assertThat(dto, is(equalTo(emptyDto)));
    }

    @Test
    void emptyOfficerFilingNullFormerNameToAppointmentDto() {
        var filing = OfficerFiling.builder().formerNames(Collections.singletonList(null))
                .build();
        var emptyDto = OfficerFilingDto.builder().formerNames(Collections.singletonList(null))
                .build();

        final var dto = testMapper.map(filing);

        assertThat(dto, is(equalTo(emptyDto)));
    }

    @Test
    void emptyOfficerFilingToAppointmentDto() {
        var filing = OfficerFiling.builder().formerNames(Collections.emptyList())
                .build();
        var emptyDto = OfficerFilingDto.builder().formerNames(Collections.emptyList())
                .build();

        final var dto = testMapper.map(filing);

        assertThat(dto, is(equalTo(emptyDto)));
        assertThat(dto.getAddressSameAsRegisteredOfficeAddress(), is(nullValue()));
        assertThat(dto.getResidentialAddressSameAsCorrespondenceAddress(), is(nullValue()));
    }

}