package uk.gov.companieshouse.officerfiling.api.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import org.springframework.validation.annotation.Validated;

@JsonDeserialize(builder = OfficerFilingDto.Builder.class)
@Validated
public class OfficerFilingDto {

    private AddressDto address;
    private Boolean addressSameAsRegisteredOfficeAddress;
    private LocalDate appointedOn;
    private String countryOfResidence;
    private Date3TupleDto dateOfBirth;
    private List<FormerNameDto> formerNames;
    private IdentificationDto identification;
    private String name;
    private String nationality;
    private String occupation;
    @NotBlank
    private String referenceEtag;
    @NotBlank
    private String referenceOfficerId;
    private String referenceOfficerListEtag;
    @PastOrPresent
    @NotNull
    private LocalDate resignedOn;
    private AddressDto residentialAddress;
    private Boolean residentialAddressSameAsCorrespondenceAddress;

    private OfficerFilingDto() {
    }

    public AddressDto getAddress() {
        return address;
    }

    public Boolean getAddressSameAsRegisteredOfficeAddress() {
        return addressSameAsRegisteredOfficeAddress;
    }

    public LocalDate getAppointedOn() {
        return appointedOn;
    }

    public String getCountryOfResidence() {
        return countryOfResidence;
    }

    public Date3TupleDto getDateOfBirth() {
        return dateOfBirth;
    }

    public List<FormerNameDto> getFormerNames() {
        return formerNames;
    }

    public IdentificationDto getIdentification() {
        return identification;
    }

    public String getName() {
        return name;
    }

