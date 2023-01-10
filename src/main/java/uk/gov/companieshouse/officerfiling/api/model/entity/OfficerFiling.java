package uk.gov.companieshouse.officerfiling.api.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "officer_filing")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfficerFiling {

    @Id
    private String id;
    private Address address;
    private Boolean addressSameAsRegisteredOfficeAddress;
    private Instant appointedOn;
    private String countryOfResidence;
    private Instant createdAt;
    private Date3Tuple dateOfBirth;
    private List<FormerName> formerNames;
    private Identification identification;
    private String kind;
    private Links links;
    private String name;
    private String firstName;
    private String lastName;
    private String nationality;
    private String occupation;
    private String officerRole;
    private String referenceEtag;
    private String referenceAppointmentId;
    private String referenceOfficerListEtag;
    private Instant resignedOn;
    private String status;
    private Instant updatedAt;
    private Address residentialAddress;
    private Boolean residentialAddressSameAsCorrespondenceAddress;

    private OfficerFiling() {
    }

    public String getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public Boolean getAddressSameAsRegisteredOfficeAddress() {
        return addressSameAsRegisteredOfficeAddress;
    }

    public Instant getAppointedOn() {
        return appointedOn;
    }

    public String getCountryOfResidence() {
        return countryOfResidence;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Date3Tuple getDateOfBirth() {
        return dateOfBirth;
    }

    public List<FormerName> getFormerNames() {
        return formerNames;
    }

    public Identification getIdentification() {
        return identification;
    }

    public String getKind() {
        return kind;
    }

    public Links getLinks() {
        return links;
    }

    public String getName() {
        return name;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNationality() {
        return nationality;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getOfficerRole() {
        return officerRole;
    }

    public String getReferenceEtag() {
        return referenceEtag;
    }

    public String getreferenceAppointmentId() {
        return referenceAppointmentId;
    }

    public String getReferenceOfficerListEtag() {
        return referenceOfficerListEtag;
    }

    public Instant getResignedOn() {
        return resignedOn;
    }

    public String getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Address getResidentialAddress() {
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
        final OfficerFiling that = (OfficerFiling) o;
        return Objects.equals(getId(), that.getId())
                && Objects.equals(getAddress(), that.getAddress())
                && Objects.equals(getAddressSameAsRegisteredOfficeAddress(),
                that.getAddressSameAsRegisteredOfficeAddress())
                && Objects.equals(getAppointedOn(), that.getAppointedOn())
                && Objects.equals(getCountryOfResidence(), that.getCountryOfResidence())
                && Objects.equals(getCreatedAt(), that.getCreatedAt())
                && Objects.equals(getDateOfBirth(), that.getDateOfBirth())
                && Objects.equals(getFormerNames(), that.getFormerNames())
                && Objects.equals(getIdentification(), that.getIdentification())
                && Objects.equals(getKind(), that.getKind())
                && Objects.equals(getLinks(), that.getLinks())
                && Objects.equals(getName(), that.getName())
                && Objects.equals(getFirstName(), that.getFirstName())
                && Objects.equals(getLastName(), that.getLastName())
                && Objects.equals(getNationality(), that.getNationality())
                && Objects.equals(getOccupation(), that.getOccupation())
                && Objects.equals(getOfficerRole(), that.getOfficerRole())
                && Objects.equals(getReferenceEtag(), that.getReferenceEtag())
                && Objects.equals(getreferenceAppointmentId(), that.getreferenceAppointmentId())
                && Objects.equals(getReferenceOfficerListEtag(), that.getReferenceOfficerListEtag())
                && Objects.equals(getResignedOn(), that.getResignedOn())
                && Objects.equals(getStatus(), that.getStatus())
                && Objects.equals(getUpdatedAt(), that.getUpdatedAt())
                && Objects.equals(getResidentialAddress(), that.getResidentialAddress())
                && Objects.equals(getResidentialAddressSameAsCorrespondenceAddress(),
                that.getResidentialAddressSameAsCorrespondenceAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAddress(), getAddressSameAsRegisteredOfficeAddress(),
                getAppointedOn(), getCountryOfResidence(), getCreatedAt(), getDateOfBirth(),
                getFormerNames(), getIdentification(), getKind(), getLinks(), getName(),
                getFirstName(), getLastName(), getNationality(), getOccupation(), getOfficerRole(),
                getReferenceEtag(), getreferenceAppointmentId(), getReferenceOfficerListEtag(),
                getResignedOn(), getStatus(), getUpdatedAt(), getResidentialAddress(),
                getResidentialAddressSameAsCorrespondenceAddress());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OfficerFiling.class.getSimpleName() + "[", "]").add(
                        "id='" + id + "'")
                .add("address=" + address)
                .add("addressSameAsRegisteredOfficeAddress=" + addressSameAsRegisteredOfficeAddress)
                .add("appointedOn=" + appointedOn)
                .add("countryOfResidence='" + countryOfResidence + "'")
                .add("createdAt=" + createdAt)
                .add("dateOfBirth=" + dateOfBirth)
                .add("formerNames=" + formerNames)
                .add("identification=" + identification)
                .add("kind='" + kind + "'")
                .add("links=" + links)
                .add("name='" + name + "'")
                .add("firstName='" + firstName + "'")
                .add("lastName='" + lastName + "'")
                .add("nationality='" + nationality + "'")
                .add("occupation='" + occupation + "'")
                .add("officerRole='" + officerRole + "'")
                .add("referenceEtag='" + referenceEtag + "'")
                .add("referenceAppointmentId='" + referenceAppointmentId + "'")
                .add("referenceOfficerListEtag='" + referenceOfficerListEtag + "'")
                .add("resignedOn=" + resignedOn)
                .add("status='" + status + "'")
                .add("updatedAt=" + updatedAt)
                .add("residentialAddress=" + residentialAddress)
                .add("residentialAddressSameAsCorrespondenceAddress="
                        + residentialAddressSameAsCorrespondenceAddress)
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(final OfficerFiling other) {
        return new Builder(other);
    }

    public static class Builder {

        private final List<Consumer<OfficerFiling>> buildSteps;

        private Builder() {
            buildSteps = new ArrayList<>();
        }

        public Builder(final OfficerFiling other) {
            this();
            this.id(other.getId())
                    .address(other.getAddress())
                    .addressSameAsRegisteredOfficeAddress(
                            other.getAddressSameAsRegisteredOfficeAddress())
                    .appointedOn(other.getAppointedOn())
                    .countryOfResidence(other.getCountryOfResidence())
                    .createdAt(other.getCreatedAt())
                    .dateOfBirth(other.getDateOfBirth())
                    .formerNames(other.getFormerNames())
                    .identification(other.getIdentification())
                    .kind(other.getKind())
                    .links(other.getLinks())
                    .name(other.getName())
                    .firstName(other.getFirstName())
                    .lastName(other.getLastName())
                    .nationality(other.getNationality())
                    .occupation(other.getOccupation())
                    .officerRole(other.getOfficerRole())
                    .referenceEtag(other.getReferenceEtag())
                    .referenceAppointmentId(other.getreferenceAppointmentId())
                    .referenceOfficerListEtag(other.getReferenceOfficerListEtag())
                    .residentialAddress(other.getResidentialAddress())
                    .residentialAddressSameAsCorrespondenceAddress(
                            other.getResidentialAddressSameAsCorrespondenceAddress())
                    .resignedOn(other.getResignedOn())
                    .status(other.getStatus())
                    .updatedAt(other.getUpdatedAt());
        }

        public Builder id(final String value) {
            buildSteps.add(data -> data.id = value);
            return this;
        }

        public Builder address(final Address value) {

            buildSteps.add(data -> data.address = Optional.ofNullable(value)
                    .map(v -> Address.builder(v)
                            .build())
                    .orElse(null));
            return this;
        }

        public Builder addressSameAsRegisteredOfficeAddress(final Boolean value) {

            buildSteps.add(data -> data.addressSameAsRegisteredOfficeAddress = value);
            return this;
        }

        public Builder appointedOn(final Instant value) {

            buildSteps.add(data -> data.appointedOn = value);
            return this;
        }

        public Builder countryOfResidence(final String value) {

            buildSteps.add(data -> data.countryOfResidence = value);
            return this;
        }

        public Builder createdAt(final Instant value) {

            buildSteps.add(data -> data.createdAt = value);
            return this;
        }

        public Builder dateOfBirth(final Date3Tuple value) {

            buildSteps.add(data -> data.dateOfBirth = Optional.ofNullable(value)
                    .map(v -> new Date3Tuple(v.getDay(), v.getMonth(), v.getYear()))
                    .orElse(null));
            return this;
        }

        public Builder formerNames(final List<FormerName> value) {
            buildSteps.add(data -> data.formerNames = value == null
                    ? null
                    : value.stream()
                            .flatMap(Stream::ofNullable)
                            .map(v -> new FormerName(v.getForenames(), v.getSurname()))
                            .collect(Collectors.toList()));
            return this;
        }

        public Builder identification(final Identification value) {

            buildSteps.add(data -> data.identification = Optional.ofNullable(value)
                    .map(v -> new Identification(v.getIdentificationType(), v.getLegalAuthority(),
                            v.getLegalForm(), v.getPlaceRegistered(), v.getRegistrationNumber()))
                    .orElse(null));
            return this;
        }

        public Builder kind(final String value) {

            buildSteps.add(data -> data.kind = value);
            return this;
        }

        public Builder links(final Links value) {

            buildSteps.add(data -> data.links = Optional.ofNullable(value)
                    .map(v -> new Links(v.getSelf(), v.getValidationStatus()))
                    .orElse(null));
            return this;
        }

        public Builder name(final String value) {

            buildSteps.add(data -> data.name = value);
            return this;
        }

        public Builder firstName(final String value) {

            buildSteps.add(data -> data.firstName = value);
            return this;
        }

        public Builder lastName(final String value) {

            buildSteps.add(data -> data.lastName = value);
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

        public Builder officerRole(final String value) {

            buildSteps.add(data -> data.officerRole = value);
            return this;
        }

        public Builder referenceEtag(final String value) {

            buildSteps.add(data -> data.referenceEtag = value);
            return this;
        }

        public Builder referenceAppointmentId(final String value) {

            buildSteps.add(data -> data.referenceAppointmentId = value);
            return this;
        }

        public Builder referenceOfficerListEtag(final String value) {

            buildSteps.add(data -> data.referenceOfficerListEtag = value);
            return this;
        }

        public Builder resignedOn(final Instant value) {

            buildSteps.add(data -> data.resignedOn = value);
            return this;
        }

        public Builder status(final String value) {

            buildSteps.add(data -> data.status = value);
            return this;
        }

        public Builder updatedAt(final Instant value) {

            buildSteps.add(data -> data.updatedAt = value);
            return this;
        }

        public Builder residentialAddress(final Address value) {

            buildSteps.add(data -> data.residentialAddress = Optional.ofNullable(value)
                    .map(v -> Address.builder(v)
                            .build())
                    .orElse(null));
            return this;
        }

        public Builder residentialAddressSameAsCorrespondenceAddress(final Boolean value) {

            buildSteps.add(data -> data.residentialAddressSameAsCorrespondenceAddress = value);
            return this;
        }

        public OfficerFiling build() {
            final var officerFiling = new OfficerFiling();
            buildSteps.forEach(s -> s.accept(officerFiling));

            return officerFiling;
        }
    }
}
