package uk.gov.companieshouse.officerfiling.api.controller;

import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentFullRecordAPI;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.error.InvalidFilingException;
import uk.gov.companieshouse.officerfiling.api.exception.FeatureNotEnabledException;
import uk.gov.companieshouse.officerfiling.api.model.entity.Address;
import uk.gov.companieshouse.officerfiling.api.model.entity.Date3Tuple;
import uk.gov.companieshouse.officerfiling.api.model.entity.FormerName;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFilingData;
import uk.gov.companieshouse.officerfiling.api.model.filing.FilingResponse;
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/transactions/{transactionId}/officers")
public class OfficerFilingControllerImpl implements OfficerFilingController {
    public static final String VALIDATION_STATUS = "validation_status";
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
    public ResponseEntity<Object> createFiling(@RequestAttribute("transaction") Transaction transaction,
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
        final var entity = filingMapper.map(dto);
        final var saveData = saveFilingWithLinks(entity, transaction, request, dto);
        final var links = saveData.getLeft();
        String filingId = saveData.getRight();
        final var resourceMap = buildResourceMap(links);

        transaction.setResources(resourceMap);
        transactionService.updateTransaction(transaction, passthroughHeader);

        // Create response with filing ID
        final var filingResponse = new FilingResponse(filingId);
        return ResponseEntity.created(links.getSelf()).body(filingResponse);
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
            @RequestAttribute("transaction") Transaction transaction,
            @RequestBody @Valid @NotNull final OfficerFilingDto dto,
            @PathVariable("filingResourceId") final String filingResourceId,
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
            officerFiling = officerFilingOptional.get();
            officerFiling = officerFilingService.mergeFilings(officerFiling, filingMapper.map(dto), transaction);
        }
        else{
            // Else just create a new filing
            officerFiling = filingMapper.map(dto);
        }

        final var saveDetails = saveFilingWithLinks(officerFiling, transaction, request, dto);
        final var links = saveDetails.getLeft();
        final var resourceMap = buildResourceMap(links);

        transaction.setResources(resourceMap);
        transactionService.updateTransaction(transaction, passthroughHeader);

        return ResponseEntity.ok(null);
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
            @PathVariable("transactionId") final String transId,
            @PathVariable("filingResourceId") final String filingResource) {

        if(!isTm01Enabled){
            throw new FeatureNotEnabledException();
        }

        var maybeOfficerFiling = officerFilingService.get(filingResource, transId);

        var maybeDto = maybeOfficerFiling.map(filingMapper::map);

        return maybeDto.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound()
                        .build());
    }

    private Map<String, Resource> buildResourceMap(final Links links) {
        final Map<String, Resource> resourceMap = new HashMap<>();
        final var resource = new Resource();
        final var linksMap = new HashMap<>(
                Map.of("resource", links.getSelf().toString(), VALIDATION_STATUS,
                        links.getValidationStatus().toString()));

        resource.setKind("officer-filing");
        resource.setLinks(linksMap);
        resource.setUpdatedAt(clock.instant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        resourceMap.put(links.getSelf().toString(), resource);
        return resourceMap;
    }

    private ImmutablePair<Links,String> saveFilingWithLinks(final OfficerFiling entity, final Transaction transaction,
            final HttpServletRequest request, OfficerFilingDto dto) {

        final var now = clock.instant();
        var createNow = now;
        OfficerFiling entityWithCreatedUpdated;
        var offdata = buildOfficerFilingData(entity, dto);
        var test = entity.getCreatedAt() != null;
        if(entity.getCreatedAt() != null){
            createNow = entity.getCreatedAt();
        }

        final var create = createNow;
        entityWithCreatedUpdated =
                OfficerFiling.builder(entity).createdAt(create).updatedAt(now).data(offdata)
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
        return new ImmutablePair<>(links, resaved.getId());
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

    private OfficerFilingData buildOfficerFilingData(OfficerFiling officerFiling, OfficerFilingDto dto) {
        OfficerFilingData data;
        String refAppointmentId = "";
        String refEtag = "";
        Instant resignOn = null;
        // if we have data already in the filing,  fill the fields with it.
        if(officerFiling.getData() != null) {
            data = officerFiling.getData();
            refEtag = data.getReferenceAppointmentId();
            refAppointmentId = data.getReferenceAppointmentId();
            resignOn = data.getResignedOn();
        }
        // if we have data coming in from the dto, replace existing data with the new data coming in.
        if(dto.getReferenceEtag()  != null) {
            refEtag = dto.getReferenceEtag();
        }
        if(dto.getReferenceAppointmentId()  != null) {
            refAppointmentId = dto.getReferenceAppointmentId();
        }
        if(dto.getResignedOn()  != null) {
            resignOn = dto.getResignedOn().atStartOfDay(ZoneId.systemDefault()).toInstant();
        }

        final var referenceAppointmentId = refAppointmentId;
        final var referenceEtag = refEtag;
        final var resignedOn = resignOn;

        return new OfficerFilingData(
                referenceEtag,
                referenceAppointmentId,
                resignedOn);
    }

}