package uk.gov.companieshouse.officerfiling.api.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.companieshouse.api.model.common.Address;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerIdentificationTypeMapper;

@Document(collection = "officer")
public class ActiveOfficerDetails {
    @JsonProperty("fore_name_1")
    private String foreName1;
    @JsonProperty("fore_name_2")
    private String foreName2;
    private String surname;
    private String occupation;
    private String nationality;
    @JsonProperty("date_of_birth")
    private String dateOfBirth;
    @JsonProperty("country_of_residence")
    private String countryOfResidence;
    @JsonProperty("date_of_appointment")
    private String dateOfAppointment;
    @JsonProperty("service_address")
    private Address serviceAddress;
    @JsonProperty("residential_address")
    private Address residentialAddress;
    @JsonProperty("is_corporate")
    private boolean corporate;
    private String role;
    @JsonProperty("place_registered")
    private String placeRegistered;
    @JsonProperty("registration_number")
    private String registrationNumber;
    @JsonProperty("law_governed")
    private String lawGoverned;
    @JsonProperty("legal_form")
    private String legalForm;
    @JsonProperty("identification_type")
    private String identificationType;

    public String getForeName1() {
        return foreName1;
    }

    public void setForeName1(String foreName1) {
        this.foreName1 = foreName1;
    }

    public String getForeName2() {
        return foreName2;
    }

    public void setForeName2(String foreName2) {
        this.foreName2 = foreName2;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCountryOfResidence() {
        return countryOfResidence;
    }

    public void setCountryOfResidence(String countryOfResidence) {
        this.countryOfResidence = countryOfResidence;
    }

    public Address getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(Address serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public Address getResidentialAddress() {
        return residentialAddress;
    }

    public void setResidentialAddress(Address residentialAddress) {
        this.residentialAddress = residentialAddress;
    }

    public String getDateOfAppointment() {
        return dateOfAppointment;
    }

    public void setDateOfAppointment(String dateOfAppointment) {
        this.dateOfAppointment = dateOfAppointment;
    }

    public boolean isCorporate() {
        return corporate;
    }

    public void setCorporate(boolean corporate) {
        this.corporate = corporate;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPlaceRegistered() {
        return placeRegistered;
    }

    public void setPlaceRegistered(String placeRegistered) {
        this.placeRegistered = placeRegistered;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getLawGoverned() {
        return lawGoverned;
    }

    public void setLawGoverned(String lawGoverned) {
        this.lawGoverned = lawGoverned;
    }

    public String getLegalForm() {
        return legalForm;
    }

    public void setLegalForm(String legalForm) {
        this.legalForm = legalForm;
    }

    public String getIdentificationType() {
        return identificationType;
    }

    public void setIdentificationType(String identificationType) {
        if (StringUtils.isNotBlank(identificationType)) {
            this.identificationType =
                    OfficerIdentificationTypeMapper.mapIdentificationTypeToChs(identificationType);
        }
    }
}
