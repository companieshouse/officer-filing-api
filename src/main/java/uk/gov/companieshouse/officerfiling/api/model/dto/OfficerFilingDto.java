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
import org.springframework.validation.annotation.Validated;

@JsonDeserialize(builder = OfficerFilingDto.Builder.class)
@Validated
public class OfficerFilingDto {

    private AddressDto serviceAddress;
    private Boolean addressSameAsRegisteredOfficeAddress;
    private LocalDate appointedOn;
    private String countryOfResidence;
    private LocalDate dateOfBirth;
    private String formerNames;
    private IdentificationDto identification;
    private String name;
    private String title;
    private String firstName;
    private String middleNames;
    private String lastName;
    private String nationality1;
    private String nationality2;
    private String nationality3;
    private String occupation;
    private String referenceEtag;
    private String referenceAppointmentId;
    private String referenceOfficerListEtag;
    private LocalDate resignedOn;
    private AddressDto residentialAddress;
    private String residentialAddressBackLink;
    private Boolean residentialAddressSameAsCorrespondenceAddress;
    private Boolean nationality2Link;
    private Boolean nationality3Link;
    private Boolean directorAppliedToProtectDetails;
    private Boolean consentToAct;

    private OfficerFilingDto() {
    }

    public AddressDto getServiceAddress() {
        return serviceAddress;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getFormerNames() {
        return formerNames;
    }

    public IdentificationDto getIdentification() {
        return identification;
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
    public String getNationality2() {
        return nationality2;
    }
    public String getNationality3() {
        return nationality3;
    }
    public String getOccupation() {
        return occupation;
    }

    public String getReferenceEtag() {
        return referenceEtag;
    }

    public String getReferenceAppointmentId() {
        return referenceAppointmentId;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final var that = (OfficerFilingDto) o;
        return Objects.equals(getServiceAddress(), that.getServiceAddress())
                && Objects.equals(getAddressSameAsRegisteredOfficeAddress(),
                that.getAddressSameAsRegisteredOfficeAddress())
                && Objects.equals(getAppointedOn(), that.getAppointedOn())
                && Objects.equals(getCountryOfResidence(), that.getCountryOfResidence())
                && Objects.equals(getDateOfBirth(), that.getDateOfBirth())
                && Objects.equals(getFormerNames(), that.getFormerNames())
                && Objects.equals(getIdentification(), that.getIdentification())
                && Objects.equals(getTitle(), that.getTitle())
                && Objects.equals(getName(), that.getName())
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
                && Objects.equals(getReferenceEtag(), that.getReferenceEtag())
                && Objects.equals(getReferenceAppointmentId(), that.getReferenceAppointmentId())
                && Objects.equals(getReferenceOfficerListEtag(), that.getReferenceOfficerListEtag())
                && Objects.equals(getResignedOn(), that.getResignedOn())
                && Objects.equals(getResidentialAddress(), that.getResidentialAddress())
                && Objects.equals(getResidentialAddressBackLink(), that.getResidentialAddressBackLink())
                && Objects.equals(getResidentialAddressSameAsCorrespondenceAddress(),
                that.getResidentialAddressSameAsCorrespondenceAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServiceAddress(), getAddressSameAsRegisteredOfficeAddress(),
                getAppointedOn(), getCountryOfResidence(), getDateOfBirth(), getFormerNames(),
                getIdentification(), getTitle(), getName(), getFirstName(), getMiddleNames(), getLastName(),
                getNationality1(), getNationality2(), getNationality2Link(), getNationality3Link(),
                getDirectorAppliedToProtectDetails(), getConsentToAct(), getNationality3(),
                getOccupation(), getReferenceEtag(), getReferenceAppointmentId(), getReferenceOfficerListEtag(),
                getResignedOn(), getResidentialAddress(), getResidentialAddressSameAsCorrespondenceAddress());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OfficerFilingDto.class.getSimpleName() + "[", "]").add(
                        "serviceAddress=" + serviceAddress)
                .add("addressSameAsRegisteredOfficeAddress=" + addressSameAsRegisteredOfficeAddress)
                .add("appointedOn=" + appointedOn)
                .add("countryOfResidence='" + countryOfResidence + "'")
                .add("dateOfBirth=" + dateOfBirth)
                .add("formerNames=" + formerNames)
                .add("identification=" + identification)
                .add("title='" + title + "'")
                .add("name='" + name + "'")
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
                .add("referenceEtag='" + referenceEtag + "'")
                .add("referenceAppointmentId='" + referenceAppointmentId + "'")
                .add("referenceOfficerListEtag='" + referenceOfficerListEtag + "'")
                .add("resignedOn=" + resignedOn)
                .add("residentialAddress=" + residentialAddress)
                .add("residentialAddressBackLink='" + residentialAddressBackLink + "'")
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

        public Builder serviceAddress(final AddressDto value) {

            buildSteps.add(data -> data.serviceAddress = Optional.ofNullable(value)
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

        public Builder dateOfBirth(final LocalDate value) {

            buildSteps.add(data -> data.dateOfBirth = value);
            return this;
        }

        public Builder formerNames(final String value) {

            buildSteps.add(data -> data.formerNames = value);
            return this;
        }

        public Builder identification(final IdentificationDto value) {

            buildSteps.add(data -> data.identification = Optional.ofNullable(value)
                    .map(v -> new IdentificationDto(v.getIdentificationType(),
                            v.getLegalAuthority(), v.getLegalForm(), v.getPlaceRegistered(),
                            v.getRegistrationNumber()))
                    .orElse(null));
            return this;
        }

        public Builder title(final String value) {

            buildSteps.add(data -> data.title = value);
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

        public Builder residentialAddressBackLink(final String value) {

            buildSteps.add(data -> data.residentialAddressBackLink = value);
            return this;
        }

        public Builder residentialAddressSameAsCorrespondenceAddress(final Boolean value) {

            buildSteps.add(data -> data.residentialAddressSameAsCorrespondenceAddress = value);
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

        public  Builder directorAppliedToProtectDetails(final Boolean value) {
            buildSteps.add(data -> data.directorAppliedToProtectDetails = value);
            return this;
        }

        public Builder consentToAct(final Boolean value) {
            buildSteps.add(data -> data.consentToAct = value);
            return this;
        }

        public OfficerFilingDto build() {

            final var data = new OfficerFilingDto();
            buildSteps.forEach(step -> step.accept(data));

            return data;
        }
    }
}
