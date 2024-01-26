package uk.gov.companieshouse.officerfiling.api.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.springframework.validation.annotation.Validated;
import uk.gov.companieshouse.JsonBooleanDeserializer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@JsonDeserialize(builder = OfficerFilingDto.Builder.class)
@Validated
public class OfficerFilingDto {

    private AddressDto serviceAddress;
    private String serviceAddressBackLink;
    private String serviceManualAddressBackLink;
    private String protectedDetailsBackLink;
    private Boolean isServiceAddressSameAsRegisteredOfficeAddress;
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
    private String residentialManualAddressBackLink;
    private Boolean isHomeAddressSameAsServiceAddress;
    private Boolean nationality2Link;
    private Boolean nationality3Link;
    private Boolean directorAppliedToProtectDetails;
    private Boolean consentToAct;
    private String checkYourAnswersLink;
    private String directorResidentialAddressChoice;
    private String directorServiceAddressChoice;
    private Boolean nameHasBeenUpdated;
    private Boolean nationalityHasBeenUpdated;
    private Boolean occupationHasBeenUpdated;
    private Boolean correspondenceAddressHasBeenUpdated;
    private Boolean residentialAddressHasBeenUpdated;
    private LocalDate directorsDetailsChangedDate;
    private String description;

    private OfficerFilingDto() {
    }

    public AddressDto getServiceAddress() {
        return serviceAddress;
    }

    public String getServiceAddressBackLink() {
        return serviceAddressBackLink;
    }

    public String getServiceManualAddressBackLink() {
        return serviceManualAddressBackLink;
    }

    public String getProtectedDetailsBackLink() {
        return protectedDetailsBackLink;
    }

