package uk.gov.companieshouse.officerfiling.api.model.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;

public class OfficerFilingDto {


    private AddressDto address;
    private Boolean addressSameAsRegisteredOfficeAddress;
    private LocalDate appointedOn;
    private String countryOfResidence;
    private Instant createdAt;
    private Date3TupleDto dateOfBirth;
    private List<FormerNameDto> formerNames;
    private IdentificationDto identification;
    private String kind;
    private LinksDto links;
    private String name;
    private String nationality;
    private String occupation;
    private String officerRole;
    private String referenceETag;
    private String referenceOfficerId;
    private String referenceOfficerListETag;
    private LocalDate resignedOn;
    private String status;
    private Instant updatedAt;
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

    public Instant getCreatedAt() {
        return createdAt;
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

    public String getKind() {
        return kind;
    }

    public LinksDto getLinks() {
        return links;
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

    public String getOfficerRole() {
        return officerRole;
    }

    public String getReferenceETag() {
        return referenceETag;
    }

    public String getReferenceOfficerId() {
        return referenceOfficerId;
    }

    public LocalDate getResignedOn() {
        return resignedOn;
    }

    public String getReferenceOfficerListETag() {
        return referenceOfficerListETag;
    }

    public String getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
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
        final OfficerFilingDto that = (OfficerFilingDto) o;
        return Objects.equals(getAddress(), that.getAddress()) &&
                Objects.equals(getAddressSameAsRegisteredOfficeAddress(),
                        that.getAddressSameAsRegisteredOfficeAddress()) &&
                Objects.equals(getAppointedOn(), that.getAppointedOn()) &&
                Objects.equals(getCountryOfResidence(), that.getCountryOfResidence()) &&
                Objects.equals(getCreatedAt(), that.getCreatedAt()) &&
                Objects.equals(getDateOfBirth(), that.getDateOfBirth()) &&
                Objects.equals(getFormerNames(), that.getFormerNames()) &&
                Objects.equals(getIdentification(), that.getIdentification()) &&
                Objects.equals(getKind(), that.getKind()) &&
                Objects.equals(getLinks(), that.getLinks()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getNationality(), that.getNationality()) &&
                Objects.equals(getOccupation(), that.getOccupation()) &&
                Objects.equals(getOfficerRole(), that.getOfficerRole()) &&
                Objects.equals(getReferenceETag(), that.getReferenceETag()) &&
                Objects.equals(getReferenceOfficerId(), that.getReferenceOfficerId()) &&
                Objects.equals(getReferenceOfficerListETag(), that.getReferenceOfficerListETag()) &&
                Objects.equals(getResignedOn(), that.getResignedOn()) &&
                Objects.equals(getStatus(), that.getStatus()) &&
                Objects.equals(getUpdatedAt(), that.getUpdatedAt()) &&
                Objects.equals(getResidentialAddress(), that.getResidentialAddress()) &&
                Objects.equals(getResidentialAddressSameAsCorrespondenceAddress(),
                        that.getResidentialAddressSameAsCorrespondenceAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getAddressSameAsRegisteredOfficeAddress(),
                getAppointedOn(), getCountryOfResidence(), getCreatedAt(), getDateOfBirth(),
                getFormerNames(), getIdentification(), getKind(), getLinks(), getName(),
                getNationality(), getOccupation(), getOfficerRole(), getReferenceETag(),
                getReferenceOfficerId(), getReferenceOfficerListETag(), getResignedOn(),
                getStatus(), getUpdatedAt(), getResidentialAddress(),
                getResidentialAddressSameAsCorrespondenceAddress());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OfficerFilingDto.class.getSimpleName() + "[", "]").add(
                        "address=" + address)
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
                .add("nationality='" + nationality + "'")
                .add("occupation='" + occupation + "'")
                .add("officerRole='" + officerRole + "'")
                .add("referenceETag='" + referenceETag + "'")
                .add("referenceOfficerId='" + referenceOfficerId + "'")
                .add("referenceOfficerListETag='" + referenceOfficerListETag + "'")
                .add("resignedOn=" + resignedOn)
                .add("status='" + status + "'")
                .add("updatedAt=" + updatedAt)
                .add("residentialAddress=" + residentialAddress)
                .add("residentialAddressSameAsCorrespondenceAddress=" +
                        residentialAddressSameAsCorrespondenceAddress)
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final List<Consumer<OfficerFilingDto>> buildSteps;

        public Builder() {
            this.buildSteps = new ArrayList<>();
        }

        public Builder address(AddressDto value) {

            buildSteps.add(data -> data.address = value);
            return this;
        }

        public Builder addressSameAsRegisteredOfficeAddress(Boolean value) {

            buildSteps.add(data -> data.addressSameAsRegisteredOfficeAddress = value);
            return this;
        }

        public Builder appointedOn(LocalDate value) {

            buildSteps.add(data -> data.appointedOn = value);
            return this;
        }

        public Builder countryOfResidence(String value) {

            buildSteps.add(data -> data.countryOfResidence = value);
            return this;
        }

        public Builder createdAt(Instant value) {

            buildSteps.add(data -> data.createdAt = value);
            return this;
        }

        public Builder dateOfBirth(Date3TupleDto value) {

            buildSteps.add(data -> data.dateOfBirth = value);
            return this;
        }

        public Builder formerNames(List<FormerNameDto> value) {

            buildSteps.add(data -> data.formerNames = value);
            return this;
        }

        public Builder identification(IdentificationDto value) {

            buildSteps.add(data -> data.identification = value);
            return this;
        }

        public Builder kind(String value) {

            buildSteps.add(data -> data.kind = value);
            return this;
        }

        public Builder links(LinksDto value) {

            buildSteps.add(data -> data.links = value);
            return this;
        }

        public Builder name(String value) {

            buildSteps.add(data -> data.name = value);
            return this;
        }

        public Builder nationality(String value) {

            buildSteps.add(data -> data.nationality = value);
            return this;
        }

        public Builder occupation(String value) {

            buildSteps.add(data -> data.occupation = value);
            return this;
        }

        public Builder officerRole(String value) {

            buildSteps.add(data -> data.officerRole = value);
            return this;
        }

        public Builder referenceETag(String value) {

            buildSteps.add(data -> data.referenceETag = value);
            return this;
        }

        public Builder referenceOfficerId(String value) {

            buildSteps.add(data -> data.referenceOfficerId = value);
            return this;
        }

        public Builder referenceOfficerListETag(String value) {

            buildSteps.add(data -> data.referenceOfficerListETag = value);
            return this;
        }

        public Builder resignedOn(LocalDate value) {

            buildSteps.add(data -> data.resignedOn = value);
            return this;
        }

        public Builder status(String value) {

            buildSteps.add(data -> data.status = value);
            return this;
        }

        public Builder updatedAt(Instant value) {

            buildSteps.add(data -> data.updatedAt = value);
            return this;
        }

        public Builder residentialAddress(AddressDto value) {

            buildSteps.add(data -> data.residentialAddress = value);
            return this;
        }

        public Builder residentialAddressSameAsCorrespondenceAddress(boolean value) {

            buildSteps.add(data -> data.residentialAddressSameAsCorrespondenceAddress = value);
            return this;
        }

        public OfficerFilingDto build() {

            OfficerFilingDto data = new OfficerFilingDto();
            buildSteps.forEach(step -> step.accept(data));

            return data;
        }
    }
}
