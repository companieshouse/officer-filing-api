package uk.gov.companieshouse.officerfiling.api.interceptor;

import static uk.gov.companieshouse.officerfiling.api.utils.Constants.ERIC_REQUEST_ID_KEY;
import static uk.gov.companieshouse.officerfiling.api.utils.Constants.TRANSACTION_ID_KEY;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.api.util.security.Permission.Key;
import uk.gov.companieshouse.api.util.security.Permission.Value;
import uk.gov.companieshouse.api.util.security.TokenPermissions;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.officerfiling.api.OfficerFilingApiApplication;


@Component
public class OfficersCRUDAuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(OfficerFilingApiApplication.APP_NAMESPACE);
    /**
     * Pre handle method to authorize the request before it reaches the controller. Retrieves the
     * TokenPermissions stored in the request (which must have been previously added by the
     * TokenPermissionsInterceptor) and checks the relevant permissions.
     * TODO this will be replaced once the CRUDAuthenticationInterceptor has been updated to fit
     * the officer filing API authorisation model.
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response, @NonNull Object handler) {
        final Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(
                        HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        final var transactionId = pathVariables.get(TRANSACTION_ID_KEY);

        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transactionId);
        String reqId = request.getHeader(ERIC_REQUEST_ID_KEY);

        // TokenPermissions should have been set up in the request by TokenPermissionsInterceptor
        final var tokenPermissions = getTokenPermissions(request)
                .orElseThrow(() -> new IllegalStateException(
                        "OfficersCRUDAuthenticationInterceptor - TokenPermissions object not present in request"));

        // Check the user has the company_officers=delete permission
        boolean hasCompanyOfficersDeletePermission = tokenPermissions.hasPermission(
                Key.COMPANY_OFFICERS, Value.DELETE);

        // Check the user has the company_officers=create permission
        boolean hasCompanyOfficersCreatePermission = tokenPermissions.hasPermission(
                Key.COMPANY_OFFICERS, Value.CREATE);

        // Check the user has the company_officers=readprotected permission
        boolean hasCompanyOfficersReadProtectedPermission = tokenPermissions.hasPermission(
                Key.COMPANY_OFFICERS, Value.READ_PROTECTED);

        var authInfoMap = new HashMap<String, Object>();
        authInfoMap.put(TRANSACTION_ID_KEY, transactionId);
        authInfoMap.put("request_method", request.getMethod());
        authInfoMap.put("has_company_officers_delete_permission",
                hasCompanyOfficersDeletePermission);
        authInfoMap.put("has_company_officers_create_permission",
                hasCompanyOfficersCreatePermission);
        authInfoMap.put("has_company_officers_readprotected_permission",
                hasCompanyOfficersReadProtectedPermission);

        if (hasCompanyOfficersDeletePermission && hasCompanyOfficersCreatePermission && hasCompanyOfficersReadProtectedPermission) {
            logger.debugContext(reqId,
                    "OfficersCRUDAuthenticationInterceptor authorised with company_officers=readprotected, " +
                            "company_officers=delete and company_officers=create permissions",
                    authInfoMap);
            return true;
        }

        logger.errorContext(reqId, "OfficersCRUDAuthenticationInterceptor unauthorised", null,
                authInfoMap);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }

    protected Optional<TokenPermissions> getTokenPermissions(HttpServletRequest request) {
        return AuthorisationUtil.getTokenPermissions(request);
    }
}
