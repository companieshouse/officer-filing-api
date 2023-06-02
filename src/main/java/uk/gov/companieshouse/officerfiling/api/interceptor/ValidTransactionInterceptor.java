package uk.gov.companieshouse.officerfiling.api.interceptor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.api.model.transaction.Transaction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static uk.gov.companieshouse.officerfiling.api.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.officerfiling.api.utils.Constants.TRANSACTION_ID_KEY;

@Component
public class ValidTransactionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response, @NonNull Object handler) {
        final Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(
                        HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        final var transaction = request.getAttribute("transaction");
        final var transactionId = transaction.getId();

        final var filingId = pathVariables.get("filingId");

        // check filing id from request matches filing id from transaction



        return true;
    }
}
