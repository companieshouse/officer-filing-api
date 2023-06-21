package uk.gov.companieshouse.officerfiling.api.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.net.URI;
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

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfficerFilingData {

    private Address address;
    private Boolean addressSameAsRegisteredOfficeAddress;
    private Instant appointedOn;
    private String countryOfResidence;
    private Date3Tuple dateOfBirth;
    private List<FormerName> formerNames;
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
    private Address residentialAddress;
    private Boolean residentialAddressSameAsCorrespondenceAddress;
    private Boolean corporateDirector;

    public OfficerFilingData(
            final Address address,
            final Boolean addressSameAsRegisteredOfficeAddress,
            final Instant appointedOn,
            final String countryOfResidence,
            final Date3Tuple dateOfBirth,
            final List<FormerName> formerNames,
            final String name,
            final String firstName,
            final String lastName,
            final String nationality,
            final String occupation,
            final String officerRole,
            final String referenceEtag,
            final String referenceAppointmentId,
            final String referenceOfficerListEtag,
            final Instant resignedOn,
            final String status,
            final Address residentialAddress,
            final Boolean residentialAddressSameAsCorrespondenceAddress,
            final Boolean corporateDirector
    ) {
        this.address = address;
        this.addressSameAsRegisteredOfficeAddress = addressSameAsRegisteredOfficeAddress;
        this.appointedOn = appointedOn;
        this.countryOfResidence = countryOfResidence;
        this.dateOfBirth = dateOfBirth;
        this.formerNames = formerNames;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
        this.occupation = occupation;
        this.officerRole = officerRole;
        this.referenceEtag = referenceEtag;
        this.referenceAppointmentId = referenceAppointmentId;
        this.referenceOfficerListEtag = referenceOfficerListEtag;
        this.resignedOn = resignedOn;
        this.status = status;
        this.residentialAddress = residentialAddress;
        this.residentialAddressSameAsCorrespondenceAddress = residentialAddressSameAsCorrespondenceAddress;
        this.corporateDirector = corporateDirector;

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

    public Date3Tuple getDateOfBirth() {
        return dateOfBirth;
    }

    public List<FormerName> getFormerNames() {
        return formerNames;
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

    public String getReferenceAppointmentId() {
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

    public Address getResidentialAddress() {
        return residentialAddress;
    }

    public Boolean getResidentialAddressSameAsCorrespondenceAddress() {
        return residentialAddressSameAsCorrespondenceAddress;
    }

    public Boolean getCorporateDirector() {
        return corporateDirector;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OfficerFilingData that = (OfficerFilingData) o;
        return Objects.equals(getAddress(), that.getAddress())
                && Objects.equals(getAddressSameAsRegisteredOfficeAddress(),
                that.getAddressSameAsRegisteredOfficeAddress())
                && Objects.equals(getAppointedOn(), that.getAppointedOn())
                && Objects.equals(getCountryOfResidence(), that.getCountryOfResidence())
                && Objects.equals(getDateOfBirth(), that.getDateOfBirth())
                && Objects.equals(getFormerNames(), that.getFormerNames())
                && Objects.equals(getName(), that.getName())
                && Objects.equals(getFirstName(), that.getFirstName())
                && Objects.equals(getLastName(), that.getLastName())
                && Objects.equals(getNationality(), that.getNationality())
                && Objects.equals(getOccupation(), that.getOccupation())
                && Objects.equals(getOfficerRole(), that.getOfficerRole())
                && Objects.equals(getReferenceEtag(), that.getReferenceEtag())
                && Objects.equals(getReferenceAppointmentId(), that.getReferenceAppointmentId())
                && Objects.equals(getReferenceOfficerListEtag(), that.getReferenceOfficerListEtag())
                && Objects.equals(getResignedOn(), that.getResignedOn())
                && Objects.equals(getStatus(), that.getStatus())
                && Objects.equals(getResidentialAddress(), that.getResidentialAddress())
                && Objects.equals(getResidentialAddressSameAsCorrespondenceAddress(),
                that.getResidentialAddressSameAsCorrespondenceAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getAddressSameAsRegisteredOfficeAddress(),
                getAppointedOn(), getCountryOfResidence(), getDateOfBirth(),
                getFormerNames(), getName(),
                getFirstName(), getLastName(), getNationality(), getOccupation(), getOfficerRole(),
                getReferenceEtag(), getReferenceAppointmentId(), getReferenceOfficerListEtag(),
                getResignedOn(), getStatus(), getResidentialAddress(),
                getResidentialAddressSameAsCorrespondenceAddress());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OfficerFilingData.class.getSimpleName() + "[", "]")
                .add("address=" + address)
                .add("addressSameAsRegisteredOfficeAddress=" + addressSameAsRegisteredOfficeAddress)
                .add("appointedOn=" + appointedOn)
                .add("countryOfResidence='" + countryOfResidence + "'")
                .add("dateOfBirth=" + dateOfBirth)
                .add("formerNames=" + formerNames)
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
                .add("residentialAddress=" + residentialAddress)
                .add("residentialAddressSameAsCorrespondenceAddress="
                        + residentialAddressSameAsCorrespondenceAddress)
                .add("corporateDirector=" + corporateDirector)
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(final OfficerFilingData other) {
        return new Builder(other);
    }

    public static class Builder {

        private final List<Consumer<OfficerFilingData>> buildSteps;

        private Builder() {
            buildSteps = new ArrayList<>();
        }

        public Builder(final OfficerFilingData other) {
            this();
            this.address(other.getAddress())
                    .addressSameAsRegisteredOfficeAddress(
                            other.getAddressSameAsRegisteredOfficeAddress())
                    .appointedOn(other.getAppointedOn())
                    .corporateDirector(other.getCorporateDirector())
                    .countryOfResidence(other.getCountryOfResidence())
                    .dateOfBirth(other.getDateOfBirth())
                    .formerNames(other.getFormerNames())
                    .name(other.getName())
                    .firstName(other.getFirstName())
                    .lastName(other.getLastName())
                    .nationality(other.getNationality())
                    .occupation(other.getOccupation())
                    .officerRole(other.getOfficerRole())
                    .referenceEtag(other.getReferenceEtag())
                    .referenceAppointmentId(other.getReferenceAppointmentId())
                    .referenceOfficerListEtag(other.getReferenceOfficerListEtag())
                    .residentialAddress(other.getResidentialAddress())
                    .residentialAddressSameAsCorrespondenceAddress(
                            other.getResidentialAddressSameAsCorrespondenceAddress())
                    .resignedOn(other.getResignedOn())
                    .status(other.getStatus());
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

        public Builder corporateDirector(final Boolean value) {
            buildSteps.add(data -> data.corporateDirector = value);
            return this;
        }

        public OfficerFilingData build() {
            final var officerFilingData = new OfficerFilingData();
            buildSteps.forEach(s -> s.accept(officerFilingData));

            return officerFilingData;
        }
    }
}
