package uk.gov.companieshouse.officerfiling.api.validation;

import org.apache.commons.lang.StringUtils;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;
import uk.gov.companieshouse.officerfiling.api.error.ApiErrors;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides all validation that should be carried out when an officer is terminated. Fetches all data necessary to complete
 * the validation and generates a list of errors that can be sent back to the caller.
 */
public class OfficerAppointmentValidator extends OfficerValidator {

    private Logger logger;
    private ApiEnumerations apiEnumerations;
    private String inputAllowedNationalities;
    private final List<String> countryList;
    private final List<String> ukCountryList;

    public OfficerAppointmentValidator(final Logger logger,
                                       final CompanyProfileService companyProfileService,
                                       final ApiEnumerations apiEnumerations,
                                       final String inputAllowedNationalities,
            List<String> countryList, List<String> ukCountryList) {
        super(logger, companyProfileService, apiEnumerations);
        this.logger = logger;
        this.apiEnumerations = getApiEnumerations();
        this.inputAllowedNationalities = inputAllowedNationalities;
        this.ukCountryList = ukCountryList;
        this.countryList = countryList;
    }

    /**
     * Main validation method to fetch the required data and validate the request. This should be the point of call when terminating an officer.
     * @param request The servlet request used in logging
     * @param dto Data Object containing details of the termination
     * @param transaction the transaction for this termination
     * @param passthroughHeader ERIC pass through header for authorisation
     * @return An object containing a list of any validation errors that have been raised
     */
    @Override
    public ApiErrors validate(HttpServletRequest request, OfficerFilingDto dto, Transaction transaction, String passthroughHeader) {
            logger.debugContext(transaction.getId(), "Beginning officer appointment validation", new LogHelper.Builder(transaction)
                .withRequest(request)
                .build());
        final List<ApiError> errorList = new ArrayList<>();

        // Validate required dto and transaction fields
        validateRequiredDtoFields(request, errorList, dto);
        validateRequiredTransactionFields(request, errorList, transaction);
        validateOptionalDtoFields(request, errorList, dto);

        // Retrieve data objects required for the validation process
        final Optional<CompanyProfileApi> companyProfile = getCompanyProfile(request, errorList, transaction, passthroughHeader);
        if ( companyProfile.isEmpty()) {
            return new ApiErrors(errorList);
        }

        // Perform validation
        validateCompanyNotDissolved(request, errorList, companyProfile.get());
        validateAllowedCompanyType(request, errorList, companyProfile.get());
        validateAppointmentDateAfterIncorporationDate(request, errorList, dto, companyProfile.get());

        return new ApiErrors(errorList);
    }

