package uk.gov.companieshouse.officerfiling.api.controller;

import java.io.File;
import java.time.Clock;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.error.InvalidFilingException;
import uk.gov.companieshouse.officerfiling.api.exception.FeatureNotEnabledException;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.Links;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.CompanyAppointmentService;
import uk.gov.companieshouse.officerfiling.api.service.CompanyProfileService;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper.Builder;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@RestController
@RequestMapping("/transactions/{transactionId}/officers")
public class OfficerFilingControllerImpl implements OfficerFilingController {
    public static final String VALIDATION_STATUS = "validation_status";
    public static final String RESOURCE = "resource";
    public static final String OFFICER_FILING = "officer-filing";
    public static final String OFFICER_FILING_NAME = "OfficerFiling";
    public static final String TRANSACTION = "transaction";
    public static final String FILING_RESOURCE_ID = "filingResourceId";
    private final TransactionService transactionService;
    private final OfficerFilingService officerFilingService;
    private final CompanyProfileService companyProfileService;
    private final CompanyAppointmentService companyAppointmentService;
    private final OfficerFilingMapper filingMapper;
    private final Clock clock;
    private final Logger logger;
    @Value("${FEATURE_FLAG_ENABLE_TM01:true}")
    private boolean isTm01Enabled;
    public OfficerFilingControllerImpl(final TransactionService transactionService,
                                       final OfficerFilingService officerFilingService,
                                       final CompanyProfileService companyProfileService,
                                       final CompanyAppointmentService companyAppointmentService,
                                       final OfficerFilingMapper filingMapper,
                                       final Clock clock,
                                       final Logger logger) {
        this.transactionService = transactionService;
        this.officerFilingService = officerFilingService;
        this.companyProfileService = companyProfileService;
        this.companyAppointmentService = companyAppointmentService;
        this.filingMapper = filingMapper;
        this.clock = clock;
        this.logger = logger;
    }

    /**
     * Create an Officer Filing.
     *
     * @param transaction the Transaction
     * @param dto           the request body payload DTO
     * @param bindingResult the MVC binding result (with any validation errors)
     * @param request       the servlet request
     * @return CREATED response containing the populated Filing resource
     */
    @Override
    @PostMapping(produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<Object> createFiling(@RequestAttribute(TRANSACTION) Transaction transaction,
            @RequestBody @Valid @NotNull final OfficerFilingDto dto,
            final BindingResult bindingResult, final HttpServletRequest request) {

        if(!isTm01Enabled){
            throw new FeatureNotEnabledException();
        }
        logger.debugContext(transaction.getId(), "Creating filing", new LogHelper.Builder(transaction)
                .withRequest(request)
                .build());

        if (bindingResult != null && bindingResult.hasErrors()) {
            throw new InvalidFilingException(bindingResult.getFieldErrors());
        }

        final var passthroughHeader =
                    request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());

        var entity = filingMapper.map(dto);

        // Reuse this filing ID if it exists as we can only have one per transaction
        //***********************************************************************************************
        // NB for the moment to get around an issue where a web user could go back and select a different
        // director, which would cause multiple resources in the transaction when only ONE is wanted.
        // More thought is required for later when we will do this within Confirmation statemnet
        // as that will need multiple resources in the same transaction.
        //***********************************************************************************************
        String preExistingFilingId = getExistingFilingId(transaction);
        if(preExistingFilingId != null){
            entity = OfficerFiling.builder(entity)
                    .id(preExistingFilingId)
                    .build();
        }
        final var saveData = saveFilingWithLinks(entity, transaction, request);
        final var links = saveData.getLeft();
        final var officerFiling = saveData.getRight();
        final var resourceMap = buildResourceMap(links);

        var updateDescription = false;
        if (!StringUtils.isBlank(dto.getDescription()) && !dto.getDescription().equals(transaction.getDescription())) {
            transaction.setDescription(dto.getDescription());
            updateDescription = true;
        }

        transaction.setResources(resourceMap);
        if(preExistingFilingId == null || updateDescription) {
            logger.debug("Update transaction" + (StringUtils.isBlank(transaction.getDescription()) ? "" :  ": " + transaction.getDescription()));
            transactionService.updateTransaction(transaction, passthroughHeader);
        }

        // Create response with filing
        return ResponseEntity.created(links.getSelf()).body(officerFiling);
    }

    /**
     * Patch an Officer Filing.
     *
     * @param transaction the Transaction
     * @param dto           the request body payload DTO
     * @param bindingResult the MVC binding result (with any validation errors)
     * @param request       the servlet request
     * @return CREATED response containing the populated Filing resource
     */
    @Override
    @PatchMapping(value = "/{filingResourceId}", produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<Object> patchFiling(
            @RequestAttribute(TRANSACTION) Transaction transaction,
            @RequestBody @Valid @NotNull final OfficerFilingDto dto,
            @PathVariable(FILING_RESOURCE_ID) final String filingResourceId,
            final BindingResult bindingResult, final HttpServletRequest request) {

        if(!isTm01Enabled){
            throw new FeatureNotEnabledException();
        }

        logger.debugContext(transaction.getId(), "Patching Filing", new Builder(transaction)
                .withRequest(request)
                .build());

        if (bindingResult != null && bindingResult.hasErrors()) {
            throw new InvalidFilingException(bindingResult.getFieldErrors());
        }

        final var passthroughHeader =
                request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());

        String transId = transaction.getId();
        // Get the current filing if it exists
        var officerFilingOptional = officerFilingService.get(filingResourceId, transId);
        OfficerFiling officerFiling;
        // If it does, then update it with the patch data
        if(officerFilingOptional.isPresent()){
            // Update the existing filing

            validateTransactionLinkedToFiling(transaction, filingResourceId);

            officerFiling = officerFilingOptional.get();
            officerFiling = officerFilingService.mergeFilings(officerFiling, filingMapper.map(dto), transaction);
        }
        else{
            // Else just create a new filing
            officerFiling = filingMapper.map(dto);
        }

        final var saveDetails = saveFilingWithLinks(officerFiling, transaction, request);
        final var links = saveDetails.getLeft();
        final var resourceMap = buildResourceMap(links);

        transaction.setResources(resourceMap);
        transactionService.updateTransaction(transaction, passthroughHeader);

        return ResponseEntity.ok(saveDetails.getRight());

    }

