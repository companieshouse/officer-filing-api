package uk.gov.companieshouse.officerfiling.api.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormerName {

    String forenames;
    String surname;

    public FormerName(String forenames, String surname) {
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
        return Objects.equals(getForenames(), that.getForenames()) &&
                Objects.equals(getSurname(), that.getSurname());
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
}
