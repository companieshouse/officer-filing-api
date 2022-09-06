package uk.gov.companieshouse.officerfiling.api.model.dto;

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
}
