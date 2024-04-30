package uk.gov.companieshouse.officerfiling.api.validation;

import org.apache.commons.lang.StringUtils;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;
import uk.gov.companieshouse.officerfiling.api.error.ApiErrors;
import uk.gov.companieshouse.officerfiling.api.model.dto.AddressDto;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;
import uk.gov.companieshouse.officerfiling.api.validation.error.CorrespondenceAddressErrorProvider;
import uk.gov.companieshouse.officerfiling.api.validation.error.ResidentialAddressErrorProvider;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.*;

/**
 * Provides all validation that should be carried out when an officer is updated. Fetches all data necessary to complete
 * the validation and generates a list of errors that can be sent back to the caller.
 */
public class OfficerUpdateValidator extends OfficerValidator {

    private final Logger logger;
    private final AddressValidator addressValidator;

    public OfficerUpdateValidator(final Logger logger,
                                  final CompanyAppointmentService companyAppointmentService,
                                  final CompanyProfileService companyProfileService,
                                  final String inputAllowedNationalities,
                                  final ApiEnumerations apiEnumerations,
                                  final AddressValidator addressValidator) {
        super(logger, companyProfileService, companyAppointmentService, inputAllowedNationalities, apiEnumerations);
        this.logger = logger;
        this.addressValidator = addressValidator;
    }