    public Boolean getIsServiceAddressSameAsRegisteredOfficeAddress() {
        return isServiceAddressSameAsRegisteredOfficeAddress;
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

    public String getResidentialManualAddressBackLink() {
        return residentialManualAddressBackLink;
    }

    public Boolean getIsHomeAddressSameAsServiceAddress() {
        return isHomeAddressSameAsServiceAddress;
    }

    public Boolean getNationality2Link() {
        return nationality2Link;
    }

    public Boolean getNationality3Link() {
        return nationality3Link;
    }

    public Boolean getDirectorAppliedToProtectDetails() {
        return directorAppliedToProtectDetails;
    }

    public Boolean getConsentToAct() {
        return consentToAct;
    }

    public String getCheckYourAnswersLink() {
        return checkYourAnswersLink;
    }

    public String getDirectorResidentialAddressChoice() {
        return directorResidentialAddressChoice;
    }

    public String getDirectorServiceAddressChoice() {
        return directorServiceAddressChoice;
    }

    public Boolean getNameHasBeenUpdated() {
        return nameHasBeenUpdated;
    }

    public Boolean getNationalityHasBeenUpdated() {
        return nationalityHasBeenUpdated;
    }

    public Boolean getOccupationHasBeenUpdated() {
        return occupationHasBeenUpdated;
    }

    public Boolean getCorrespondenceAddressHasBeenUpdated() {
        return correspondenceAddressHasBeenUpdated;
    }

    public Boolean getResidentialAddressHasBeenUpdated() {
        return residentialAddressHasBeenUpdated;
    }

    public LocalDate getDirectorsDetailsChangedDate() {
        return directorsDetailsChangedDate;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfficerFilingDto)) return false;
        OfficerFilingDto that = (OfficerFilingDto) o;
        return Objects.equals(getServiceAddress(), that.getServiceAddress()) && Objects.equals(getServiceAddressBackLink(), that.getServiceAddressBackLink()) && Objects.equals(getServiceManualAddressBackLink(), that.getServiceManualAddressBackLink()) && Objects.equals(getProtectedDetailsBackLink(), that.getProtectedDetailsBackLink()) && Objects.equals(getIsServiceAddressSameAsRegisteredOfficeAddress(), that.getIsServiceAddressSameAsRegisteredOfficeAddress()) && Objects.equals(getAppointedOn(), that.getAppointedOn()) && Objects.equals(getCountryOfResidence(), that.getCountryOfResidence()) && Objects.equals(getDateOfBirth(), that.getDateOfBirth()) && Objects.equals(getFormerNames(), that.getFormerNames()) && Objects.equals(getIdentification(), that.getIdentification()) && Objects.equals(getName(), that.getName()) && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getFirstName(), that.getFirstName()) && Objects.equals(getMiddleNames(), that.getMiddleNames()) && Objects.equals(getLastName(), that.getLastName()) && Objects.equals(getNationality1(), that.getNationality1()) && Objects.equals(getNationality2(), that.getNationality2()) && Objects.equals(getNationality3(), that.getNationality3()) && Objects.equals(getOccupation(), that.getOccupation()) && Objects.equals(getReferenceEtag(), that.getReferenceEtag()) && Objects.equals(getReferenceAppointmentId(), that.getReferenceAppointmentId()) && Objects.equals(getReferenceOfficerListEtag(), that.getReferenceOfficerListEtag()) && Objects.equals(getResignedOn(), that.getResignedOn()) && Objects.equals(getResidentialAddress(), that.getResidentialAddress()) && Objects.equals(getResidentialAddressBackLink(), that.getResidentialAddressBackLink()) && Objects.equals(getResidentialManualAddressBackLink(), that.getResidentialManualAddressBackLink()) && Objects.equals(getIsHomeAddressSameAsServiceAddress(), that.getIsHomeAddressSameAsServiceAddress()) && Objects.equals(getNationality2Link(), that.getNationality2Link()) && Objects.equals(getNationality3Link(), that.getNationality3Link()) && Objects.equals(getDirectorAppliedToProtectDetails(), that.getDirectorAppliedToProtectDetails()) && Objects.equals(getConsentToAct(), that.getConsentToAct()) && Objects.equals(getCheckYourAnswersLink(), that.getCheckYourAnswersLink()) && Objects.equals(getDirectorResidentialAddressChoice(), that.getDirectorResidentialAddressChoice()) && Objects.equals(getDirectorServiceAddressChoice(), that.getDirectorServiceAddressChoice()) && Objects.equals(getNameHasBeenUpdated(), that.getNameHasBeenUpdated()) && Objects.equals(getNationalityHasBeenUpdated(), that.getNationalityHasBeenUpdated()) && Objects.equals(getOccupationHasBeenUpdated(), that.getOccupationHasBeenUpdated()) && Objects.equals(getCorrespondenceAddressHasBeenUpdated(), that.getCorrespondenceAddressHasBeenUpdated()) && Objects.equals(getResidentialAddressHasBeenUpdated(), that.getResidentialAddressHasBeenUpdated()) && Objects.equals(getDirectorsDetailsChangedDate(), that.getDirectorsDetailsChangedDate()) && Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServiceAddress(), getServiceAddressBackLink(), getServiceManualAddressBackLink(), getProtectedDetailsBackLink(), getIsServiceAddressSameAsRegisteredOfficeAddress(), getAppointedOn(), getCountryOfResidence(), getDateOfBirth(), getFormerNames(), getIdentification(), getName(), getTitle(), getFirstName(), getMiddleNames(), getLastName(), getNationality1(), getNationality2(), getNationality3(), getOccupation(), getReferenceEtag(), getReferenceAppointmentId(), getReferenceOfficerListEtag(), getResignedOn(), getResidentialAddress(), getResidentialAddressBackLink(), getResidentialManualAddressBackLink(), getIsHomeAddressSameAsServiceAddress(), getNationality2Link(), getNationality3Link(), getDirectorAppliedToProtectDetails(), getConsentToAct(), getCheckYourAnswersLink(), getDirectorResidentialAddressChoice(), getDirectorServiceAddressChoice(), getNameHasBeenUpdated(), getNationalityHasBeenUpdated(), getOccupationHasBeenUpdated(), getCorrespondenceAddressHasBeenUpdated(), getResidentialAddressHasBeenUpdated(), getDirectorsDetailsChangedDate(), getDescription());
    }

    @Override
    public String toString() {
        return "OfficerFilingDto{" +
                "serviceAddress=" + serviceAddress +
                ", serviceAddressBackLink='" + serviceAddressBackLink + '\'' +
                ", serviceManualAddressBackLink='" + serviceManualAddressBackLink + '\'' +
                ", protectedDetailsBackLink='" + protectedDetailsBackLink + '\'' +
                ", isServiceAddressSameAsRegisteredOfficeAddress=" + isServiceAddressSameAsRegisteredOfficeAddress +
                ", appointedOn=" + appointedOn +
                ", countryOfResidence='" + countryOfResidence + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", formerNames='" + formerNames + '\'' +
                ", identification=" + identification +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleNames='" + middleNames + '\'' +
                ", lastName='" + lastName + '\'' +
                ", nationality1='" + nationality1 + '\'' +
                ", nationality2='" + nationality2 + '\'' +
                ", nationality3='" + nationality3 + '\'' +
                ", occupation='" + occupation + '\'' +
                ", referenceEtag='" + referenceEtag + '\'' +
                ", referenceAppointmentId='" + referenceAppointmentId + '\'' +
                ", referenceOfficerListEtag='" + referenceOfficerListEtag + '\'' +
                ", resignedOn=" + resignedOn +
                ", residentialAddress=" + residentialAddress +
                ", residentialAddressBackLink='" + residentialAddressBackLink + '\'' +
                ", residentialManualAddressBackLink='" + residentialManualAddressBackLink + '\'' +
                ", isHomeAddressSameAsServiceAddress=" + isHomeAddressSameAsServiceAddress +
                ", nationality2Link=" + nationality2Link +
                ", nationality3Link=" + nationality3Link +
                ", directorAppliedToProtectDetails=" + directorAppliedToProtectDetails +
                ", consentToAct=" + consentToAct +
                ", checkYourAnswersLink='" + checkYourAnswersLink + '\'' +
                ", directorResidentialAddressChoice='" + directorResidentialAddressChoice + '\'' +
                ", directorServiceAddressChoice='" + directorServiceAddressChoice + '\'' +
                ", nameHasBeenUpdated=" + nameHasBeenUpdated +
                ", nationalityHasBeenUpdated=" + nationalityHasBeenUpdated +
                ", occupationHasBeenUpdated=" + occupationHasBeenUpdated +
                ", correspondenceAddressHasBeenUpdated=" + correspondenceAddressHasBeenUpdated +
                ", residentialAddressHasBeenUpdated=" + residentialAddressHasBeenUpdated +
                ", directorsDetailsChangedDate=" + directorsDetailsChangedDate +
                ", description='" + description +
                '}';
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

        public Builder serviceAddressBackLink(final String value) {

            buildSteps.add(data -> data.serviceAddressBackLink = value);
            return this;
        }

        public Builder serviceManualAddressBackLink(final String value) {

            buildSteps.add(data -> data.serviceManualAddressBackLink = value);
            return this;
        }

        public Builder protectedDetailsBackLink(final String value) {

            buildSteps.add(data -> data.protectedDetailsBackLink = value);
            return this;
        }

        @JsonDeserialize(using = JsonBooleanDeserializer.class)
        public Builder isServiceAddressSameAsRegisteredOfficeAddress(final Boolean value) {

            buildSteps.add(data -> data.isServiceAddressSameAsRegisteredOfficeAddress = value);
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

        public Builder residentialManualAddressBackLink(final String value) {

            buildSteps.add(data -> data.residentialManualAddressBackLink = value);
            return this;
        }

        public Builder directorResidentialAddressChoice(final String value) {
            buildSteps.add(data -> data.directorResidentialAddressChoice = value);
            return this;
        }

        public Builder directorServiceAddressChoice(final String value) {
            buildSteps.add(data -> data.directorServiceAddressChoice = value);
            return this;
        }

        @JsonDeserialize(using = JsonBooleanDeserializer.class)
        public Builder isHomeAddressSameAsServiceAddress(final Boolean value) {

            buildSteps.add(data -> data.isHomeAddressSameAsServiceAddress = value);
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

        @JsonDeserialize(using = JsonBooleanDeserializer.class)
        public Builder directorAppliedToProtectDetails(final Boolean value) {
            buildSteps.add(data -> data.directorAppliedToProtectDetails = value);
            return this;
        }

        @JsonDeserialize(using = JsonBooleanDeserializer.class)
        public Builder consentToAct(final Boolean value) {
            buildSteps.add(data -> data.consentToAct = value);
            return this;
        }

        public Builder checkYourAnswersLink(final String value) {
            buildSteps.add(data -> data.checkYourAnswersLink = value);
            return this;
        }

        public Builder nameHasBeenUpdated(final Boolean value) {
            buildSteps.add(data -> data.nameHasBeenUpdated = value);
            return this;
        }

        public Builder nationalityHasBeenUpdated(final Boolean value) {
            buildSteps.add(data -> data.nationalityHasBeenUpdated = value);
            return this;
        }

        public Builder occupationHasBeenUpdated(final Boolean value) {
            buildSteps.add(data -> data.occupationHasBeenUpdated = value);
            return this;
        }

        public Builder correspondenceAddressHasBeenUpdated(final Boolean value) {
            buildSteps.add(data -> data.correspondenceAddressHasBeenUpdated = value);
            return this;
        }

        public Builder residentialAddressHasBeenUpdated(final Boolean value) {
            buildSteps.add(data -> data.residentialAddressHasBeenUpdated = value);
            return this;
        }

        public Builder directorsDetailsChangedDate(final LocalDate value) {
            buildSteps.add(data -> data.directorsDetailsChangedDate = value);
            return this;
        }

        public Builder description(final String value) {
            buildSteps.add(data -> data.description = value);
            return this;
        }

        public OfficerFilingDto build() {

            final var data = new OfficerFilingDto();
            buildSteps.forEach(step -> step.accept(data));

            return data;
        }
    }
}
