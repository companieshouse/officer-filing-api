package uk.gov.companieshouse.officerfiling.api.model.mapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.companieshouse.officerfiling.api.model.dto.AddressDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.AppointmentDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.FormerNameDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.IdentificationDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.Address;
import uk.gov.companieshouse.officerfiling.api.model.entity.FormerName;
import uk.gov.companieshouse.officerfiling.api.model.entity.Identification;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;

class AppointmentMapperTest {

    private Address address;
    private AddressDto addressDto;
    private LocalDate localDate1;
    private LocalDate localDate2;
    private Instant instant1;
    private List<FormerNameDto> nameDtoList;
    private List<FormerName> nameList;
    private IdentificationDto identificationDto;
    private Identification identification;
    private AppointmentMapper testMapper;

    @BeforeEach
    void setUp() {
        testMapper = Mappers.getMapper(AppointmentMapper.class);
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
        localDate2 = LocalDate.of(1970, 1, 1);
        instant1 = Instant.parse("2022-08-30T16:21:02Z");
        nameDtoList = List.of(new FormerNameDto("f1", "n1"), new FormerNameDto("f2", "n2"),
                new FormerNameDto("f3", "n3"));
        nameList = List.of(new FormerName("f1", "n1"), new FormerName("f2", "n2"),
                new FormerName("f3", "n3"));
        identification = new Identification("type", "auth", "legal", "place", "number");
        identificationDto = new IdentificationDto("type", "auth", "legal", "place", "number");
    }

    @Test
    void appointmentDtoToOfficerFiling() {
        var dto = AppointmentDto.builder()
                .address(addressDto)
                .addressSameAsRegisteredOfficeAddress(true)
                .appointedOn(localDate1)
                .countryOfResidence("countryOfResidence")
                .createdAt(instant1)
                .dateOfBirth(localDate2)
                .eTag("eTag")
                .formerNames(nameDtoList)
                .identification(identificationDto)
                .kind("kind")
                .name("name")
                .officerRole("role")
                .nationality("nation")
                .occupation("work")
                .referenceOfficerListETag("list")
                .residentialAddress(addressDto)
                .residentialAddressSameAsCorrespondenceAddress(true)
                .status("status")
                .updatedAt(instant1)
                .build();

        final var filing = testMapper.map(dto);

        assertThat(filing.getAddress(), is(equalTo(address)));
        assertThat(filing.isAddressSameAsRegisteredOfficeAddress(), is(true));
        assertThat(filing.getAppointedOn(),
                is(localDate1.atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertThat(filing.getCountryOfResidence(), is("countryOfResidence"));
        assertThat(filing.getCreatedAt(), is(instant1));
        assertThat(filing.getDateOfBirth(),
                is(localDate2.atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertThat(filing.geteTag(), is("eTag"));
        assertThat(filing.getFormerNames(), is(equalTo(nameList)));
        assertThat(filing.getIdentification(), is(equalTo(identification)));
        assertThat(filing.getKind(), is("kind"));
        assertThat(filing.getName(), is("name"));
        assertThat(filing.getOfficerRole(), is("role"));
        assertThat(filing.getNationality(), is("nation"));
        assertThat(filing.getOccupation(), is("work"));
        assertThat(filing.getReferenceETag(), is(nullValue()));
        assertThat(filing.getReferenceOfficerId(), is(nullValue()));
        assertThat(filing.getReferenceOfficerListETag(), is("list"));
        assertThat(filing.getResignedOn(), is(nullValue()));
        assertThat(filing.getResidentialAddress(), is(equalTo(address)));
        assertThat(filing.isResidentialAddressSameAsCorrespondenceAddress(), is(true));
        assertThat(filing.getStatus(), is("status"));
        assertThat(filing.getUpdatedAt(), is(instant1));
    }

    @Test
    void nullAppointmentDtoToOfficerFiling() {
        final var filing = testMapper.map((AppointmentDto) null);

        assertThat(filing, is(nullValue()));
    }

    @Test
    void emptyAppointmentNullFormerNameDtoToOfficerFiling() {
        final var dto = AppointmentDto.builder().formerNames(Collections.singletonList(null))
                .build();
        final var emptyFiling = OfficerFiling.builder().formerNames(Collections.singletonList(null))
                .build();

        final var filing = testMapper.map(dto);

        assertThat(filing, is(equalTo(emptyFiling)));

    }

    @Test
    void officerFilingToAppointmentDto() {
        var filing = OfficerFiling.builder()
                .address(address)
                .addressSameAsRegisteredOfficeAddress(true)
                .appointedOn(localDate1.atStartOfDay().toInstant(ZoneOffset.UTC))
                .countryOfResidence("countryOfResidence")
                .createdAt(instant1)
                .dateOfBirth(localDate2.atStartOfDay().toInstant(ZoneOffset.UTC))
                .eTag("eTag")
                .formerNames(nameList)
                .identification(identification)
                .kind("kind")
                .name("name")
                .officerRole("role")
                .nationality("nation")
                .occupation("work")
                .referenceOfficerListETag("list")
                .residentialAddress(address)
                .residentialAddressSameAsCorrespondenceAddress(true)
                .status("status")
                .updatedAt(instant1)
                .build();

        final var dto = testMapper.map(filing);

        assertThat(dto.getAddress(), is(equalTo(addressDto)));
        assertThat(dto.isAddressSameAsRegisteredOfficeAddress(), is(true));
        assertThat(dto.getAppointedOn(), is(localDate1));
        assertThat(dto.getCountryOfResidence(), is("countryOfResidence"));
        assertThat(dto.getCreatedAt(), is(instant1));
        assertThat(dto.getDateOfBirth(), is(localDate2));
        assertThat(dto.geteTag(), is("eTag"));
        assertThat(dto.getFormerNames(), is(equalTo(nameDtoList)));
        assertThat(dto.getIdentification(), is(equalTo(identificationDto)));
        assertThat(dto.getKind(), is("kind"));
        assertThat(dto.getName(), is("name"));
        assertThat(dto.getOfficerRole(), is("role"));
        assertThat(dto.getNationality(), is("nation"));
        assertThat(dto.getOccupation(), is("work"));
        assertThat(dto.getReferenceOfficerListETag(), is("list"));
        assertThat(dto.getResidentialAddress(), is(equalTo(addressDto)));
        assertThat(dto.isResidentialAddressSameAsCorrespondenceAddress(), is(true));
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
        var emptyDto = AppointmentDto.builder()
                .build();

        final var dto = testMapper.map(filing);

        assertThat(dto, is(equalTo(emptyDto)));
    }

    @Test
    void emptyOfficerFilingNullFormerNameToAppointmentDto() {
        var filing = OfficerFiling.builder().formerNames(Collections.singletonList(null))
                .build();
        var emptyDto = AppointmentDto.builder().formerNames(Collections.singletonList(null))
                .build();

        final var dto = testMapper.map(filing);

        assertThat(dto, is(equalTo(emptyDto)));
    }

    @Test
    void emptyOfficerFilingToAppointmentDto() {
        var filing = OfficerFiling.builder().formerNames(Collections.emptyList())
                .build();
        var emptyDto = AppointmentDto.builder().formerNames(Collections.emptyList())
                .build();

        final var dto = testMapper.map(filing);

        assertThat(dto, is(equalTo(emptyDto)));
    }

}