    public String getNationality() {
        return nationality;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getReferenceEtag() {
        return referenceEtag;
    }

    public String getReferenceOfficerId() {
        return referenceOfficerId;
    }

    public LocalDate getResignedOn() {
        return resignedOn;
    }

    public String getReferenceOfficerListEtag() {
        return referenceOfficerListEtag;
    }

    public AddressDto getResidentialAddress() {
        return residentialAddress;
    }

    public Boolean getResidentialAddressSameAsCorrespondenceAddress() {
        return residentialAddressSameAsCorrespondenceAddress;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final var that = (OfficerFilingDto) o;
        return Objects.equals(getAddress(), that.getAddress())
                && Objects.equals(getAddressSameAsRegisteredOfficeAddress(),
                that.getAddressSameAsRegisteredOfficeAddress())
                && Objects.equals(getAppointedOn(), that.getAppointedOn())
                && Objects.equals(getCountryOfResidence(), that.getCountryOfResidence())
                && Objects.equals(getDateOfBirth(), that.getDateOfBirth())
                && Objects.equals(getFormerNames(), that.getFormerNames())
                && Objects.equals(getIdentification(), that.getIdentification())
                && Objects.equals(getName(), that.getName())
                && Objects.equals(getNationality(), that.getNationality())
                && Objects.equals(getOccupation(), that.getOccupation())
                && Objects.equals(getReferenceEtag(), that.getReferenceEtag())
                && Objects.equals(getReferenceOfficerId(), that.getReferenceOfficerId())
                && Objects.equals(getReferenceOfficerListEtag(), that.getReferenceOfficerListEtag())
                && Objects.equals(getResignedOn(), that.getResignedOn())
                && Objects.equals(getResidentialAddress(), that.getResidentialAddress())
                && Objects.equals(getResidentialAddressSameAsCorrespondenceAddress(),
                that.getResidentialAddressSameAsCorrespondenceAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getAddressSameAsRegisteredOfficeAddress(),
                getAppointedOn(), getCountryOfResidence(), getDateOfBirth(), getFormerNames(),
                getIdentification(), getName(), getNationality(), getOccupation(),
                getReferenceEtag(), getReferenceOfficerId(), getReferenceOfficerListEtag(),
                getResignedOn(), getResidentialAddress(),
                getResidentialAddressSameAsCorrespondenceAddress());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OfficerFilingDto.class.getSimpleName() + "[", "]").add(
                        "address=" + address)
                .add("addressSameAsRegisteredOfficeAddress=" + addressSameAsRegisteredOfficeAddress)
                .add("appointedOn=" + appointedOn)
                .add("countryOfResidence='" + countryOfResidence + "'")
                .add("dateOfBirth=" + dateOfBirth)
                .add("formerNames=" + formerNames)
                .add("identification=" + identification)
                .add("name='" + name + "'")
                .add("nationality='" + nationality + "'")
                .add("occupation='" + occupation + "'")
                .add("referenceEtag='" + referenceEtag + "'")
                .add("referenceOfficerId='" + referenceOfficerId + "'")
                .add("referenceOfficerListEtag='" + referenceOfficerListEtag + "'")
                .add("resignedOn=" + resignedOn)
                .add("residentialAddress=" + residentialAddress)
                .add("residentialAddressSameAsCorrespondenceAddress="
                        + residentialAddressSameAsCorrespondenceAddress)
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private final List<Consumer<OfficerFilingDto>> buildSteps;

        public Builder() {
            this.buildSteps = new ArrayList<>();
        }

        public Builder address(final AddressDto value) {

            buildSteps.add(data -> data.address = Optional.ofNullable(value)
                    .map(v -> AddressDto.builder(v)
                            .build())
                    .orElse(null));
            return this;
        }

        public Builder addressSameAsRegisteredOfficeAddress(final Boolean value) {

            buildSteps.add(data -> data.addressSameAsRegisteredOfficeAddress = value);
            return this;
        }

        public Builder appointedOn(final LocalDate value) {

            buildSteps.add(data -> data.appointedOn = value);
            return this;
        }

        public Builder countryOfResidence(final String value) {

            buildSteps.add(data -> data.countryOfResidence = value);
            return this;
        }

        public Builder dateOfBirth(final Date3TupleDto value) {

            buildSteps.add(data -> data.dateOfBirth = Optional.ofNullable(value)
                    .map(v -> new Date3TupleDto(v.getDay(), v.getMonth(), v.getYear()))
                    .orElse(null));
            return this;
        }

        public Builder formerNames(final List<FormerNameDto> value) {

            buildSteps.add(data -> data.formerNames = value == null
                    ? null
                    : value.stream()
                            .flatMap(Stream::ofNullable)
                            .map(v -> new FormerNameDto(v.getForenames(), v.getSurname()))
                            .collect(Collectors.toList()));
            return this;
        }

        public Builder identification(final IdentificationDto value) {

            buildSteps.add(data -> data.identification = Optional.ofNullable(value)
                    .map(v -> new IdentificationDto(v.getIdentificationType(), v.getLegalAuthority(),
                            v.getLegalForm(), v.getPlaceRegistered(), v.getRegistrationNumber()))
                    .orElse(null));
            return this;
        }

        public Builder name(final String value) {

            buildSteps.add(data -> data.name = value);
            return this;
        }

        public Builder nationality(final String value) {

            buildSteps.add(data -> data.nationality = value);
            return this;
        }

        public Builder occupation(final String value) {

            buildSteps.add(data -> data.occupation = value);
            return this;
        }

        public Builder referenceEtag(final String value) {

            buildSteps.add(data -> data.referenceEtag = value);
            return this;
        }

        public Builder referenceOfficerId(final String value) {

            buildSteps.add(data -> data.referenceOfficerId = value);
            return this;
        }

        public Builder referenceOfficerListEtag(final String value) {

            buildSteps.add(data -> data.referenceOfficerListEtag = value);
            return this;
        }

        public Builder resignedOn(final LocalDate value) {

            buildSteps.add(data -> data.resignedOn = value);
            return this;
        }

        public Builder residentialAddress(final AddressDto value) {

            buildSteps.add(data -> data.residentialAddress = Optional.ofNullable(value)
                    .map(v -> AddressDto.builder(v)
                            .build())
                    .orElse(null));
            return this;
        }

        public Builder residentialAddressSameAsCorrespondenceAddress(final Boolean value) {

            buildSteps.add(data -> data.residentialAddressSameAsCorrespondenceAddress = value);
            return this;
        }

        public OfficerFilingDto build() {

            final var data = new OfficerFilingDto();
            buildSteps.forEach(step -> step.accept(data));

            return data;
        }
    }
}
