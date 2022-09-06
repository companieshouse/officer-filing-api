package uk.gov.companieshouse.officerfiling.api.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormerName {

    String forenames;
    String surname;

    public FormerName(String forenames, String surnames) {
        this.forenames = forenames;
        this.surname = surnames;
    }

    public FormerName(FormerName other) {
        this.forenames = other.forenames;
        this.surname = other.surname;
    }

    public String getForenames() {
        return forenames;
    }

    public String getSurname() {
        return surname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FormerName that = (FormerName) o;
        return Objects.equals(getForenames(), that.getForenames()) &&
            Objects.equals(getSurname(), that.getSurname());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getForenames(), getSurname());
    }
}