    /**
     * Retrieve Officer Filing submission for review by the user before completing the submission.
     *
     * @param transId        the Transaction ID
     * @param filingResource the Officer Filing ID
     * @return OK response containing Filing DTO resource
     */
    @Override
    @GetMapping(value = "/{filingResourceId}", produces = {"application/json"})
    public ResponseEntity<OfficerFilingDto> getFilingForReview(
            @RequestAttribute(TRANSACTION) Transaction transaction,
            @PathVariable(FILING_RESOURCE_ID) final String filingResourceId) {

        if(!isTm01Enabled){
            throw new FeatureNotEnabledException();
        }

        validateTransactionLinkedToFiling(transaction, filingResourceId);

        var maybeOfficerFiling = officerFilingService.get(filingResourceId, transaction.getId());

        var maybeDto = maybeOfficerFiling.map(filingMapper::map);

        return maybeDto.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound()
                        .build());
    }

    private Map<String, Resource> buildResourceMap(final Links links) {
        final Map<String, Resource> resourceMap = new HashMap<>();
        final var resource = new Resource();
        final var linksMap = new HashMap<>(
                Map.of(RESOURCE, links.getSelf().toString(), VALIDATION_STATUS,
                        links.getValidationStatus().toString()));

        resource.setKind(OFFICER_FILING);
        resource.setLinks(linksMap);
        resource.setUpdatedAt(clock.instant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        resourceMap.put(links.getSelf().toString(), resource);
        return resourceMap;
    }

    private ImmutablePair<Links,OfficerFiling> saveFilingWithLinks(final OfficerFiling entity, final Transaction transaction,
            final HttpServletRequest request) {
        final var now = clock.instant();
        var createNow = now;
        OfficerFiling entityWithCreatedUpdated;
        if(entity.getCreatedAt() != null){
            createNow = entity.getCreatedAt();
        }

        final var create = createNow;
        entityWithCreatedUpdated =
                OfficerFiling.builder(entity).createdAt(create).updatedAt(now).data(entity.getData())
                        .build();
        final var finalEntityWithCreatedUpdated = entityWithCreatedUpdated;
        final var saved = officerFilingService.save(finalEntityWithCreatedUpdated, transaction.getId());
        final var links = buildLinks(saved.getId(), request);

        final var updated = OfficerFiling.builder(saved).links(links)
                .build();
        final var resaved = officerFilingService.save(updated, transaction.getId());
        logger.infoContext(transaction.getId(), "Filing saved", new LogHelper.Builder(transaction)
                        .withFilingId(resaved.getId())
                        .withRequest(request)
                        .build());
        return new ImmutablePair<>(links, resaved);
    }

    private Links buildLinks(final String savedFilingId, final HttpServletRequest request) {
        final var requestUri = request.getRequestURI();
        final var uriBuilder = UriComponentsBuilder.fromUriString(requestUri);
        final var objectId = new ObjectId(Objects.requireNonNull(savedFilingId));
        final var objectIdString = objectId.toHexString();
        // A patch URI already ends with the filingId
        if(!requestUri.endsWith(objectIdString)){
            uriBuilder.pathSegment(objectIdString);
        }
        final var selfUri = uriBuilder.build().toUri();

        final var validateUri = uriBuilder.pathSegment(VALIDATION_STATUS)
            .build().toUri();

        return new Links(selfUri, validateUri);
    }

    private String getExistingFilingId(Transaction transaction){
        Map<String, Resource> resources = transaction.getResources();
        String filingId = null;
        if(resources != null && !resources.isEmpty()){
            // There should only be one resource.
            Resource entry = (Resource) resources.values().toArray()[0];
            var resource = entry.getLinks().get(RESOURCE);
            var resourcePath = new File(resource);
            filingId = resourcePath.getName();
        }
        return filingId;
    }

    private void validateTransactionLinkedToFiling(Transaction transaction, String filingResourceId) {
        List<FieldError> errors = new ArrayList<>();

        if (transaction == null) {
            errors.add(new FieldError(OFFICER_FILING_NAME, TRANSACTION, "Transaction not found"));
            throw new InvalidFilingException(errors);
        }
        if (transaction.getResources() == null) {
            errors.add(new FieldError(OFFICER_FILING_NAME, TRANSACTION, "Transaction resources not found"));
            throw new InvalidFilingException(errors);
        }
        if (transaction.getResources().isEmpty()) {
            errors.add(new FieldError(OFFICER_FILING_NAME, TRANSACTION, "Transaction resources empty"));
            throw new InvalidFilingException(errors);
        }

        String path = "/transactions/" + transaction.getId() + "/officers/" + filingResourceId; 
        logger.debug("Validate transaction linked to: " + path);

        for (Resource resource : transaction.getResources().values()) {
            if (resource.getKind().equals(OFFICER_FILING) &&
                resource.getLinks().containsKey(RESOURCE) && 
                path.equals(resource.getLinks().get(RESOURCE))) {
                return;
            }
        }

        errors.add(new FieldError(OFFICER_FILING_NAME, FILING_RESOURCE_ID, "Filing resource does not match request"));
        throw new InvalidFilingException(errors);
    }
}