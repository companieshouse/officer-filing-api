package uk.gov.companieshouse.officerfiling.api.validation;

import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.enumerations.ValidationEnum;
import uk.gov.companieshouse.officerfiling.api.error.ApiErrors;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides all validation that should be carried out when an officer is updated. Fetches all data necessary to complete
 * the validation and generates a list of errors that can be sent back to the caller.
 */
public class OfficerUpdateValidator extends OfficerValidator {

    private final Logger logger;
    private ApiEnumerations apiEnumerations;

    public OfficerUpdateValidator(final Logger logger,
                                  final CompanyProfileService companyProfileService,
                                  final ApiEnumerations apiEnumerations) {
        super(logger, companyProfileService, apiEnumerations);
        this.logger = logger;
        this.apiEnumerations = apiEnumerations;
    }

    /**
     * Main validation method to fetch the required data and validate the request. This should be the point of call when updating an officer.
     * @param request The servlet request used in logging
     * @param dto Data Object containing details of the update
     * @param transaction the transaction for this update
     * @param passthroughHeader ERIC pass through header for authorisation
     * @return An object containing a list of any validation errors that have been raised
     */
    @Override
    public ApiErrors validate(HttpServletRequest request, OfficerFilingDto dto, Transaction transaction, String passthroughHeader) {
            logger.debugContext(transaction.getId(), "Beginning officer update validation", new LogHelper.Builder(transaction)
                .withRequest(request)
                .build());
        final List<ApiError> errorList = new ArrayList<>();

        validateRequiredDtoFields(request, errorList, dto);

        return new ApiErrors(errorList);
    }

    @Override
    public void validateRequiredDtoFields(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        validateChangeDate(request, errorList, dto);
    }

    public void validateChangeDate(HttpServletRequest request, List<ApiError> errorList, OfficerFilingDto dto) {
        if (dto.getChangeDate() == null) {
            createValidationError(request, errorList, apiEnumerations.getValidation(ValidationEnum.CHANGE_DATE_MISSING));
        } else {
            //validateChangePastOrPresent(request, errorList, dto);
            //validate...(request, errorList, dto);
        }
    }

}
