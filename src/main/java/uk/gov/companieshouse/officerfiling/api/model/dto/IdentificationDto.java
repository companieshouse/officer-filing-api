package uk.gov.companieshouse.officerfiling.api.model.dto;

import java.util.Objects;
import java.util.StringJoiner;

public class IdentificationDto {

    private String identificationType;
    private String legalAuthority;
    private String legalForm;
    private String placeRegistered;
    private String registrationNumber;

    public IdentificationDto(String identificationType, String legalAuthority, String legalForm,
                          String placeRegistered, String registrationNumber) {
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
        return new StringJoiner(", ", IdentificationDto.class.getSimpleName() + "[", "]").add(
                        "identificationType='" + identificationType + "'")
                .add("legalAuthority='" + legalAuthority + "'")
                .add("legalForm='" + legalForm + "'")
                .add("placeRegistered='" + placeRegistered + "'")
                .add("registrationNumber='" + registrationNumber + "'")
                .toString();
    }
}
