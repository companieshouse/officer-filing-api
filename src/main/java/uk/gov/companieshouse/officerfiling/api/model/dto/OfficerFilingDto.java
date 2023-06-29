package uk.gov.companieshouse.officerfiling.api.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
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

    private OfficerFilingDataDto officerFilingData;
    private IdentificationDto identification;

    private OfficerFilingDto() {
    }

    public OfficerFilingDataDto getOfficerFilingData() {
        return officerFilingData;
    }

    public IdentificationDto getIdentification() {
        return identification;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final var that = (OfficerFilingDto) o;
        return (Objects.equals(getIdentification(), that.getIdentification())
                && Objects.equals(getOfficerFilingData(), that.getOfficerFilingData()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentification(), getOfficerFilingData());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OfficerFilingDto.class.getSimpleName() + "[", "]")
                .add("identification=" + identification.toString())
                .add("officerFilingData='" + officerFilingData.toString() + "'")
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

        public Builder identification(final IdentificationDto value) {

            buildSteps.add(data -> data.identification = Optional.ofNullable(value)
                    .map(v -> new IdentificationDto(v.getIdentificationType(),
                            v.getLegalAuthority(), v.getLegalForm(), v.getPlaceRegistered(),
                            v.getRegistrationNumber()))
                    .orElse(null));
            return this;
        }

        public Builder officerFilingData(final OfficerFilingDataDto value) {

            buildSteps.add(data -> data.officerFilingData = Optional.ofNullable(value)
                    .map(v -> new OfficerFilingDataDto(v.getAddress(),
                            v.getAddressSameAsRegisteredOfficeAddress(),
                            v.getAppointedOn(),
                            v.getCountryOfResidence(),
                            v.getDateOfBirth(),
                            v.getFormerNames(),
                            v.getName(),
                            v.getLastName(),
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
                            v.getCorporateDirector()))
                    .orElse(null));
            return this;
        }

        public OfficerFilingDto build() {

            final var data = new OfficerFilingDto();
            buildSteps.forEach(step -> step.accept(data));

            return data;
        }
    }
}
