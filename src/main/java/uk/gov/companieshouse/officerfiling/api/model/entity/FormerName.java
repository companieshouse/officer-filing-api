package uk.gov.companieshouse.officerfiling.api.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormerName {

    String forenames;
    String surname;

    public FormerName() {
    }

    public FormerName(final String forenames, final String surname) {
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
        final FormerName that = (FormerName) o;
        return Objects.equals(getForenames(), that.getForenames()) && Objects.equals(getSurname(),
                that.getSurname());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getForenames(), getSurname());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FormerName.class.getSimpleName() + "[", "]").add(
                "forenames='" + forenames + "'").add("surname='" + surname + "'").toString();
    }

    public static Builder builder() {
        return new FormerName.Builder();
    }

    public static Builder builder(final FormerName other) {
        return new Builder(other);
    }
    public static class Builder {

        private final List<Consumer<FormerName>> buildSteps;

        public Builder() {
            buildSteps = new ArrayList<>();
        }

        public Builder(final FormerName other) {
            this();
            this.forenames(other.getForenames())
                    .surname(other.getSurname());
        }

        public Builder forenames(final String value) {
            buildSteps.add(data -> data.forenames = value);
            return this;
        }

        public Builder surname(final String value) {
            buildSteps.add(data -> data.surname = value);
            return this;
        }

        public FormerName build() {
            final var data = new FormerName();
            buildSteps.forEach(step -> step.accept(data));
            return data;
        }
    }
}