    @Override
    public void validateRequiredDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        validateFirstName(request, errorList, dto);
        validateLastName(request, errorList, dto);
        validateDateOfBirth(request, errorList, dto);
        validateNationality1(request, errorList, dto);
        validateNationality2(request, errorList, dto);
        validateNationality3(request, errorList, dto);
        validateNationalityLength(request, errorList, dto);
        validateRequiredResidentialAddressFields(request, errorList, dto);
        if (!Boolean.TRUE.equals(dto.getIsServiceAddressSameAsRegisteredOfficeAddress()) || dto.getServiceAddress() != null) {
            validateCorrespondenceAddressFields(request, errorList, dto);
        }
        validateAppointmentDate(request, errorList, dto);
        validateProtectedDetails(request, errorList, dto);
        validateConsentToAct(request, errorList, dto);
    }

    @Override
    public void validateOptionalDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        validateTitle(request, errorList, dto);
        validateMiddleNames(request, errorList, dto);
        validateFormerNames(request, errorList, dto);
        validateOccupation(request, errorList, dto);
        if(dto.getResidentialAddress() != null){
            validateOptionalResidentialAddressFields(request, errorList, dto);
        }
        validateAddressesMultipleFlags(request, errorList, dto);
    }

    private void validateFirstName(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        if (dto.getFirstName() == null || dto.getFirstName().isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_BLANK));
        }
        else{
            if(!validateDtoFieldLength(dto.getFirstName(), 50)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_LENGTH));
            }
            if(!isValidCharacters(dto.getFirstName())){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.FIRST_NAME_CHARACTERS));
            }
        }
    }

    private void validateLastName(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        if (dto.getLastName() == null || dto.getLastName().isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.LAST_NAME_BLANK));
        }
        else{
            if(!validateDtoFieldLength(dto.getLastName(), 160)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.LAST_NAME_LENGTH));
            }
            if(!isValidCharacters(dto.getLastName())){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.LAST_NAME_CHARACTERS));
            }
        }
    }

    private void validateDateOfBirth(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        if (dto.getDateOfBirth() == null ) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DATE_OF_BIRTH_BLANK));
        }
        else{
            var officerDateOfBirth = dto.getDateOfBirth();
            var currentDate = LocalDate.now();
            var age = Period.between(officerDateOfBirth, currentDate).getYears();
            if(age >= 110){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DATE_OF_BIRTH_OVERAGE));
            }
            else if(age < 16){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DATE_OF_BIRTH_UNDERAGE));
            }
        }
    }

    public void validateAppointmentDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getAppointedOn() == null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.APPOINTMENT_DATE_MISSING));
        } else {
            validateAppointmentPastOrPresent(request, errorList, dto);
            validateDirectorAgeAtAppointment(request, errorList, dto);
        }
    }

    public void validateAppointmentPastOrPresent(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getAppointedOn().isAfter(LocalDate.now())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.APPOINTMENT_DATE_IN_PAST));
        }
    }

    public void validateDirectorAgeAtAppointment(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        if (dto.getDateOfBirth() == null ) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DATE_OF_BIRTH_BLANK));
        }
        else{
            var age = Period.between(dto.getDateOfBirth(), dto.getAppointedOn()).getYears();
            if(age >= 110){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DATE_OF_BIRTH_OVERAGE));
            }
            else if(age < 16){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.APPOINTMENT_DATE_UNDERAGE));
            }
        }
    }

    private void validateProtectedDetails(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        if(dto.getDirectorAppliedToProtectDetails() == null){
            createValidationError(request, errorList,
                    apiEnumerations.getValidation(ValidationEnum.PROTECTED_DETAILS_MISSING));
        }
    }

    private void validateConsentToAct(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        if(dto.getConsentToAct() == null){
            createValidationError(request, errorList,
                    apiEnumerations.getValidation(ValidationEnum.CONSENT_TO_ACT_MISSING));
        }
        else if(!dto.getConsentToAct()){
            createValidationError(request, errorList,
                    apiEnumerations.getValidation(ValidationEnum.CONSENT_TO_ACT_FALSE));
        }
    }

    private void validateTitle(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        if(dto.getTitle() != null){
            if (!validateDtoFieldLength(dto.getTitle(), 50)) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.TITLE_LENGTH));
            }
            if (!isValidCharacters(dto.getTitle())) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.TITLE_CHARACTERS));
            }
        }
    }

    private void validateMiddleNames(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        if(dto.getMiddleNames() != null){
            if (!validateDtoFieldLength(dto.getMiddleNames(), 50)) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.MIDDLE_NAME_LENGTH));
            }
            if (!isValidCharacters(dto.getMiddleNames())) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.MIDDLE_NAME_CHARACTERS));
            }
        }
    }

    private void validateFormerNames(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if(dto.getFormerNames() != null){
            if(!validateFormerNamesLength(dto.getFormerNames())) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.FORMER_NAMES_LENGTH));
            }
            if(!isValidCharacters(dto.getFormerNames())) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.FORMER_NAMES_CHARACTERS));
            }
        }
    }

    @Override
    public void validateRequiredTransactionFields(HttpServletRequest request, List<ApiError> errorList, Transaction transaction) {
        if (transaction.getCompanyNumber() == null || transaction.getCompanyNumber().isBlank()) {
            createValidationError(request, errorList, "The company number cannot be null or blank");
        }
    }

    @Override
    public void validateCompanyNotDissolved(HttpServletRequest request, List<ApiError> errorList, CompanyProfileApi companyProfile) {
        if (companyProfile.getCompanyStatus() == null) {
            logger.errorRequest(request, "null data was found in the Company Profile API within the Company Status field");
            return;
        }
        if (Objects.equals(companyProfile.getCompanyStatus(), "dissolved") || companyProfile.getDateOfCessation() != null) {
            createValidationError(request, errorList, getApiEnumerations().getValidation(ValidationEnum.COMPANY_DISSOLVED));
        }
    }

    private void validateOccupation(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        if(dto.getOccupation() != null){
            if (!validateDtoFieldLength(dto.getOccupation(), 100)) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.OCCUPATION_LENGTH));
            }
            if (!isValidCharacters(dto.getOccupation())) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.OCCUPATION_CHARACTERS));
            }
        }
    }

    private void validateNationalityLength(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (!StringUtils.isEmpty(dto.getNationality3()) && !StringUtils.isEmpty(
                dto.getNationality2()) && !StringUtils.isEmpty(dto.getNationality1())) {
            if (!validateDtoFieldLength(dto.getNationality1() + "," + dto.getNationality2() + ","
                    + dto.getNationality3(), 50)) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH48));
            }
        } else if (!StringUtils.isEmpty(dto.getNationality2()) && !StringUtils.isEmpty(
                dto.getNationality1())) {
            if (!validateDtoFieldLength(dto.getNationality1() + "," + dto.getNationality2(),
                    50)) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH49));
            }
        } else if (!StringUtils.isEmpty(dto.getNationality1()) && !validateDtoFieldLength(dto.getNationality1(), 50)) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.NATIONALITY_LENGTH));
        }
    }

    private void validateNationality1(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        if (dto.getNationality1() == null || dto.getNationality1().isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.NATIONALITY_BLANK));
        }
            else {
                if (!isValidNationalityFromAllowedList(dto.getNationality1(),
                        inputAllowedNationalities)) {
                    createValidationError(request, errorList,
                            apiEnumerations.getValidation(ValidationEnum.INVALID_NATIONALITY));
                }
            }
        }

        private void validateNationality2(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
            if (!StringUtils.isEmpty(dto.getNationality2())) {
                if (dto.getNationality2().equalsIgnoreCase(dto.getNationality1())) {
                    createValidationError(request, errorList,
                            apiEnumerations.getValidation(ValidationEnum.DUPLICATE_NATIONALITY2));
                }
                if (!isValidNationalityFromAllowedList(dto.getNationality2(),
                        inputAllowedNationalities)) {
                    createValidationError(request, errorList,
                            apiEnumerations.getValidation(ValidationEnum.INVALID_NATIONALITY));
                }
            }
        }
    private void validateNationality3(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        if (!StringUtils.isEmpty(dto.getNationality3())) {

            if (dto.getNationality3().equalsIgnoreCase(dto.getNationality1())) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.DUPLICATE_NATIONALITY3));
            }
            if (dto.getNationality3().equalsIgnoreCase(dto.getNationality2())) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.DUPLICATE_NATIONALITY3));
            }
            if(!isValidNationalityFromAllowedList(dto.getNationality3(), inputAllowedNationalities)) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.INVALID_NATIONALITY));
            }
        }
    }

    private void validateRequiredResidentialAddressFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        if(dto.getResidentialAddress() != null){
            validateResidentialPremises(request, errorList, dto.getResidentialAddress().getPremises());
            validateResidentialAddressLine1(request, errorList, dto.getResidentialAddress().getAddressLine1());
            validateResidentialLocality(request, errorList, dto.getResidentialAddress().getLocality());
            validateResidentialCountry(request, errorList, dto.getResidentialAddress().getCountry());
            validateResidentialPostalCode(request, errorList, dto.getResidentialAddress().getPostalCode(), dto.getResidentialAddress().getCountry());
        }
        else{
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_BLANK));
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_BLANK));
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_BLANK));
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_BLANK));
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_BLANK));
        }
    }

    private void validateOptionalResidentialAddressFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        validateResidentialAddressLine2(request, errorList, dto.getResidentialAddress().getAddressLine2());
        validateResidentialRegion(request, errorList, dto.getResidentialAddress().getRegion());
    }

    private void validateCorrespondenceAddressFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        if(dto.getServiceAddress() != null){
            validateCorrespondencePremises(request, errorList, dto.getServiceAddress().getPremises());
            validateCorrespondenceAddressLine1(request, errorList, dto.getServiceAddress().getAddressLine1());
            validateCorrespondenceAddressLine2(request, errorList, dto.getServiceAddress().getAddressLine2());
            validateCorrespondenceRegion(request, errorList, dto.getServiceAddress().getRegion());
            validateCorrespondenceLocality(request, errorList, dto.getServiceAddress().getLocality());
            validateCorrespondenceCountry(request, errorList, dto.getServiceAddress().getCountry());
            validateCorrespondencePostalCode(request, errorList, dto.getServiceAddress().getPostalCode(), dto.getServiceAddress().getCountry());
        }
        else{
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_PREMISES_BLANK));
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_ONE_BLANK));
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_LOCALITY_BLANK));
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_BLANK));
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_BLANK));
        }
    }

    private void validateAddressesMultipleFlags(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if(Boolean.TRUE.equals(dto.getIsServiceAddressSameAsHomeAddress()) && Boolean.TRUE.equals(dto.getIsServiceAddressSameAsRegisteredOfficeAddress())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.ADDRESS_LINKS_MULTIPLE_FLAGS));
        }
    }

    private void validateResidentialPremises(HttpServletRequest request, List<ApiError> errorList, String premises){
        if (premises == null || premises.isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_BLANK));
        }
        else{
            if(!validateDtoFieldLength(premises, 200)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_LENGTH));
            }
            if(!isValidCharacters(premises)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_PREMISES_CHARACTERS));
            }
        }
    }

    private void validateCorrespondencePremises(HttpServletRequest request, List<ApiError> errorList, String premises){
        if (premises == null || premises.isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_PREMISES_BLANK));
        }
        else{
            if(!validateDtoFieldLength(premises, 200)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_PREMISES_LENGTH));
            }
            if(!isValidCharacters(premises)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_PREMISES_CHARACTERS));
            }
        }
    }

    private void validateResidentialAddressLine1(HttpServletRequest request, List<ApiError> errorList, String addressLineOne){
        if (addressLineOne == null ||addressLineOne.isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_BLANK));
        }
        else{
            if(!validateDtoFieldLength(addressLineOne, 50)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_LENGTH));
            }
            if(!isValidCharacters(addressLineOne)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_ONE_CHARACTERS));
            }
        }
    }

    private void validateCorrespondenceAddressLine1(HttpServletRequest request, List<ApiError> errorList, String addressLineOne){
        if (addressLineOne == null ||addressLineOne.isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_ONE_BLANK));
        }
        else{
            if(!validateDtoFieldLength(addressLineOne, 50)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_ONE_LENGTH));
            }
            if(!isValidCharacters(addressLineOne)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_ONE_CHARACTERS));
            }
        }
    }

    private void validateResidentialAddressLine2(HttpServletRequest request, List<ApiError> errorList, String addressLineTwo){
        if (addressLineTwo != null && !addressLineTwo.isBlank()) {
            if(!validateDtoFieldLength(addressLineTwo, 50)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_TWO_LENGTH));
            }
            if(!isValidCharacters(addressLineTwo)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_ADDRESS_LINE_TWO_CHARACTERS));
            }
        }
    }

    private void validateCorrespondenceAddressLine2(HttpServletRequest request, List<ApiError> errorList, String addressLineTwo){
        if (addressLineTwo != null && !addressLineTwo.isBlank()) {
            if(!validateDtoFieldLength(addressLineTwo, 50)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_TWO_LENGTH));
            }
            if(!isValidCharacters(addressLineTwo)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_ADDRESS_LINE_TWO_CHARACTERS));
            }
        }
    }

    private void validateResidentialRegion(HttpServletRequest request, List<ApiError> errorList, String region){
        if (region != null && !region.isBlank()) {
            if(!validateDtoFieldLength(region, 50)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_REGION_LENGTH));
            }
            if(!isValidCharacters(region)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_REGION_CHARACTERS));
            }
        }
    }

    private void validateCorrespondenceRegion(HttpServletRequest request, List<ApiError> errorList, String region){
        if (region != null && !region.isBlank()) {
            if(!validateDtoFieldLength(region, 50)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_REGION_LENGTH));
            }
            if(!isValidCharacters(region)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_REGION_CHARACTERS));
            }
        }
    }

    private void validateResidentialLocality(HttpServletRequest request, List<ApiError> errorList, String locality){
        if (locality == null || locality.isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_BLANK));
        }
        else{
            if(!validateDtoFieldLength(locality, 50)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_LENGTH));
            }
            if(!isValidCharacters(locality)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_LOCALITY_CHARACTERS));
            }
        }
    }

    private void validateCorrespondenceLocality(HttpServletRequest request, List<ApiError> errorList, String locality){
        if (locality == null || locality.isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_LOCALITY_BLANK));
        }
        else{
            if(!validateDtoFieldLength(locality, 50)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_LOCALITY_LENGTH));
            }
            if(!isValidCharacters(locality)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_LOCALITY_CHARACTERS));
            }
        }
    }

    private void validateResidentialCountry(HttpServletRequest request, List<ApiError> errorList, String country){
        if (country == null || country.isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_BLANK));
        }
        else{
            if(!countryList.stream().map(String::toLowerCase).collect(Collectors.toList()).contains(country.toLowerCase())){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_INVALID));
            }
            if(!validateDtoFieldLength(country, 50)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_LENGTH));
            }
            if(!isValidCharacters(country)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_COUNTRY_CHARACTERS));
            }
        }
    }

    private void validateCorrespondenceCountry(HttpServletRequest request, List<ApiError> errorList, String country){
        if (country == null || country.isBlank()) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_BLANK));
        }
        else{
            if(!countryList.stream().map(String::toLowerCase).collect(Collectors.toList()).contains(country.toLowerCase())){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_INVALID));
            }
            if(!validateDtoFieldLength(country, 50)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_LENGTH));
            }
            if(!isValidCharacters(country)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_COUNTRY_CHARACTERS));
            }
        }
    }

    private void validateResidentialPostalCode(HttpServletRequest request, List<ApiError> errorList, String postalCode, String country) {
        if (postalCode != null && !postalCode.isBlank()){
            if (!isValidCharacters(postalCode)) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_CHARACTERS));
            }
            if (!validateDtoFieldLength(postalCode, 20)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_LENGTH));
            }
        }
        if ((country == null || country.isBlank() || isUkCountry(country)) && (postalCode == null || postalCode.isBlank())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTAL_CODE_BLANK));
        } else if (isUkCountry(country) && !isValidCharactersForUkPostcode(postalCode)) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.RESIDENTIAL_POSTCODE_UK_INVALID));
        }
    }

    private void validateCorrespondencePostalCode(HttpServletRequest request, List<ApiError> errorList,String postalCode, String country) {
        if (postalCode != null && !postalCode.isBlank()){
            if (!isValidCharacters(postalCode)) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_CHARACTERS));
            }
            if (!validateDtoFieldLength(postalCode, 20)){
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_LENGTH));
            }
        }
        if ((country == null || country.isBlank() || isUkCountry(country)) && (postalCode == null || postalCode.isBlank())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTAL_CODE_BLANK));
        } else if (isUkCountry(country) && !isValidCharactersForUkPostcode(postalCode)) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CORRESPONDENCE_POSTCODE_UK_INVALID));
        }
    }

    public void validateAppointmentDateAfterIncorporationDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, CompanyProfileApi companyProfile) {
        if (companyProfile.getDateOfCreation() == null) {
            logger.errorRequest(request, "null data was found in the Company Profile API within the Date Of Creation field");
            return;
        }

        if (dto.getAppointedOn() == null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.APPOINTMENT_DATE_MISSING));
        } else if (dto.getAppointedOn().isBefore(companyProfile.getDateOfCreation())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.APPOINTMENT_DATE_AFTER_INCORPORATION_DATE));
        }
    }

    private boolean isUkCountry(String country) {
        return country != null && !country.isBlank() && ukCountryList.stream().anyMatch(country::equalsIgnoreCase);
    }

}
