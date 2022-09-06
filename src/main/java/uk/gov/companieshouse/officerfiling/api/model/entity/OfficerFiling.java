package uk.gov.companieshouse.officerfiling.api.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "officer-filing")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfficerFiling {

    @Id
    private String id;
    private Address address;
    private boolean addressSameAsRegisteredOfficeAddress;
    private Instant appointedOn;
    private String countryOfResidence;
    private Instant createdAt;
    private Instant dateOfBirth;
    private String eTag;
    private List<FormerName> formerNames;
    private Identification identification;
    private String kind;
    private String name;
    private String nationality;
    private String occupation;
    private String officerRole;
    private String referenceETag;
    private String referenceOfficerId;
    private String referenceOfficerListETag;
    private Instant resignedOn;
    private String status;
    private Instant updatedAt;
    private Address residentialAddress;
    private boolean residentialAddressSameAsCorrespondenceAddress;

    private OfficerFiling() {}

    public String getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public boolean isAddressSameAsRegisteredOfficeAddress() {
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

    public Instant getDateOfBirth() {
        return dateOfBirth;
    }

    public String getETag() {
        return eTag;
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

    public String getReferenceOfficerListETag() {
        return referenceOfficerListETag;
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

    public boolean isResidentialAddressSameAsCorrespondenceAddress() {
        return residentialAddressSameAsCorrespondenceAddress;
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

        public Builder addressSameAsRegisteredOfficeAddress(boolean value) {

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

        public Builder dateOfBirth(Instant value) {

            buildSteps.add(data -> data.dateOfBirth = value);
            return this;
        }

        public Builder eTag(String value) {

            buildSteps.add(data -> data.eTag = value);
            return this;
        }

        public Builder formerNames(List<FormerName> value) {

            buildSteps.add(data -> data.formerNames = value);
            return this;
        }

        public Builder identification(Identification value) {

            buildSteps.add(data -> data.identification = new Identification(value));
            return this;
        }

        public Builder kind(String value) {

            buildSteps.add(data -> data.kind = value);
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

        public Builder residentialAddressSameAsCorrespondenceAddress(boolean value) {

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
