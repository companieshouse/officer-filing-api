package uk.gov.companieshouse.officerfiling.api.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.StringJoiner;

public class FormerNameDto {

    String forenames;
    String surname;

    @JsonCreator
    public FormerNameDto(@JsonProperty("forenames") final String forenames,
            @JsonProperty("surname") final String surname) {
        this.forenames = forenames;
        this.surname = surname;
    }

    public String getForenames() {
        return forenames;
    }

    public String getSurname() {
        return surname;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FormerNameDto that = (FormerNameDto) o;
        return Objects.equals(getForenames(), that.getForenames()) && Objects.equals(getSurname(),
                that.getSurname());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getForenames(), getSurname());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FormerNameDto.class.getSimpleName() + "[", "]").add(
                "forenames='" + forenames + "'").add("surname='" + surname + "'").toString();
    }
}
