package uk.gov.companieshouse.officerfiling.api.controller;

import static uk.gov.companieshouse.officerfiling.api.model.entity.Links.PREFIX_PRIVATE;

import java.time.Clock;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.error.InvalidFilingException;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.entity.Links;
import uk.gov.companieshouse.officerfiling.api.model.entity.OfficerFiling;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@RestController
@RequestMapping("/transactions/{transId}/officers")
public class OfficerFilingControllerImpl implements OfficerFilingController {
    public static final String VALIDATION_STATUS = "validation_status";
    private final TransactionService transactionService;
    private final OfficerFilingService officerFilingService;
    private final OfficerFilingMapper filingMapper;
    private final Clock clock;
    private final Logger logger;

    public OfficerFilingControllerImpl(final TransactionService transactionService,
            final OfficerFilingService officerFilingService, final OfficerFilingMapper filingMapper,
            final Clock clock, final Logger logger) {
        this.transactionService = transactionService;
        this.officerFilingService = officerFilingService;
        this.filingMapper = filingMapper;
        this.clock = clock;
        this.logger = logger;
    }

    @Override
    @PostMapping(produces = {"application/json"}, consumes = {"application/json"})
    public ResponseEntity<Object> createFiling(@PathVariable final String transId,
            @RequestBody @Valid @NotNull final OfficerFilingDto dto, final BindingResult bindingResult,
            final HttpServletRequest request) {
        final Map<String, Object> logMap = new HashMap<>();

        logMap.put("transactionId", transId);
        logger.debugRequest(request, "POST", logMap);

        if (bindingResult != null && bindingResult.hasErrors()) {
            throw new InvalidFilingException(bindingResult.getFieldErrors());
        }

        final var passthroughHeader =
                request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());
        final var transaction = transactionService.getTransaction(transId, passthroughHeader);
        logger.infoContext(transId, "transaction found", logMap);

        final var entity = filingMapper.map(dto);
        final var links = saveFilingWithLinks(entity, transId, request, logMap);
        final var resourceMap = buildResourceMap(links);

        transaction.setResources(resourceMap);
        transactionService.updateTransaction(transaction, passthroughHeader);

        return ResponseEntity.created(links.getSelf())
                .build();
    }

    @Override
    @GetMapping(value = "/{filingResourceId}", produces = {"application/json"})
    public ResponseEntity<OfficerFilingDto> getFilingForReview(@PathVariable("transId") final String transId,
                                                            @PathVariable("filingResourceId") final String filingResource) {

        var maybeOfficerFiling = officerFilingService.get(filingResource);

        var maybeDto = maybeOfficerFiling.map(filingMapper::map);

        return maybeDto.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
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

    private Links saveFilingWithLinks(final OfficerFiling entity, final String transId,
            final HttpServletRequest request, final Map<String, Object> logMap) {
        final var saved = officerFilingService.save(entity);
        final var links = buildLinks(saved, request);
        final var updated = OfficerFiling.builder(saved).links(links)
                .build();
        final var resaved = officerFilingService.save(updated);

        logMap.put("filingId", resaved.getId());
        logger.infoContext(transId, "Filing saved", logMap);

        return links;
    }

    private Links buildLinks(final OfficerFiling savedFiling, final HttpServletRequest request) {
        final var objectId = new ObjectId(Objects.requireNonNull(savedFiling.getId()));
        final var uriBuilder = UriComponentsBuilder.fromUriString(request.getRequestURI())
                .pathSegment(objectId.toHexString());
        final var selfUri = uriBuilder.build().toUri();
        final var privateUriBuilder = UriComponentsBuilder.fromUriString(PREFIX_PRIVATE + "/" + request.getRequestURI())
                .pathSegment(objectId.toHexString());
        final var validateUri = privateUriBuilder.pathSegment(VALIDATION_STATUS)
                .build().toUri();

        return new Links(selfUri, validateUri);
    }

}
