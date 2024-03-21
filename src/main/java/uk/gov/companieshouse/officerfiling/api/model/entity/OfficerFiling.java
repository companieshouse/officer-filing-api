package uk.gov.companieshouse.officerfiling.api.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;

@Document(collection = "officer_filing")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfficerFiling {

    @Id
    private String id;
    private Instant createdAt;
    private Identification identification;
    private String kind;
    private Links links;
    private OfficerFilingData data;
    private Instant updatedAt;

    private OfficerFiling() {
    }

    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
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

    public OfficerFilingData getData() {
        return data;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
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
        return Objects.equals(getCreatedAt(), that.getCreatedAt())
                && Objects.equals(getIdentification(), that.getIdentification())
                && Objects.equals(getKind(), that.getKind())
                && Objects.equals(getLinks(), that.getLinks())
                && Objects.equals(getData(), that.getData())
                && Objects.equals(getUpdatedAt(), that.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCreatedAt(), getIdentification(), getKind(), getLinks(), getData(),
                getUpdatedAt());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OfficerFiling.class.getSimpleName() + "[", "]").add(
                        "id='" + id + "'")
                .add("createdAt=" + createdAt)
                .add("identification=" + identification)
                .add("kind='" + kind + "'")
                .add("links=" + links)
                .add("data='" + data + "'")
                .add("updatedAt=" + updatedAt)
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
                    .createdAt(other.getCreatedAt())
                    .identification(other.getIdentification())
                    .kind(other.getKind())
                    .links(other.getLinks())
                    .data(other.getData())
                    .updatedAt(other.getUpdatedAt());
        }

        public Builder id(final String value) {
            buildSteps.add(buildData -> buildData.id = value);
            return this;
        }

        public Builder createdAt(final Instant value) {

            buildSteps.add(buildData -> buildData.createdAt = value);
            return this;
        }

        public Builder identification(final Identification value) {

            buildSteps.add(buildData -> buildData.identification = Optional.ofNullable(value)
                    .map(v -> new Identification(v.getIdentificationType(), v.getLegalAuthority(),
                            v.getLegalForm(), v.getPlaceRegistered(), v.getRegistrationNumber()))
                    .orElse(null));
            return this;
        }

        public Builder kind(final String value) {

            buildSteps.add(buildData -> buildData.kind = value);
            return this;
        }

        public Builder links(final Links value) {

            buildSteps.add(buildData -> buildData.links = Optional.ofNullable(value)
                    .map(v -> new Links(v.getSelf(), v.getValidationStatus()))
                    .orElse(null));
            return this;
        }

        public Builder data(final OfficerFilingData value) {
            buildSteps.add(buildData -> buildData.data = Optional.ofNullable(value)
                    .map(v -> OfficerFilingData.builder()
                            .serviceAddress(v.getServiceAddress())
                            .serviceAddressBackLink(v.getServiceAddressBackLink())
                            .serviceManualAddressBackLink(v.getServiceManualAddressBackLink())
                            .protectedDetailsBackLink(v.getProtectedDetailsBackLink())
                            .isServiceAddressSameAsRegisteredOfficeAddress(v.getIsServiceAddressSameAsRegisteredOfficeAddress())
                            .appointedOn(v.getAppointedOn())
                            .countryOfResidence(v.getCountryOfResidence())
                            .dateOfBirth(v.getDateOfBirth())
                            .formerNames(v.getFormerNames())
                            .title(v.getTitle())
                            .name(v.getName())
                            .firstName(v.getFirstName())
                            .middleNames(v.getMiddleNames())
                            .lastName(v.getLastName())
                            .nationality1(v.getNationality1())
                            .nationality2(v.getNationality2())
                            .nationality3(v.getNationality3())
                            .nationality2Link(v.getNationality2Link())
                            .nationality3Link(v.getNationality3Link())
                            .directorAppliedToProtectDetails(v.getDirectorAppliedToProtectDetails())
                            .consentToAct(v.getConsentToAct())
                            .occupation(v.getOccupation())
                            .officerRole(v.getOfficerRole())
                            .referenceEtag(v.getReferenceEtag())
                            .referenceAppointmentId(v.getReferenceAppointmentId())
                            .referenceOfficerListEtag(v.getReferenceOfficerListEtag())
                            .resignedOn(v.getResignedOn())
                            .status(v.getStatus())
                            .residentialAddress(v.getResidentialAddress())
                            .residentialAddressBackLink(v.getResidentialAddressBackLink())
                            .residentialManualAddressBackLink(v.getResidentialManualAddressBackLink())
                            .directorResidentialAddressChoice(v.getDirectorResidentialAddressChoice())
                            .directorServiceAddressChoice(v.getDirectorServiceAddressChoice())
                            .isHomeAddressSameAsServiceAddress(v.getIsHomeAddressSameAsServiceAddress())
                            .corporateDirector(v.getCorporateDirector())
                            .checkYourAnswersLink(v.getCheckYourAnswersLink())
                            .officerPreviousDetails(v.getOfficerPreviousDetails())
                            .directorsDetailsChangedDate(v.getDirectorsDetailsChangedDate())
                            .nameHasBeenUpdated(v.getNameHasBeenUpdated())
                            .nationalityHasBeenUpdated(v.getNationalityHasBeenUpdated())
                            .occupationHasBeenUpdated(v.getOccupationHasBeenUpdated())
                            .serviceAddressHasBeenUpdated(v.getServiceAddressHasBeenUpdated())
                            .residentialAddressHasBeenUpdated(v.getResidentialAddressHasBeenUpdated())
                            .build())
                    .orElse(null));
            return this;
        }

        public Builder updatedAt(final Instant value) {

            buildSteps.add(buildData -> buildData.updatedAt = value);
            return this;
        }

        public OfficerFiling build() {
            final var officerFiling = new OfficerFiling();
            buildSteps.forEach(s -> s.accept(officerFiling));

            return officerFiling;
        }
    }

}
