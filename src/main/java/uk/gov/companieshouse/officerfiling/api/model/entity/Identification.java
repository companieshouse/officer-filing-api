package uk.gov.companieshouse.officerfiling.api.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Identification {

    private final String identificationType;
    private final String legalAuthority;
    private final String legalForm;
    private final String placeRegistered;
    private final String registrationNumber;

    public Identification(final String identificationType, final String legalAuthority, final String legalForm,
                          final String placeRegistered, final String registrationNumber) {
        this.identificationType = identificationType;
        this.legalAuthority = legalAuthority;
        this.legalForm = legalForm;
        this.placeRegistered = placeRegistered;
        this.registrationNumber = registrationNumber;
    }

    public String getIdentificationType() {
        return identificationType;
    }

    public String getLegalAuthority() {
        return legalAuthority;
    }

    public String getLegalForm() {
        return legalForm;
    }

    public String getPlaceRegistered() {
        return placeRegistered;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Identification that = (Identification) o;
        return Objects.equals(getIdentificationType(), that.getIdentificationType()) &&
            Objects.equals(getLegalAuthority(), that.getLegalAuthority()) &&
            Objects.equals(getLegalForm(), that.getLegalForm()) &&
            Objects.equals(getPlaceRegistered(), that.getPlaceRegistered()) &&
            Objects.equals(getRegistrationNumber(), that.getRegistrationNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentificationType(), getLegalAuthority(), getLegalForm(),
            getPlaceRegistered(), getRegistrationNumber());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Identification.class.getSimpleName() + "[", "]").add(
                        "identificationType='" + identificationType + "'")
                .add("legalAuthority='" + legalAuthority + "'")
                .add("legalForm='" + legalForm + "'")
                .add("placeRegistered='" + placeRegistered + "'")
                .add("registrationNumber='" + registrationNumber + "'")
                .toString();
    }
}