    /**
     * Main validation method to fetch the required data and validate the request. This should be the point of call when updating an officer.
     *
     * @param request           The servlet request used in logging
     * @param dto               Data Object containing details of the update
     * @param transaction       the transaction for this update
     * @param passthroughHeader ERIC pass through header for authorisation
     * @return An object containing a list of any validation errors that have been raised
     */
    @Override
    public ApiErrors validate(HttpServletRequest request, OfficerFilingDto dto, Transaction transaction, String passthroughHeader) {
        logger.debugContext(transaction.getId(), "Beginning officer update validation", new LogHelper.Builder(transaction)
                .withRequest(request)
                .build());
        final List<ApiError> errorList = new ArrayList<>();
        validateRequiredTransactionFields(request, errorList, transaction);
        validateRequiredDtoFields(request, errorList, dto);

        // Retrieve data objects required for the validation process
        final Optional<CompanyProfileApi> companyProfile = getCompanyProfile(request, errorList, transaction, passthroughHeader);
        final Optional<AppointmentFullRecordAPI> companyAppointment = getOfficerAppointment(request, errorList, dto, transaction, passthroughHeader);
        if (companyProfile.isEmpty() || companyAppointment.isEmpty()) {
            return new ApiErrors(errorList);
        }

        // Perform validation
        validateSubmissionInformationInDate(request, dto, companyAppointment.get(), errorList);
        validateCompanyNotDissolved(request, errorList, companyProfile.get());
        validateChangeDateAfterAppointmentDate(request, errorList, dto, companyAppointment.get());
        validateChangeDateAfterIncorporationDate(request, errorList, dto, companyProfile.get());
        validateAddressesMultipleFlagsUpdate(request, errorList, dto, companyAppointment.get());
        Boolean nonEmptyNameSection = validateNameSection(request, errorList, dto, companyAppointment.get());
        Boolean nonEmptyNationalitySection = validateNationalitySection(request, errorList, dto, companyAppointment.get());
        Boolean nonEmptyOccupationSection = validateOccupationSection(request, errorList, dto, companyAppointment.get());
        Boolean nonEmptyCorrespondenceAddressSection = validateCorrespondenceAddressSection(request, errorList, dto, companyAppointment.get());
        Boolean nonEmptyResidentialAddressSection = validateResidentialAddressSection(request, errorList, dto, companyAppointment.get());

        if (Boolean.FALSE.equals(nonEmptyNameSection) && Boolean.FALSE.equals(nonEmptyNationalitySection)
                && Boolean.FALSE.equals(nonEmptyOccupationSection) && Boolean.FALSE.equals(nonEmptyCorrespondenceAddressSection)
                && Boolean.FALSE.equals(nonEmptyResidentialAddressSection)) {
           createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.BLANK_CH01_SUBMISSION));
        }

        return new ApiErrors(errorList);
    }

    private void validateRequiredDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        validateChangeDate(request, errorList, dto);
        validateEtagPresent(request, dto, errorList);
    }

    public void validateChangeDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getDirectorsDetailsChangedDate() == null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_MISSING));
        } else {
            validateChangeDatePastOrPresent(request, errorList, dto);
            validateMinChangeDate(request, errorList, dto);
        }
    }

    public void validateChangeDatePastOrPresent(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getDirectorsDetailsChangedDate().isAfter(LocalDate.now())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_IN_PAST));
        }
    }

    public void validateMinChangeDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getDirectorsDetailsChangedDate().isBefore(MIN_RESIGNATION_DATE)) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_BEFORE_2009));
        }
    }

    public void validateChangeDateAfterIncorporationDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, CompanyProfileApi companyProfile) {
        if (companyProfile.getDateOfCreation() == null) {
            logger.errorRequest(request, "null data was found in the Company Profile API within the Date Of Creation field");
            return;
        }
        if (dto.getDirectorsDetailsChangedDate() == null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_MISSING));
        } else if (dto.getDirectorsDetailsChangedDate().isBefore(companyProfile.getDateOfCreation())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_AFTER_INCORPORATION_DATE));
        }
    }

    public void validateChangeDateAfterAppointmentDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI companyAppointment) {
        var appointmentDate = getAppointmentDate(request, companyAppointment);
        if (dto.getDirectorsDetailsChangedDate() == null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_MISSING));
        } else if (appointmentDate.isPresent() && dto.getDirectorsDetailsChangedDate().isBefore(appointmentDate.get())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_BEFORE_OFFICER_APPOINTMENT_DATE));
        }
    }

    /**
     * Validates the name section of the officer update request
     * @param request the servlet request
     * @param errorList the list of errors
     * @param dto the officer filing dto
     * @param appointment the appointment full record api
     * @return false if section is empty, returns true otherwise
     */
    public Boolean validateNameSection(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI appointment) {
        if (isSectionEmpty(dto.getNameHasBeenUpdated(), Arrays.asList(dto.getTitle(), dto.getFirstName(), dto.getMiddleNames(), dto.getLastName()))) {
            return false;
        }

        // If the section matches the current chips data then throw a validation error and don't continue
        if (doesNameMatchChipsData(dto, appointment)) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.NAME_MATCHES_CHIPS_DATA));
            return true;
        }
        // Perform validation
        validateTitle(request, errorList, dto);
        validateFirstName(request, errorList, dto);
        validateLastName(request, errorList, dto);
        validateMiddleNames(request, errorList, dto);
        return true;
    }

    /**
     * Validates the nationality section of the officer update request
     * @param request the servlet request
     * @param errorList the list of errors
     * @param dto the officer filing dto
     * @param appointment the appointment full record api
     * @return false if section is empty, returns true otherwise
     */
    public Boolean validateNationalitySection(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI appointment) {
        if (isSectionEmpty(dto.getNationalityHasBeenUpdated(), Arrays.asList(dto.getNationality1(), dto.getNationality2(), dto.getNationality3()))) {
            return false;
        }
        // If the section matches the current chips data then throw a validation error and don't continue
        if (doesNationalityMatchChipsData(dto, appointment)) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.NATIONALITY_MATCHES_CHIPS_DATA));
            return true;
        }
        // Perform validation
        validateNationality1(request, errorList, dto);
        validateNationality2(request, errorList, dto);
        validateNationality3(request, errorList, dto);
        validateNationalityLength(request, errorList, dto);
        return true;
    }

    /**
     * Validates the occupation section of the officer update request
     * @param request the servlet request
     * @param errorList the list of errors
     * @param dto the officer filing dto
     * @param appointmentFullRecordAPI the appointment full record api
     * @return false if section is empty, returns true otherwise
     */
    public Boolean validateOccupationSection(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI appointmentFullRecordAPI) {
        if(isSectionEmpty(dto.getOccupationHasBeenUpdated(), Collections.singletonList(dto.getOccupation()))){
            return false;
        }

        // If the section matches the current chips data then throw a validation error and don't continue
        if (doesOccupationMatchChipsData(dto, appointmentFullRecordAPI)) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.OCCUPATION_MATCHES_CHIPS_DATA));
            return true;
        }
        validateOccupation(request, errorList, dto);
        return true;
    }

    /**
     * Validates the correspondence address section of the officer update request
     * @param request the servlet request
     * @param errorList the list of errors
     * @param dto the officer filing dto
     * @param appointment the appointment full record api
     * @return false if the hasBeenUpdated flag is false, address is null and sameAs flag is null; returns true otherwise
     */
    public Boolean validateCorrespondenceAddressSection(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI appointment) {
        if (Boolean.FALSE.equals(dto.getServiceAddressHasBeenUpdated())) {
            return false;
        }
        // If address isn't null and any field within this section has been provided then continue
        if (isAddressNull(dto.getServiceAddress()) && dto.getIsServiceAddressSameAsRegisteredOfficeAddress() == null) {
            return false;
        }
        // If the section matches the current chips data then throw a validation error and don't continue
        if (doesAddressMatchChipsData(dto.getServiceAddress(), dto.getIsServiceAddressSameAsRegisteredOfficeAddress(), appointment.getServiceAddress(), appointment.getServiceAddressIsSameAsRegisteredOfficeAddress())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_MATCHES_CHIPS_DATA));
            return true;
        }
        // Perform validation if link is false or null
        if (!Boolean.TRUE.equals(dto.getIsServiceAddressSameAsRegisteredOfficeAddress())) {
            addressValidator.validate(new CorrespondenceAddressErrorProvider(apiEnumerations), request, errorList, dto.getServiceAddress());
        }
        return true;
    }

    /**
     * Validates the residential address section of the officer update request
     * @param request the servlet request
     * @param errorList the list of errors
     * @param dto the officer filing dto
     * @param appointment the appointment full record api
     * @return false if the hasBeenUpdated flag is false, address is null and sameAs flag is null; returns true otherwise
     */
    public Boolean validateResidentialAddressSection(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI appointment) {
        if (Boolean.FALSE.equals(dto.getResidentialAddressHasBeenUpdated())) {
            return false;
        }
        // If address isn't null and any field within this section has been provided then continue
        if (isAddressNull(dto.getResidentialAddress()) && dto.getIsHomeAddressSameAsServiceAddress() == null) {
            return false;
        }
        // If the section matches the current chips data then throw a validation error and don't continue
        if (doesAddressMatchChipsData(dto.getResidentialAddress(), dto.getIsHomeAddressSameAsServiceAddress(), appointment.getUsualResidentialAddress(), appointment.getResidentialAddressIsSameAsServiceAddress())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_MATCHES_CHIPS_DATA));
            return true;
        }
        // Perform validation if link is false or null
        if (!Boolean.TRUE.equals(dto.getIsHomeAddressSameAsServiceAddress())) {
            addressValidator.validate(new ResidentialAddressErrorProvider(apiEnumerations), request, errorList, dto.getResidentialAddress());
        }
        return true;
    }

    private boolean isAddressNull(AddressDto address) {
        return address == null || (
                address.getPremises() == null &&
                        address.getAddressLine1() == null &&
                        address.getAddressLine2() == null &&
                        address.getLocality() == null &&
                        address.getRegion() == null &&
                        address.getPostalCode() == null &&
                        address.getCountry() == null);
    }

    /**
     * Util method to validate if a section has been updated and if the section is empty
     * @param hasSectionBeenUpdated boolean to indicate if the section has been updated
     * @param sectionValues list of values within the section
     * @return true if the all sectionValues are null, false if flag is false or any one value is non-null.
     */
    boolean isSectionEmpty(Boolean hasSectionBeenUpdated, List<String> sectionValues) {
        if (Boolean.FALSE.equals(hasSectionBeenUpdated)) {
            return true;
        }
        var isEmpty = true;
        for (String value : sectionValues) {
            if (value != null) {
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    }

    public boolean doesNationalityMatchChipsData(OfficerFilingDto dto, AppointmentFullRecordAPI appointment) {
        if (appointment.getNationality() == null) {
            return false;
        }
        final String[] chipsNationalities = appointment.getNationality().split(",");

        if (!matchesChipsField(dto.getNationality1(), chipsNationalities[0])) {
            return false;
        }
        if (chipsNationalities.length < 2 && dto.getNationality2() != null) {
            return false;
        }
        if (chipsNationalities.length >= 2 && !matchesChipsField(dto.getNationality2(), chipsNationalities[1])) {
            return false;
        }
        if (chipsNationalities.length < 3 && dto.getNationality3() != null) {
            return false;
        }
        return chipsNationalities.length < 3 || matchesChipsField(dto.getNationality3(), chipsNationalities[2]);
    }

    public boolean doesNameMatchChipsData(OfficerFilingDto dto, AppointmentFullRecordAPI appointment) {
        return matchesChipsField(dto.getTitle(), appointment.getTitle()) &&
                matchesChipsField(dto.getFirstName(), appointment.getForename()) &&
                matchesChipsField(dto.getMiddleNames(), appointment.getOtherForenames()) &&
                matchesChipsField(dto.getLastName(), appointment.getSurname());
    }

    public boolean doesOccupationMatchChipsData(OfficerFilingDto dto, AppointmentFullRecordAPI appointmentFullRecordAPI) {
        if (appointmentFullRecordAPI.getOccupation().equalsIgnoreCase("NONE") && ("NONE".equalsIgnoreCase(dto.getOccupation()) || StringUtils.isEmpty(dto.getOccupation()))) {
            return true;
        }

        final String chipsOccupation = appointmentFullRecordAPI.getOccupation();
        return matchesChipsField(dto.getOccupation(), chipsOccupation);
    }

    public boolean doesAddressMatchChipsData(AddressDto filingAddress, Boolean filingSameAsLink, AddressAPI chipsAddress, Boolean chipsSameAsLink) {
        // If sameAs link is true in the filing then the address can be ignored
        if (Boolean.TRUE.equals(filingSameAsLink)) {
            return Boolean.TRUE.equals(chipsSameAsLink);
        }
        if (Boolean.TRUE.equals(chipsSameAsLink)) {
            return false;
        }
        return compareAddresses(filingAddress, chipsAddress);
    }

    private boolean compareAddresses(AddressDto filingAddress, AddressAPI chipsAddress) {
        if (filingAddress == null && chipsAddress == null) {
            return true;
        }
        if (filingAddress == null || chipsAddress == null) {
            return false;
        }
        return matchesChipsField(filingAddress.getPremises(), chipsAddress.getPremises()) &&
                matchesChipsField(filingAddress.getAddressLine1(), chipsAddress.getAddressLine1()) &&
                matchesChipsField(filingAddress.getAddressLine2(), chipsAddress.getAddressLine2()) &&
                matchesChipsField(filingAddress.getLocality(), chipsAddress.getLocality()) &&
                matchesChipsField(filingAddress.getRegion(), chipsAddress.getRegion()) &&
                matchesChipsField(filingAddress.getPostalCode(), chipsAddress.getPostcode()) &&
                matchesChipsField(filingAddress.getCountry(), chipsAddress.getCountry());
    }

    private boolean matchesChipsField(String field, String chipsField) {
        if (field == null && chipsField == null) {
            return true;
        } else if (field == null || chipsField == null) {
            return false;
        }
        return field.trim().equalsIgnoreCase(chipsField.trim());
    }

    public void validateAddressesMultipleFlagsUpdate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, AppointmentFullRecordAPI appointmentFullRecordAPI) {
        // validate we are not setting both flags to true
        if ((!Boolean.FALSE.equals(dto.getResidentialAddressHasBeenUpdated()) && Boolean.TRUE.equals(dto.getIsHomeAddressSameAsServiceAddress())) && 
            (!Boolean.FALSE.equals(dto.getServiceAddressHasBeenUpdated()) && Boolean.TRUE.equals(dto.getIsServiceAddressSameAsRegisteredOfficeAddress()))) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.ADDRESS_LINKS_MULTIPLE_FLAGS));
        }

        // validate we are not only setting service address same are registered office address to true when home address is already same as service address
        if (!Boolean.FALSE.equals(dto.getServiceAddressHasBeenUpdated()) && Boolean.TRUE.equals(dto.getIsServiceAddressSameAsRegisteredOfficeAddress()) &&
            !(!Boolean.FALSE.equals(dto.getResidentialAddressHasBeenUpdated()) &&
              (Boolean.FALSE.equals(dto.getIsHomeAddressSameAsServiceAddress()) || (dto.getIsHomeAddressSameAsServiceAddress() ==  null && dto.getResidentialAddress() != null)) // not clearing home address
            ) &&
            Boolean.TRUE.equals(appointmentFullRecordAPI.getResidentialAddressIsSameAsServiceAddress())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.ADDRESS_LINKS_MULTIPLE_FLAGS));
        }

        // validate we are not only setting home address same as service address to true when service address is already same as registered office address
        if (!Boolean.FALSE.equals(dto.getResidentialAddressHasBeenUpdated()) && Boolean.TRUE.equals(dto.getIsHomeAddressSameAsServiceAddress()) &&
            !(!Boolean.FALSE.equals(dto.getServiceAddressHasBeenUpdated()) &&
              (Boolean.FALSE.equals(dto.getIsServiceAddressSameAsRegisteredOfficeAddress()) || (dto.getIsServiceAddressSameAsRegisteredOfficeAddress() == null && dto.getServiceAddress() != null)) // not clearing correspondence address
            ) &&
            Boolean.TRUE.equals(appointmentFullRecordAPI.getServiceAddressIsSameAsRegisteredOfficeAddress())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.ADDRESS_LINKS_MULTIPLE_FLAGS));
        }
    }
}
