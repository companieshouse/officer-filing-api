package uk.gov.companieshouse.officerfiling.api.controller;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.error.ApiErrors;
import uk.gov.companieshouse.officerfiling.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.mapper.ErrorMapper;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;
import uk.gov.companieshouse.officerfiling.api.validation.OfficerTerminationValidator;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@RestController
@RequestMapping("/transactions/{transactionId}/officers")
public class ValidationStatusControllerImpl implements ValidationStatusController {
    private final OfficerFilingService officerFilingService;
    private final Logger logger;
    private final TransactionService transactionService;
    private final CompanyProfileService companyProfileService;
    private final CompanyAppointmentService companyAppointmentService;
    private final OfficerFilingMapper officerFilingMapper;
    private final ErrorMapper errorMapper;

    public ValidationStatusControllerImpl(OfficerFilingService officerFilingService, Logger logger,
        TransactionService transactionService, CompanyProfileService companyProfileService,
        CompanyAppointmentService companyAppointmentService, OfficerFilingMapper officerFilingMapper,
        ErrorMapper errorMapper) {
        this.officerFilingService = officerFilingService;
        this.logger = logger;
        this.transactionService = transactionService;
        this.companyProfileService = companyProfileService;
        this.companyAppointmentService = companyAppointmentService;
        this.officerFilingMapper = officerFilingMapper;
        this.errorMapper = errorMapper;
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

        logger.debugContext(transaction.getId(), "GET validation request", new LogHelper.Builder(transaction)
                .withFilingId(filingResourceId)
                .withRequest(request)
                .build());

        final var passthroughHeader =
            request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());

        Optional<OfficerFiling> maybeOfficerFiling = officerFilingService.get(filingResourceId, transaction.getId());

        return maybeOfficerFiling.map(officerFiling -> isValid(request, officerFilingMapper.map(officerFiling),
                transaction, passthroughHeader))
            .orElseThrow(() -> new ResourceNotFoundException(
                "Filing resource not found: " + filingResourceId));
    }

    private ValidationStatusResponse isValid(HttpServletRequest request, OfficerFilingDto officerFiling,
        Transaction transaction, String passthroughHeader) {
        var validationStatus = new ValidationStatusResponse();

        final var validator = new OfficerTerminationValidator(logger, transactionService,
            companyProfileService, companyAppointmentService);
        final ApiErrors validationErrors  = validator.validate(request, officerFiling, transaction, passthroughHeader);

        if(validationErrors.hasErrors()) {
            validationStatus.setValidationStatusError(errorMapper.map(
                validationErrors.getErrors()));
            return validationStatus;
        }

        validationStatus.setValid(true);
        return validationStatus;
    }
}
