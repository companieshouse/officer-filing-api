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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.officerfiling.api.model.dto.AddressDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.IdentificationDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.Address;
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
    private LocalDate localDateDob1;
    private Instant dob1;
    private Instant instant1;
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
                .country("country")
                .locality("locality")
                .poBox("poBox")
                .postalCode("postalCode")
                .premises("premises")
                .region("region")
                .build();
        localDate1 = LocalDate.of(2019, 11, 5);
        localDateDob1 = LocalDate.of(1970, 9, 12);
        dob1 = Instant.parse("1970-09-12T00:00:00Z");
        instant1 = Instant.parse("2019-11-05T00:00:00Z");
        identification = new Identification("type", "auth", "legal", "place", "number");
        identificationDto = new IdentificationDto("type", "auth", "legal", "place", "number");
        links = new Links(URI.create(SELF_URI), URI.create(SELF_URI + "validation_status"));
    }

    @Test
    void appointmentDtoToOfficerFiling() {
        final var dto = OfficerFilingDto.builder()
                .identification(identificationDto)
                .serviceAddress(addressDto)
                .serviceAddressBackLink("backLink")
                .serviceManualAddressBackLink("serviceManualAddressBackLink")
                .protectedDetailsBackLink("protectedDetailsBackLink")
                .isServiceAddressSameAsRegisteredOfficeAddress(true)
                .appointedOn(localDate1)
                .countryOfResidence("countryOfResidence")
                .dateOfBirth(localDateDob1)
                .formerNames("Karim,Anton")
                .name("name")
                .referenceEtag("referenceEtag")
                .referenceAppointmentId("referenceAppointmentId")
                .nationality1("nation")
                .occupation("work")
                .referenceOfficerListEtag("list")
                .residentialAddress(addressDto)
                .residentialAddressBackLink("backLink")
                .residentialManualAddressBackLink("residentialManualAddressBackLink")
                .directorResidentialAddressChoice("different-address")
                .directorServiceAddressChoice("different-address")
                .isHomeAddressSameAsServiceAddress(true)
                .resignedOn(localDate1)
                .build();

        final var filing = testMapper.map(dto);

        assertThat(filing.getData().getServiceAddress(), is(equalTo(address)));
        assertThat(filing.getData().getServiceAddressBackLink(), is(equalTo("backLink")));
        assertThat(filing.getData().getServiceManualAddressBackLink(), is(equalTo("serviceManualAddressBackLink")));
        assertThat(filing.getData().getProtectedDetailsBackLink(), is(equalTo("protectedDetailsBackLink")));
        assertThat(filing.getData().getIsServiceAddressSameAsRegisteredOfficeAddress(), is(true));
        assertThat(filing.getData().getAppointedOn(),
                is(localDate1.atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertThat(filing.getData().getCountryOfResidence(), is("countryOfResidence"));
        assertThat(filing.getCreatedAt(), is(nullValue()));
        assertThat(filing.getData().getDateOfBirth(), is(dob1));
        assertThat(filing.getData().getFormerNames(), is("Karim,Anton"));
        assertThat(filing.getIdentification(), is(equalTo(identification)));
        assertThat(filing.getKind(), is(nullValue()));
        assertThat(filing.getLinks(), is(nullValue()));
        assertThat(filing.getData().getName(), is("name"));
        assertThat(filing.getData().getOfficerRole(), is(nullValue()));
        assertThat(filing.getData().getReferenceEtag(), is("referenceEtag"));
        assertThat(filing.getData().getReferenceAppointmentId(), is("referenceAppointmentId"));
        assertThat(filing.getData().getNationality1(), is("nation"));
        assertThat(filing.getData().getOccupation(), is("work"));
        assertThat(filing.getData().getReferenceEtag(), is("referenceEtag"));
        assertThat(filing.getData().getReferenceAppointmentId(), is("referenceAppointmentId"));
        assertThat(filing.getData().getReferenceOfficerListEtag(), is("list"));
        assertThat(filing.getData().getResignedOn(), is(localDate1.atStartOfDay().toInstant(ZoneOffset.UTC)));
        assertThat(filing.getData().getResidentialAddress(), is(equalTo(address)));
        assertThat(filing.getData().getResidentialAddressBackLink(), is(equalTo("backLink")));
        assertThat(filing.getData().getResidentialManualAddressBackLink(), is(equalTo("residentialManualAddressBackLink")));
        assertThat(filing.getData().getDirectorResidentialAddressChoice(), is(equalTo("different-address")));
        assertThat(filing.getData().getDirectorServiceAddressChoice(), is(equalTo("different-address")));
        assertThat(filing.getData().getIsHomeAddressSameAsServiceAddress(), is(true));
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
        final var dto = OfficerFilingDto.builder().build();
        var offData = OfficerFilingData.builder().build();
        final var now = clock.instant();
        final var emptyFiling = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();

        final var filing = testMapper.map(dto);

        assertThat(filing, is(equalTo(emptyFiling)));
        assertThat(filing.getData().getIsServiceAddressSameAsRegisteredOfficeAddress(), is(nullValue()));
        assertThat(filing.getData().getIsHomeAddressSameAsServiceAddress(), is(nullValue()));

    }

    @Test
    void officerFilingToOfficerFilingDto() {
        var offData = OfficerFilingData.builder()
                .serviceAddress(address)
                .serviceAddressBackLink("backLink")
                .serviceManualAddressBackLink("serviceManualAddressBackLink")
                .protectedDetailsBackLink("protectedDetailsBackLink")
                .isServiceAddressSameAsRegisteredOfficeAddress(true)
                .appointedOn(localDate1.atStartOfDay().toInstant(ZoneOffset.UTC))
                .countryOfResidence("countryOfResidence")
                .dateOfBirth(dob1)
                .formerNames("Karim,Anton")
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
                .residentialAddress(address)
                .residentialAddressBackLink("backLink")
                .residentialManualAddressBackLink("residentialManualAddressBackLink")
                .directorResidentialAddressChoice("different-address")
                .directorServiceAddressChoice("different-address")
                .isHomeAddressSameAsServiceAddress(true)
                .corporateDirector(false)
                .build();
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .identification(identification).build();

        final var dto = testMapper.map(filing);

        assertThat(dto.getServiceAddress(), is(equalTo(addressDto)));
        assertThat(dto.getServiceAddressBackLink(), is(equalTo("backLink")));
        assertThat(dto.getServiceManualAddressBackLink(), is(equalTo("serviceManualAddressBackLink")));
        assertThat(dto.getProtectedDetailsBackLink(), is(equalTo("protectedDetailsBackLink")));
        assertThat(dto.getIsServiceAddressSameAsRegisteredOfficeAddress(), is(true));
        assertThat(dto.getAppointedOn(), is(localDate1));
        assertThat(dto.getCountryOfResidence(), is("countryOfResidence"));
        assertThat(dto.getDateOfBirth(), is(localDateDob1));
        assertThat(dto.getFormerNames(), is("Karim,Anton"));
        assertThat(dto.getIdentification(), is(equalTo(identificationDto)));
        assertThat(dto.getName(), is("name"));
        assertThat(dto.getReferenceEtag(), is("referenceEtag"));
        assertThat(dto.getReferenceAppointmentId(), is("referenceAppointmentId"));
        assertThat(dto.getNationality1(), is("nation"));
        assertThat(dto.getOccupation(), is("work"));
        assertThat(dto.getReferenceOfficerListEtag(), is("list"));
        assertThat(dto.getResidentialAddress(), is(equalTo(addressDto)));
        assertThat(dto.getResidentialAddressBackLink(), is(equalTo("backLink")));
        assertThat(dto.getResidentialManualAddressBackLink(), is(equalTo("residentialManualAddressBackLink")));
        assertThat(dto.getDirectorResidentialAddressChoice(), is(equalTo("different-address")));
        assertThat(dto.getDirectorServiceAddressChoice(), is(equalTo("different-address")));
        assertThat(dto.getIsHomeAddressSameAsServiceAddress(), is(true));
        assertThat(dto.getResignedOn(), is(localDate1));
    }

    @Test
    void emptyOfficerFilingToAppointmentDto() {
        var offData = OfficerFilingData.builder().build();
        final var now = clock.instant();
        final var filing = OfficerFiling.builder().createdAt(now).updatedAt(now).data(offData)
                .build();
        final var emptyDto = OfficerFilingDto.builder()
                .build();

        final var dto = testMapper.map(filing);

        assertThat(dto, is(equalTo(emptyDto)));
        assertThat(dto.getIsServiceAddressSameAsRegisteredOfficeAddress(), is(nullValue()));
        assertThat(dto.getIsHomeAddressSameAsServiceAddress(), is(nullValue()));
    }

}