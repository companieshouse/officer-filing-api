package uk.gov.companieshouse.officerfiling.api.model.filing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.officerfiling.api.annotations.Default;
import uk.gov.companieshouse.officerfiling.api.model.entity.Address;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilingData {

    private String title;
    private String firstName;
    private String middleNames;
    private String lastName;
    private String formerNames;
    private String dateOfBirth;
    private String appointedOn;
    private String resignedOn;
    private String nationality1;
    private String nationality2;
    private String nationality3;
    private String occupation;
    private Address serviceAddress;
    private Boolean isServiceAddressSameAsRegisteredOfficeAddress;
    private Address residentialAddress;
    private Boolean isServiceAddressSameAsHomeAddress;
    private Boolean directorAppliedToProtectDetails;
    private Boolean consentToAct;
    @JsonProperty("is_corporate_director")
    private Boolean corporateDirector;

    @JsonCreator
    public FilingData(@JsonProperty("first_name") String firstName,
                      @JsonProperty("middle_names") String middleNames,
                      @JsonProperty("last_name") String lastName,
                      @JsonProperty("date_of_birth") String dateOfBirth,
                      @JsonProperty("resigned_on") String resignedOn,
                      @JsonProperty("is_corporate_director") Boolean corporateDirector) {
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.resignedOn = resignedOn;
        this.corporateDirector = corporateDirector;
    }

    @JsonCreator
    @Default
    public FilingData(@JsonProperty("title") String title,
                         @JsonProperty("first_name") String firstName,
                         @JsonProperty("middle_names") String middleNames,
                         @JsonProperty("last_name") String lastName,
                         @JsonProperty("former_names") String formerNames,
                         @JsonProperty("date_of_birth") String dateOfBirth,
                         @JsonProperty("appointed_on") String appointedOn,
                         @JsonProperty("resigned_on") String resignedOn,
                         @JsonProperty("nationality1") String nationality1,
                         @JsonProperty("nationality2") String nationality2,
                         @JsonProperty("nationality3") String nationality3,
                         @JsonProperty("occupation") String occupation,
                         @JsonProperty("service_address") Address serviceAddress,
                         @JsonProperty("is_service_address_same_as_registered_office_address") Boolean isServiceAddressSameAsRegisteredOfficeAddress,
                         @JsonProperty("residential_address") Address residentialAddress,
                         @JsonProperty("is_service_address_same_as_home_address") Boolean isServiceAddressSameAsHomeAddress,
                         @JsonProperty("director_applied_to_protect_details") Boolean directorAppliedToProtectDetails,
                         @JsonProperty("consent_to_act") Boolean consentToAct,
                         @JsonProperty("is_corporate_director") Boolean corporateDirector) {
        this.title = title;
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.lastName = lastName;
        this.formerNames = formerNames;
        this.dateOfBirth = dateOfBirth;
        this.appointedOn = appointedOn;
        this.resignedOn = resignedOn;
        this.nationality1 = nationality1;
        this.nationality2 = nationality2;
        this.nationality3 = nationality3;
        this.occupation = occupation;
        this.serviceAddress = serviceAddress;
        this.isServiceAddressSameAsRegisteredOfficeAddress = isServiceAddressSameAsRegisteredOfficeAddress;
        this.residentialAddress = residentialAddress;
        this.isServiceAddressSameAsHomeAddress = isServiceAddressSameAsHomeAddress;
        this.directorAppliedToProtectDetails = directorAppliedToProtectDetails;
        this.consentToAct = consentToAct;
        this.corporateDirector = corporateDirector;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleNames() {
        return middleNames;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFormerNames() {
        return formerNames;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAppointedOn() {
        return appointedOn;
    }

    public String getResignedOn() {
        return resignedOn;
    }

    public String getNationality1() {
        return nationality1;
    }

    public String getNationality2() {
        return nationality2;
    }

    public String getNationality3() {
        return nationality3;
    }

    public String getOccupation() {
        return occupation;
    }

    public Address getServiceAddress() {
        return serviceAddress;
    }

    public Boolean getServiceAddressSameAsRegisteredOfficeAddress() {
        return isServiceAddressSameAsRegisteredOfficeAddress;
    }

    public Address getResidentialAddress() {
        return residentialAddress;
    }

    public Boolean getServiceAddressSameAsHomeAddress() {
        return isServiceAddressSameAsHomeAddress;
    }

    public Boolean getDirectorAppliedToProtectDetails() {
        return directorAppliedToProtectDetails;
    }

    public Boolean getConsentToAct() {
        return consentToAct;
    }

    public Boolean getCorporateDirector() {
        return corporateDirector;
    }

    @Override
    public String toString() {
        return "FilingData{" +
                "title='" + title + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleNames='" + middleNames + '\'' +
                ", lastName='" + lastName + '\'' +
                ", formerNames='" + formerNames + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", appointedOn='" + appointedOn + '\'' +
                ", resignedOn='" + resignedOn + '\'' +
                ", nationality1='" + nationality1 + '\'' +
                ", nationality2='" + nationality2 + '\'' +
                ", nationality3='" + nationality3 + '\'' +
                ", occupation='" + occupation + '\'' +
                ", serviceAddress=" + serviceAddress +
                ", isServiceAddressSameAsRegisteredOfficeAddress=" + isServiceAddressSameAsRegisteredOfficeAddress +
                ", residentialAddress=" + residentialAddress +
                ", isServiceAddressSameAsHomeAddress=" + isServiceAddressSameAsHomeAddress +
                ", directorAppliedToProtectDetails=" + directorAppliedToProtectDetails +
                ", consentToAct=" + consentToAct +
                ", corporateDirector=" + corporateDirector +
                '}';
    }
}
