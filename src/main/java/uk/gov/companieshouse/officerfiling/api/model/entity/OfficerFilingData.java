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

    private Address serviceAddress;
    private String serviceAddressBackLink;
    private String protectedDetailsBackLink;
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
    private String nationality1;
    private String nationality2;
    private String nationality3;
    private String occupation;
    private String officerRole;
    private String referenceEtag;
    private String referenceAppointmentId;
    private String referenceOfficerListEtag;
    private Instant resignedOn;
    private String status;
    private Address residentialAddress;
    private String residentialAddressBackLink;
    private Boolean residentialAddressSameAsCorrespondenceAddress;
    private Boolean corporateDirector;
    private Boolean nationality2Link;
    private Boolean nationality3Link;
    private Boolean directorAppliedToProtectDetails;
    private Boolean consentToAct;
    private String checkYourAnswersLink;

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
    public Address getServiceAddress() {
        return serviceAddress;
    }

    public String getServiceAddressBackLink() {
        return serviceAddressBackLink;
    }

    public String getProtectedDetailsBackLink() {
        return protectedDetailsBackLink;
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

    public String getNationality1() {
        return nationality1;
    }
    public String getNationality2() { return nationality2; }

    public String getNationality3() { return nationality3; }

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

    public String getResidentialAddressBackLink() {
        return residentialAddressBackLink;
    }

    public Boolean getResidentialAddressSameAsCorrespondenceAddress() {
        return residentialAddressSameAsCorrespondenceAddress;
    }

    public Boolean getNationality2Link(){
        return nationality2Link;
    }

    public Boolean getNationality3Link(){
        return nationality3Link;
    }

    public Boolean getDirectorAppliedToProtectDetails() { return directorAppliedToProtectDetails; }

    public Boolean getConsentToAct() { return consentToAct; }

    public Boolean getCorporateDirector() {
        return corporateDirector;
    }

    public String getCheckYourAnswersLink() {
        return checkYourAnswersLink;
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
        return Objects.equals(getServiceAddress(), that.getServiceAddress())
                && Objects.equals(getServiceAddressBackLink(), that.getServiceAddressBackLink())
                && Objects.equals(getProtectedDetailsBackLink(), that.getProtectedDetailsBackLink())
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
                && Objects.equals(getNationality1(), that.getNationality1())
                && Objects.equals(getNationality2(), that.getNationality2())
                && Objects.equals(getNationality3(), that.getNationality3())
                && Objects.equals(getNationality2Link(), that.getNationality2Link())
                && Objects.equals(getNationality3Link(), that.getNationality3Link())
                && Objects.equals(getDirectorAppliedToProtectDetails(), that.getDirectorAppliedToProtectDetails())
                && Objects.equals(getConsentToAct(), that.getConsentToAct())
                && Objects.equals(getOccupation(), that.getOccupation())
                && Objects.equals(getOfficerRole(), that.getOfficerRole())
                && Objects.equals(getReferenceEtag(), that.getReferenceEtag())
                && Objects.equals(getReferenceAppointmentId(), that.getReferenceAppointmentId())
                && Objects.equals(getReferenceOfficerListEtag(), that.getReferenceOfficerListEtag())
                && Objects.equals(getResignedOn(), that.getResignedOn())
                && Objects.equals(getStatus(), that.getStatus())
                && Objects.equals(getResidentialAddress(), that.getResidentialAddress())
                && Objects.equals(getResidentialAddressBackLink(), that.getResidentialAddressBackLink())
                && Objects.equals(getResidentialAddressSameAsCorrespondenceAddress(),
                that.getResidentialAddressSameAsCorrespondenceAddress())
                && Objects.equals(getCheckYourAnswersLink(), that.getCheckYourAnswersLink());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServiceAddress(), getServiceAddressBackLink(), getProtectedDetailsBackLink(),
                getAddressSameAsRegisteredOfficeAddress(), getAppointedOn(), getCountryOfResidence(), getDateOfBirth(),
                getFormerNames(), getName(), getTitle(),
                getFirstName(), getMiddleNames(), getLastName(), getNationality1(), getNationality2(),
                getNationality3(), getNationality2Link(), getNationality3Link(), getDirectorAppliedToProtectDetails(),
                getConsentToAct(), getOccupation(), getOfficerRole(),
                getReferenceEtag(), getReferenceAppointmentId(), getReferenceOfficerListEtag(),
                getResignedOn(), getStatus(), getResidentialAddress(), getResidentialAddressBackLink(),
                getResidentialAddressSameAsCorrespondenceAddress(), getCheckYourAnswersLink());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OfficerFilingData.class.getSimpleName() + "[", "]")
                .add("serviceAddress=" + serviceAddress)
                .add("serviceAddressBackLink=" + serviceAddressBackLink)
                .add("protectedDetailsBackLink=" + protectedDetailsBackLink)
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
                .add("nationality1='" + nationality1 + "'")
                .add("nationality2='" + nationality2 + "'")
                .add("nationality3='" + nationality3 + "'")
                .add("nationality2Link='" + nationality2Link + "'")
                .add("nationality3Link='" + nationality3Link + "'")
                .add("directorAppliedToProtectDetails='" + directorAppliedToProtectDetails + "'")
                .add("consentToAct='" + consentToAct + "'")
                .add("occupation='" + occupation + "'")
                .add("officerRole='" + officerRole + "'")
                .add("referenceEtag='" + referenceEtag + "'")
                .add("referenceAppointmentId='" + referenceAppointmentId + "'")
                .add("referenceOfficerListEtag='" + referenceOfficerListEtag + "'")
                .add("resignedOn=" + resignedOn)
                .add("status='" + status + "'")
                .add("residentialAddress=" + residentialAddress)
                .add("residentialAddressBackLink=" + residentialAddressBackLink)
                .add("residentialAddressSameAsCorrespondenceAddress="
                        + residentialAddressSameAsCorrespondenceAddress)
                .add("corporateDirector=" + corporateDirector)
                .add("checkYourAnswersLink=" + checkYourAnswersLink)
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
            this.serviceAddress(other.getServiceAddress())
                    .serviceAddressBackLink(other.getServiceAddressBackLink())
                    .protectedDetailsBackLink(other.getProtectedDetailsBackLink())
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
                    .nationality1(other.getNationality1())
                    .nationality2(other.getNationality2())
                    .nationality3(other.getNationality3())
                    .nationality2Link(other.getNationality2Link())
                    .nationality3Link(other.getNationality3Link())
                    .directorAppliedToProtectDetails(other.getDirectorAppliedToProtectDetails())
                    .consentToAct(other.getConsentToAct())
                    .occupation(other.getOccupation())
                    .officerRole(other.getOfficerRole())
                    .referenceEtag(other.getReferenceEtag())
                    .referenceAppointmentId(other.getReferenceAppointmentId())
                    .referenceOfficerListEtag(other.getReferenceOfficerListEtag())
                    .residentialAddress(other.getResidentialAddress())
                    .residentialAddressBackLink(other.getResidentialAddressBackLink())
                    .residentialAddressSameAsCorrespondenceAddress(
                            other.getResidentialAddressSameAsCorrespondenceAddress())
                    .resignedOn(other.getResignedOn())
                    .status(other.getStatus())
                    .checkYourAnswersLink(other.getCheckYourAnswersLink());
        }

        public Builder serviceAddress(final Address value) {

            buildSteps.add(data -> data.serviceAddress = Optional.ofNullable(value)
                    .map(v -> Address.builder(v)
                            .build())
                    .orElse(null));
            return this;
        }

        public Builder serviceAddressBackLink(final String value) {

            buildSteps.add(data -> data.serviceAddressBackLink = value);
            return this;
        }

        public Builder protectedDetailsBackLink(final String value) {

            buildSteps.add(data -> data.protectedDetailsBackLink = value);
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

        public Builder nationality1(final String value) {

            buildSteps.add(data -> data.nationality1 = value);
            return this;
        }
        public Builder nationality2(final String value) {

            buildSteps.add(data -> data.nationality2 = value);
            return this;
        }
        public Builder nationality3(final String value) {

            buildSteps.add(data -> data.nationality3 = value);
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

        public Builder residentialAddressBackLink(final String value) {

            buildSteps.add(data -> data.residentialAddressBackLink = value);
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

        public Builder nationality2Link(final Boolean value) {
            buildSteps.add(data -> data.nationality2Link = value);
            return this;
        }

        public Builder nationality3Link(final Boolean value) {
            buildSteps.add(data -> data.nationality3Link = value);
            return this;
        }

        public Builder directorAppliedToProtectDetails(final Boolean value) {
            buildSteps.add(data -> data.directorAppliedToProtectDetails = value);
            return this;
        }

        public Builder consentToAct(final Boolean value) {
            buildSteps.add(data -> data.consentToAct = value);
            return this;
        }

        public Builder checkYourAnswersLink(final String value) {
            buildSteps.add(data -> data.checkYourAnswersLink = value);
            return this;
        }

        public OfficerFilingData build() {

            final var data = new OfficerFilingData();
            buildSteps.forEach(step -> step.accept(data));

            return data;
        }
    }
}
