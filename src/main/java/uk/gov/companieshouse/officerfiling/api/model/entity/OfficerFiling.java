package uk.gov.companieshouse.officerfiling.api.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "officer_filing")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfficerFiling {

    @Id
    private String id;
    private Instant createdAt;
    private Identification identification;
    private String kind;
    private Links links;
    private OfficerFilingData officerFilingData;
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
    public OfficerFilingData getOfficerFilingData() {
        return officerFilingData;
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
                && Objects.equals(getOfficerFilingData(), that.getOfficerFilingData())
                && Objects.equals(getUpdatedAt(), that.getUpdatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCreatedAt(), getIdentification(), getKind(), getLinks(), getOfficerFilingData(),
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
                .add("data='" + officerFilingData + "'")
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
                    .officerFilingData(other.getOfficerFilingData())
                    .updatedAt(other.getUpdatedAt());
        }

        public Builder id(final String value) {
            buildSteps.add(data -> data.id = value);
            return this;
        }

        public Builder createdAt(final Instant value) {

            buildSteps.add(data -> data.createdAt = value);
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

        public Builder officerFilingData(final OfficerFilingData value) {
            buildSteps.add(data -> data.officerFilingData = Optional.ofNullable(value)
                    .map(v -> new OfficerFilingData(
                            v.getAddress(),
                            v.getAddressSameAsRegisteredOfficeAddress(),
                            v.getAppointedOn(),
                            v.getCountryOfResidence(),
                            v.getDateOfBirth(),
                            v.getFormerNames(),
                            v.getName(),
                            v.getFirstName(),
                            v.getLastName(),
                            v.getNationality(),
                            v.getOccupation(),
                            v.getOfficerRole(),
                            v.getReferenceEtag(),
                            v.getReferenceAppointmentId(),
                            v.getReferenceOfficerListEtag(),
                            v.getResignedOn(),
                            v.getStatus(),
                            v.getResidentialAddress(),
                            v.getResidentialAddressSameAsCorrespondenceAddress(),
                            v.getCorporateDirector()
                            ))
                    .orElse(null));
            return this;
        }

        public Builder updatedAt(final Instant value) {

            buildSteps.add(data -> data.updatedAt = value);
            return this;
        }

        public OfficerFiling build() {
            final var officerFiling = new OfficerFiling();
            buildSteps.forEach(s -> s.accept(officerFiling));

            return officerFiling;
        }
    }
}
