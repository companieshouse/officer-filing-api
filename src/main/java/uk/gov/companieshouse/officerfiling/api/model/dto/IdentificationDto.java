package uk.gov.companieshouse.officerfiling.api.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.StringJoiner;

public class IdentificationDto {

    private final String identificationType;
    private final String legalAuthority;
    private final String legalForm;
    private final String placeRegistered;
    private final String registrationNumber;

    @JsonCreator
    public IdentificationDto(@JsonProperty("identification_type") final String identificationType,
            @JsonProperty("legal_authority") final String legalAuthority,
            @JsonProperty("legal_form") final String legalForm,
            @JsonProperty("place_registered") final String placeRegistered,
            @JsonProperty("registration_number") final String registrationNumber) {
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
        final IdentificationDto that = (IdentificationDto) o;
        return Objects.equals(getIdentificationType(), that.getIdentificationType())
                && Objects.equals(getLegalAuthority(), that.getLegalAuthority())
                && Objects.equals(getLegalForm(), that.getLegalForm())
                && Objects.equals(getPlaceRegistered(), that.getPlaceRegistered())
                && Objects.equals(getRegistrationNumber(), that.getRegistrationNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentificationType(), getLegalAuthority(), getLegalForm(),
                getPlaceRegistered(), getRegistrationNumber());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", IdentificationDto.class.getSimpleName() + "[", "]").add(
                        "identificationType='" + identificationType + "'")
                .add("legalAuthority='" + legalAuthority + "'")
                .add("legalForm='" + legalForm + "'")
                .add("placeRegistered='" + placeRegistered + "'")
                .add("registrationNumber='" + registrationNumber + "'")
                .toString();
    }
}
