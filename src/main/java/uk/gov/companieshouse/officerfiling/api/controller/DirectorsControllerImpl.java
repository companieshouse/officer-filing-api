package uk.gov.companieshouse.officerfiling.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.exception.FeatureNotEnabledException;
import uk.gov.companieshouse.officerfiling.api.exception.OfficerServiceException;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper.Builder;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;

import static uk.gov.companieshouse.officerfiling.api.utils.Constants.TRANSACTION_ID_KEY;

@RestController
public class DirectorsControllerImpl implements DirectorsController {

    private final OfficerService officerService;
    private final Logger logger;
    private final CompanyAppointmentService companyAppointmentService;
    private final OfficerFilingService officerFilingService;
    @Value("${FEATURE_FLAG_ENABLE_TM01:true}")
    private boolean isTm01Enabled;

    public DirectorsControllerImpl(final OfficerService officerService,
            final CompanyAppointmentService companyAppointmentService, OfficerFilingService officerFilingService,
            final Logger logger) {
        this.officerService = officerService;
        this.companyAppointmentService = companyAppointmentService;
        this.officerFilingService = officerFilingService;
        this.logger = logger;
    }

    @Override
    @ResponseBody
    @GetMapping(value = "/transactions/{transactionId}/officers/active-directors-details", produces = {"application/json"})
    public ResponseEntity<Object> getListActiveDirectorsDetails(
        @RequestAttribute("transaction") Transaction transaction,
        final HttpServletRequest request) {

        if(!isTm01Enabled){
            throw new FeatureNotEnabledException();
        }

        try {
            logger.debugContext(transaction.getId(), "Calling service to retrieve the active officers details", new Builder(transaction)
                .withRequest(request)
                .build());

            final var passthroughHeader =
                request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());

            final var directorsDetails = officerService.getListOfActiveDirectorsDetails(request, transaction.getId(),
                transaction.getCompanyNumber(), passthroughHeader);

            return ResponseEntity.status(HttpStatus.OK).body(directorsDetails);
        } catch (OfficerServiceException e) {
            logger.debugContext(transaction.getId(), "Error retrieving active officers details", new Builder(transaction)
                .withRequest(request)
                .build());
            //If the exception contains an empty json, this means the officers could not be found for the company
            if(e.getCause().getMessage().endsWith("{}")){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @ResponseBody
    @GetMapping(value = "/transactions/{transactionId}/officers/{filingId}/tm01-check-answers-directors-details", produces = {"application/json"})
    public ResponseEntity<Object> getRemoveCheckAnswersDirectorDetails(
            @RequestAttribute("transaction") Transaction transaction,
            @PathVariable("filingId") String filingId, final HttpServletRequest request) {

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transaction.getId());

        try {
            logger.debugContext(transaction.getId(), "Calling service to retrieve the check answers details for TM01", new Builder(transaction)
                    .withRequest(request)
                    .build());

            final var passthroughHeader =
                    request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());

            final var officerFiling = officerFilingService.get(filingId, transaction.getId());
            Instant resignedOn = null;

            if(officerFiling.isPresent() && officerFiling.get().getOfficerFilingData().getResignedOn() != null){
                resignedOn = officerFiling.get().getOfficerFilingData().getResignedOn();
            }
            else{
                throw new OfficerServiceException("Could not find resigned on date for " + filingId);
            }
            final var directorsDetails = companyAppointmentService.getCompanyAppointment(transaction.getId(),
                    transaction.getCompanyNumber(), officerFiling.get().getOfficerFilingData().getReferenceAppointmentId(), passthroughHeader);
            directorsDetails.setResignedOn(LocalDate.ofInstant(resignedOn,
                    ZoneId.systemDefault()));

            return ResponseEntity.status(HttpStatus.OK).body(directorsDetails);
        } catch (OfficerServiceException e) {
            logger.debugContext(transaction.getId(), "Error retrieving TM01 check your answers details", new Builder(transaction)
                    .withRequest(request)
                    .build());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}