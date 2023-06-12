package uk.gov.companieshouse.officerfiling.api.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class ValidTransactionInterceptor implements HandlerInterceptor {
    @Autowired
    private Logger logger;
    @Autowired
    private OfficerFilingService officerFilingService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        final Map<String, String> pathVariables = (Map)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        final String transactionId = (String)pathVariables.get("transactionId");
        final var filingId = pathVariables.get("filingResourceId");

        // check filing id from request matches filing id from transaction
        final var patchResult = officerFilingService.get(filingId, transactionId).filter(
                f1 -> officerFilingService.requestMatchesResourceSelf(request, f1));

        if (patchResult.isPresent()) {
            return true;
        } else {
            logger.errorRequest(request, "Filing resource not found");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }
    }
}
