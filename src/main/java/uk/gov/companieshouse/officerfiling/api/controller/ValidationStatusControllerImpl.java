package uk.gov.companieshouse.officerfiling.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.enumerations.ApiEnumerations;
import uk.gov.companieshouse.officerfiling.api.error.ApiErrors;
import uk.gov.companieshouse.officerfiling.api.exception.FeatureNotEnabledException;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.mapper.ErrorMapper;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;
import uk.gov.companieshouse.officerfiling.api.validation.OfficerAppointmentValidator;
import uk.gov.companieshouse.officerfiling.api.validation.OfficerTerminationValidator;
import uk.gov.companieshouse.officerfiling.api.validation.OfficerUpdateValidator;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/transactions/{transactionId}/officers")
public class ValidationStatusControllerImpl implements ValidationStatusController {
    private final OfficerFilingService officerFilingService;
    private final Logger logger;
    private final CompanyProfileService companyProfileService;
    private final CompanyAppointmentService companyAppointmentService;
    private final OfficerFilingMapper officerFilingMapper;
    private final ErrorMapper errorMapper;
    private final ApiEnumerations apiEnumerations;
    @Value("${FEATURE_FLAG_ENABLE_TM01:true}")
    private boolean isTm01Enabled;
    @Value("${FEATURE_FLAG_ENABLE_AP01:true}")
    private boolean isAp01Enabled;
    @Value("${FEATURE_FLAG_ENABLE_CH01:false}")
    private boolean isCh01Enabled;
    @Value("${NATIONALITY_LIST}")
    public String inputAllowedNationalities;
    @Value("#{'${COUNTRY_LIST}'.split(';')}")
    private List<String> countryList;
    @Value("#{'${UK_COUNTRY_LIST}'.split(';')}")
    private List<String> ukCountryList;

    public ValidationStatusControllerImpl(OfficerFilingService officerFilingService, Logger logger,
            CompanyProfileService companyProfileService,
        CompanyAppointmentService companyAppointmentService, OfficerFilingMapper officerFilingMapper,
        ErrorMapper errorMapper, ApiEnumerations apiEnumerations) {
        this.officerFilingService = officerFilingService;
        this.logger = logger;
        this.companyProfileService = companyProfileService;
        this.companyAppointmentService = companyAppointmentService;
        this.officerFilingMapper = officerFilingMapper;
        this.errorMapper = errorMapper;
        this.apiEnumerations = apiEnumerations;
    }

    /**
     * Controller endpoint: Perform final validation checks.
     * Provisional behaviour: return TRUE response until details of requirements known.
     *
     * @param transaction        the Transaction
     * @param filingResourceId the Filing resource ID
     * @param request        the servlet request
     * @return ValidationResponse of TRUE (provisional)
     */
    @Override
    @ResponseBody
    @GetMapping(value = "/{filingResourceId}/validation_status", produces = {"application/json"})
    public ValidationStatusResponse validate(
        @RequestAttribute("transaction") Transaction transaction,
        @PathVariable("filingResourceId") final String filingResourceId,
        final HttpServletRequest request) {

        if(!isTm01Enabled){
            throw new FeatureNotEnabledException();
        }

        logger.debugContext(transaction.getId(), "GET validation status request", new LogHelper.Builder(transaction)
                .withFilingId(filingResourceId)
                .withRequest(request)
                .build());

        final var passthroughHeader = request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());
        final var officerFiling = officerFilingService.get(filingResourceId, transaction.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Filing resource not found: " + filingResourceId));

        final ApiErrors validationErrors = validate(request, officerFilingMapper.map(officerFiling), transaction, passthroughHeader);

        if (validationErrors.hasErrors()) {
            return new ValidationStatusResponse(errorMapper.map(validationErrors.getErrors()), false);
        }
        return new ValidationStatusResponse(null, true);
    }

    /**
     * Create the associated OfficerValidator object (TM01, AP01, or CH01) and validate using that object
     * @return All validation errors raised during the validation
     */
    private ApiErrors validate(HttpServletRequest request, OfficerFilingDto officerFiling, Transaction transaction, String passthroughHeader) {
        if (isTm01Enabled && officerFiling.getResignedOn() != null) {
            return new OfficerTerminationValidator(logger, companyProfileService, companyAppointmentService, apiEnumerations)
                    .validate(request, officerFiling, transaction, passthroughHeader);
        }
        if (isAp01Enabled && officerFiling.getReferenceEtag() == null) {
            return new OfficerAppointmentValidator(logger, companyProfileService, apiEnumerations, inputAllowedNationalities, countryList, ukCountryList)
                    .validate(request, officerFiling, transaction, passthroughHeader);
        }
        if (isCh01Enabled){
            return new OfficerUpdateValidator(logger, companyProfileService, apiEnumerations)
                    .validate(request, officerFiling, transaction, passthroughHeader);
        }

        return new ApiErrors();
    }
}
