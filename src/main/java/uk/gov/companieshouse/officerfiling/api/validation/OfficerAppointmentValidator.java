package uk.gov.companieshouse.officerfiling.api.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
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

/**
 * Provides all validation that should be carried out when an officer is terminated. Fetches all data necessary to complete
 * the validation and generates a list of errors that can be sent back to the caller.
 */
public class OfficerAppointmentValidator extends OfficerValidator {

    private Logger logger;
    private ApiEnumerations apiEnumerations;

    public OfficerAppointmentValidator(final Logger logger,
                                       final CompanyProfileService companyProfileService,
                                       final ApiEnumerations apiEnumerations) {
        super(logger, companyProfileService, apiEnumerations);
        this.logger = logger;
        this.apiEnumerations = getApiEnumerations();
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
            logger.debugContext(transaction.getId(), "Beginning officer appointmnet validation", new LogHelper.Builder(transaction)
                .withRequest(request)
                .build());
        final List<ApiError> errorList = new ArrayList<>();

        // Validate required dto and transaction fields and fail early
        validateRequiredDtoFields(request, errorList, dto);
        validateRequiredTransactionFields(request, errorList, transaction);
        validateOptionalDtoFields(request, errorList, dto);
        if (!errorList.isEmpty()) {
            return new ApiErrors(errorList);
        }

        // Retrieve data objects required for the validation process
        final Optional<CompanyProfileApi> companyProfile = getCompanyProfile(request, errorList, transaction, passthroughHeader);
        if ( companyProfile.isEmpty()) {
            return new ApiErrors(errorList);
        }

        // Perform validation
        validateCompanyNotDissolved(request, errorList, companyProfile.get());
        validateAllowedCompanyType(request, errorList, companyProfile.get());

        return new ApiErrors(errorList);
    }

    @Override
    public void validateRequiredDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
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

    @Override
    public void validateOptionalDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto){
        validateTitle(request, errorList, dto);
        validateMiddleNames(request, errorList, dto);
        validateFormerNames(request, errorList, dto);
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
            for(var formerName : dto.getFormerNames()){
                if(!isValidCharacters(formerName.getSurname()) || !isValidCharacters(formerName.getForenames())){
                    createValidationError(request, errorList,
                            apiEnumerations.getValidation(ValidationEnum.FORMER_NAMES_CHARACTERS));
                    break;
                }
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

}
