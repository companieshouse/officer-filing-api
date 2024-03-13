package uk.gov.companieshouse.officerfiling.api.interceptor;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import io.micrometer.core.instrument.util.StringUtils;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.service.OfficerFilingService;

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

        if (StringUtils.isEmpty(filingId)) {
            logger.debug("Skip intercepting request to validate transaction as no filing resource id in " + request.getRequestURI());
            return true;
        }

        logger.debug("Intercepting request to validate transaction for " + request.getRequestURI());

        // check filing exists
        final var officerFiling = officerFilingService.get(filingId, transactionId);
        if (officerFiling.isEmpty()) {
            logger.errorRequest(request, "Filing resource not found");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }

        String selfLink = officerFiling.get().getLinks().getSelf().toString();
        String path = "/transactions/" + transactionId + "/officers/" + filingId;
        logger.debug("Intercepting request path " + path + " to validate filing resource " + officerFiling.get().getLinks().getSelf().toString());
       
        if (!path.equals(selfLink)) {
            logger.errorRequest(request, "Filing resource does not match request");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }

        return true;
    }
}
