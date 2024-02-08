package uk.gov.companieshouse.officerfiling.api.validation;

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
import uk.gov.companieshouse.officerfiling.api.validation.error.CorrespondenceAddressErrorProvider;
import uk.gov.companieshouse.officerfiling.api.validation.error.ResidentialAddressErrorProvider;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides all validation that should be carried out when an officer is appointed. Fetches all data necessary to complete
 * the validation and generates a list of errors that can be sent back to the caller.
 */
public class OfficerAppointmentValidator extends OfficerValidator {

    private static final Integer AGE_16 = 16;
    private static final Integer AGE_110 = 110;
    private final Logger logger;
    private final AddressValidator addressValidator;

    public OfficerAppointmentValidator(final Logger logger,
                                       final CompanyProfileService companyProfileService,
                                       final ApiEnumerations apiEnumerations,
                                       final String inputAllowedNationalities,
                                       final List<String> countryList,
                                       final List<String> ukCountryList) {
        super(logger, companyProfileService, inputAllowedNationalities, apiEnumerations);
        this.logger = logger;
        this.addressValidator = new AddressValidator(logger, companyProfileService, inputAllowedNationalities, apiEnumerations, countryList, ukCountryList);
    }

    /**
     * Main validation method to fetch the required data and validate the request. This should be the point of call when appointing an officer.
     *
     * @param request           The servlet request used in logging
     * @param dto               Data Object containing details of the appointment
     * @param transaction       the transaction for this appointment
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
        validateAddressSections(request, errorList, dto);

        // Retrieve data objects required for the validation process
        final Optional<CompanyProfileApi> companyProfile = getCompanyProfile(request, errorList, transaction, passthroughHeader);
        if (companyProfile.isEmpty()) {
            return new ApiErrors(errorList);
        }

        // Perform validation
        validateCompanyNotDissolved(request, errorList, companyProfile.get());
        validateAllowedCompanyType(request, errorList, companyProfile.get());
        validateAppointmentDateBeforeIncorporationDate(request, errorList, dto, companyProfile.get());

        return new ApiErrors(errorList);
    }

    private void validateRequiredDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        validateFirstName(request, errorList, dto);
        validateLastName(request, errorList, dto);
        validateDateOfBirth(request, errorList, dto);
        validateNationality1(request, errorList, dto);
        validateNationality2(request, errorList, dto);
        validateNationality3(request, errorList, dto);
        validateNationalityLength(request, errorList, dto);
        validateAppointmentDate(request, errorList, dto);
        validateProtectedDetails(request, errorList, dto);
        validateConsentToAct(request, errorList, dto);
    }

    private void validateOptionalDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        validateTitle(request, errorList, dto);
        validateMiddleNames(request, errorList, dto);
        validateFormerNames(request, errorList, dto);
        validateOccupation(request, errorList, dto);
        validateAddressesMultipleFlags(request, errorList, dto);
    }

    private void validateAddressSections(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (!Boolean.TRUE.equals(dto.getIsHomeAddressSameAsServiceAddress())) {
            addressValidator.validate(new ResidentialAddressErrorProvider(apiEnumerations), request, errorList, dto.getResidentialAddress());
        }
        if (!Boolean.TRUE.equals(dto.getIsServiceAddressSameAsRegisteredOfficeAddress())) {
            addressValidator.validate(new CorrespondenceAddressErrorProvider(apiEnumerations), request, errorList, dto.getServiceAddress());
        }
    }

    private void validateDateOfBirth(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getDateOfBirth() == null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DATE_OF_BIRTH_BLANK));
        } else {
            var officerDateOfBirth = dto.getDateOfBirth();
            var currentDate = LocalDate.now();
            var age = Period.between(officerDateOfBirth, currentDate).getYears();
            if (age >= AGE_110) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DATE_OF_BIRTH_OVERAGE));
            } else if (age < AGE_16) {
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

    public void validateDirectorAgeAtAppointment(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getDateOfBirth() == null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DATE_OF_BIRTH_BLANK));
        } else {
            var age = Period.between(dto.getDateOfBirth(), dto.getAppointedOn()).getYears();
            if (age >= AGE_110) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.DATE_OF_BIRTH_OVERAGE));
            } else if (age < AGE_16) {
                createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.APPOINTMENT_DATE_UNDERAGE));
            }
        }
    }

    private void validateProtectedDetails(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getDirectorAppliedToProtectDetails() == null) {
            createValidationError(request, errorList,
                    apiEnumerations.getValidation(ValidationEnum.PROTECTED_DETAILS_MISSING));
        }
    }

    private void validateConsentToAct(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getConsentToAct() == null) {
            createValidationError(request, errorList,
                    apiEnumerations.getValidation(ValidationEnum.CONSENT_TO_ACT_MISSING));
        } else if (!Boolean.TRUE.equals(dto.getConsentToAct())) {
            createValidationError(request, errorList,
                    apiEnumerations.getValidation(ValidationEnum.CONSENT_TO_ACT_FALSE));
        }
    }

    private void validateFormerNames(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getFormerNames() != null) {
            if (!validateFormerNamesLength(dto.getFormerNames())) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.FORMER_NAMES_LENGTH));
            }
            if (!isValidCharacters(dto.getFormerNames())) {
                createValidationError(request, errorList,
                        apiEnumerations.getValidation(ValidationEnum.FORMER_NAMES_CHARACTERS));
            }
        }
    }

    private void validateAddressesMultipleFlags(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (Boolean.TRUE.equals(dto.getIsHomeAddressSameAsServiceAddress()) && Boolean.TRUE.equals(dto.getIsServiceAddressSameAsRegisteredOfficeAddress())) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.ADDRESS_LINKS_MULTIPLE_FLAGS));
        }
    }

    public void validateAppointmentDateBeforeIncorporationDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto, CompanyProfileApi companyProfile) {
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

}