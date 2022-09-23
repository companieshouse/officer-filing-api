package uk.gov.companieshouse.officerfiling.api.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;
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
    private String nationality;
    private String occupation;
    private String officerRole;
    private String referenceEtag;
    private String referenceOfficerId;
    private String referenceOfficerListEtag;
    private Instant resignedOn;
    private String status;
    private Instant updatedAt;
    private Address residentialAddress;
    private Boolean residentialAddressSameAsCorrespondenceAddress;

    private OfficerFiling() {}

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

    public String getReferenceOfficerId() {
        return referenceOfficerId;
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
                Objects.equals(getReferenceEtag(), that.getReferenceEtag()) &&
                Objects.equals(getReferenceOfficerId(), that.getReferenceOfficerId()) &&
                Objects.equals(getReferenceOfficerListEtag(), that.getReferenceOfficerListEtag()) &&
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
                getNationality(), getOccupation(), getOfficerRole(), getReferenceEtag(),
                getReferenceOfficerId(), getReferenceOfficerListEtag(), getResignedOn(),
                getStatus(), getUpdatedAt(), getResidentialAddress(),
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
                .add("nationality='" + nationality + "'")
                .add("occupation='" + occupation + "'")
                .add("officerRole='" + officerRole + "'")
                .add("referenceETag='" + referenceEtag + "'")
                .add("referenceOfficerId='" + referenceOfficerId + "'")
                .add("referenceOfficerListEtag='" + referenceOfficerListEtag + "'")
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

        private final List<Consumer<OfficerFiling>> buildSteps;

        private Builder() {
            buildSteps = new ArrayList<>();
        }

        public Builder address(Address value) {

            buildSteps.add(data -> data.address = value);
            return this;
        }

        public Builder addressSameAsRegisteredOfficeAddress(Boolean value) {

            buildSteps.add(data -> data.addressSameAsRegisteredOfficeAddress = value);
            return this;
        }

        public Builder appointedOn(Instant value) {

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

        public Builder dateOfBirth(Date3Tuple value) {

            buildSteps.add(data -> data.dateOfBirth = value);
            return this;
        }

        public Builder formerNames(List<FormerName> value) {

            buildSteps.add(data -> data.formerNames = value);
            return this;
        }

        public Builder identification(Identification value) {

            buildSteps.add(data -> data.identification = value);
            return this;
        }

        public Builder kind(String value) {

            buildSteps.add(data -> data.kind = value);
            return this;
        }

        public Builder links(Links value) {

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

        public Builder referenceEtag(String value) {

            buildSteps.add(data -> data.referenceEtag = value);
            return this;
        }

        public Builder referenceOfficerId(String value) {

            buildSteps.add(data -> data.referenceOfficerId = value);
            return this;
        }

        public Builder referenceOfficerListEtag(String value) {

            buildSteps.add(data -> data.referenceOfficerListEtag = value);
            return this;
        }

        public Builder resignedOn(Instant value) {

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

        public Builder residentialAddress(Address value) {

            buildSteps.add(data -> data.residentialAddress = value);
            return this;
        }

        public Builder residentialAddressSameAsCorrespondenceAddress(Boolean value) {

            buildSteps.add(data -> data.residentialAddressSameAsCorrespondenceAddress = value);
            return this;
        }

        public OfficerFiling build() {
            var officerFiling = new OfficerFiling();
            buildSteps.forEach(s -> s.accept(officerFiling));

            return officerFiling;
        }
    }
}
