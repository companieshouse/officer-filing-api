package uk.gov.companieshouse.officerfiling.api.model.dto;

import uk.gov.companieshouse.officerfiling.api.model.entity.FormerName;

public class FormerNameDto {

    String forenames;
    String surname;

    public FormerNameDto(String forenames, String surnames) {
        this.forenames = forenames;
        this.surname = surnames;
    }

    public String getForenames() {
        return forenames;
    }

    public String getSurname() {
        return surname;
    }
}
