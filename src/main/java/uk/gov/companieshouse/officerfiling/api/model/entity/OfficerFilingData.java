package uk.gov.companieshouse.officerfiling.api.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfficerFilingData {

    private Address address;
    private Boolean addressSameAsRegisteredOfficeAddress;
    private Instant appointedOn;
    private String countryOfResidence;
    private Instant dateOfBirth;
    private String formerNames;
    private String name;
    private String title;
    private String firstName;
    private String middleNames;
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
            final String referenceEtag,
            final String referenceAppointmentId,
            final Instant resignedOn
    ) {
        this.referenceEtag = referenceEtag;
        this.referenceAppointmentId = referenceAppointmentId;
        this.resignedOn = resignedOn;
    }

    public OfficerFilingData(
            final String referenceEtag,
            final String referenceAppointmentId
    ) {
        this.referenceEtag = referenceEtag;
        this.referenceAppointmentId = referenceAppointmentId;
    }

    public OfficerFilingData() {

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

    public Instant getDateOfBirth() {
        return dateOfBirth;
    }

    public String getFormerNames() {
        return formerNames;
    }

    public String getName() {
        return name;
    }
    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleNames() {
        return middleNames;
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
                && Objects.equals(getTitle(), that.getTitle())
                && Objects.equals(getFirstName(), that.getFirstName())
                && Objects.equals(getMiddleNames(), that.getMiddleNames())
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
                getFormerNames(), getName(), getTitle(),
                getFirstName(), getMiddleNames(), getLastName(), getNationality(), getOccupation(), getOfficerRole(),
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
                .add("title='" + title + "'")
                .add("firstName='" + firstName + "'")
                .add("middleNames='" + middleNames + "'")
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

        public Builder() {
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
                    .title(other.getTitle())
                    .firstName(other.getFirstName())
                    .middleNames(other.getMiddleNames())
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

        public Builder dateOfBirth(final Instant value) {

            buildSteps.add(data -> data.dateOfBirth = value);

            return this;
        }

        public Builder formerNames(final String value) {

            buildSteps.add(data -> data.formerNames = value);
            return this;
        }

        public Builder name(final String value) {

            buildSteps.add(data -> data.name = value);
            return this;
        }

        public Builder title(final String value) {

            buildSteps.add(data -> data.title = value);
            return this;
        }

        public Builder firstName(final String value) {

            buildSteps.add(data -> data.firstName = value);
            return this;
        }

        public Builder middleNames(final String value) {

            buildSteps.add(data -> data.middleNames = value);
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

            final var data = new OfficerFilingData();
            buildSteps.forEach(step -> step.accept(data));

            return data;
        }
    }
}
