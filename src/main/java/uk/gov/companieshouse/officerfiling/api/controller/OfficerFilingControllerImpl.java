package uk.gov.companieshouse.officerfiling.api.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.officerfiling.api.model.dto.OfficerFilingDto;
import uk.gov.companieshouse.officerfiling.api.model.mapper.OfficerFilingMapper;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;

@RestController
@RequestMapping(value = "/transactions/{transId}/officers",
        produces = {"application/json"}, consumes = {"application/json"})
public class OfficerFilingControllerImpl implements OfficerFilingController {
    private OfficerFilingService officerFilingService;
    private OfficerFilingMapper filingMapper;

    public OfficerFilingControllerImpl(final OfficerFilingService officerFilingService,
            final OfficerFilingMapper filingMapper) {
        this.officerFilingService = officerFilingService;
        this.filingMapper = filingMapper;
    }

    @Override
    @PostMapping
    public ResponseEntity<Object> createFiling(@PathVariable final String transId,
            @RequestBody @Valid @NotNull OfficerFilingDto dto, BindingResult result) {
        final var entity = filingMapper.map(dto);

        officerFilingService.save(entity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }
}
