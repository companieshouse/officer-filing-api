package uk.gov.companieshouse.officerfiling.api.model.filing;

public class OfficerPreviousDetails {

    private final String title;
    private final String firstName;
    private final String middleNames;
    private final String lastName;
    private final String dateOfBirth;

    public OfficerPreviousDetails(String title, String firstName, String middleNames, String lastName, String dateOfBirth) {
        this.title = title;
        this.firstName = firstName;
        this.middleNames = middleNames;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String title;
        private String firstName;
        private String middleNames;
        private String lastName;
        private String dateOfBirth;

        private Builder() {
        }

        public static Builder anOfficerPreviousDetails() {
            return new Builder();
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder middleNames(String middleNames) {
            this.middleNames = middleNames;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder dateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public OfficerPreviousDetails build() {
            return new OfficerPreviousDetails(title, firstName, middleNames, lastName, dateOfBirth);
        }
    }

    @Override
    public String toString() {
        return "OfficerPreviousDetails{" +
                "title='" + title + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleNames='" + middleNames + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
}
