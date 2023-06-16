package uk.gov.companieshouse.officerfiling.api.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class ValidTransactionInterceptor implements HandlerInterceptor {
    @Autowired
    private Logger logger;
    @Autowired
    private OfficerFilingService officerFilingService;

    public ValidTransactionInterceptor(){}

    public ValidTransactionInterceptor(Logger logger, OfficerFilingService officerFilingService) {
        this.logger = logger;
        this.officerFilingService = officerFilingService;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        final Map<String, String> pathVariables = (Map)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        final String transactionId = pathVariables.get("transactionId");
        final String filingId = pathVariables.get("filingResourceId");

        // check filing exists
        final var officerFiling = officerFilingService.get(filingId, transactionId);
        if (officerFiling.isEmpty()) {
            logger.errorRequest(request, "Filing resource not found");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }

        // check filing id from request matches filing id from transaction
        if (!officerFilingService.requestUriContainsFilingSelfLink(request, officerFiling.get())) {
            logger.errorRequest(request, "Filing resource does not match request");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }

        return true;
    }
}
