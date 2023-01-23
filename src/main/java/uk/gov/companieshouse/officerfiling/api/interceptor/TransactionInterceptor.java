package uk.gov.companieshouse.officerfiling.api.interceptor;

import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;
import uk.gov.companieshouse.officerfiling.api.utils.LogHelper;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Component
public class TransactionInterceptor implements HandlerInterceptor {

    private final TransactionService transactionService;
    private final Logger logger;

    @Autowired
    public TransactionInterceptor(TransactionService transactionService, Logger logger) {
        this.transactionService = transactionService;
        this.logger = logger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        final Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        final var transactionId = pathVariables.get("transId");
        String passthroughHeader = request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());

        final var logMap = LogHelper.createLogMap(transactionId);
        try {
            logger.debugContext(transactionId, "Getting transaction for request.", logMap);

            final var transaction = transactionService.getTransaction(transactionId, passthroughHeader);
            logMap.put("company_number", transaction.getCompanyNumber());
            logMap.put("company_name", transaction.getCompanyName());
            logger.debugContext(transactionId, "Retrieved transaction details", logMap);

            if (!(Objects.equals(transaction.getStatus().getStatus(), "open"))) {
                //logger.
                response.setStatus(400);
                return false;
            }

            request.setAttribute("transaction", transaction);
            return true;
        } catch (Exception e) {
            logger.errorContext(transactionId, "Error retrieving transaction" , e, logMap);
            response.setStatus(500);
            return false;
        }
    }
}
