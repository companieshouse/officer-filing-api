package uk.gov.companieshouse.officerfiling.api.interceptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.api.util.security.Permission.Key;
import uk.gov.companieshouse.api.util.security.Permission.Value;
import uk.gov.companieshouse.api.util.security.TokenPermissions;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officerfiling.api.service.TransactionService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import static uk.gov.companieshouse.officerfiling.api.utils.Constants.ERIC_REQUEST_ID_KEY;

public class OfficersCRUDAuthenticationInterceptor implements HandlerInterceptor {
    
    private static final String TRANSACTION_ID_KEY = "transactionId";
    public static final String COMPANY_NUMBER_KEY = "company_number";
    public static final String ERIC_AUTHORISED_TOKEN_PERMISSIONS = "ERIC-Authorised-Token-Permissions";

    @Autowired
    private Logger logger;

    @Autowired
    private TransactionService transactionService;

    public OfficersCRUDAuthenticationInterceptor() {
    }

    public OfficersCRUDAuthenticationInterceptor(Logger logger, TransactionService transactionService) {
        this.logger = logger;
        this.transactionService = transactionService;
    }

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
        final String reqId = request.getHeader(ERIC_REQUEST_ID_KEY);
        final var logMap = new HashMap<String, Object>();
        
        if (StringUtils.isEmpty(transactionId)) {
            logger.errorContext(reqId, "OfficersCRUDAuthenticationInterceptor unauthorised - no transaction identifier found", null, logMap);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        logMap.put(TRANSACTION_ID_KEY, transactionId);
        logger.debugContext(reqId, "OfficersCRUDAuthenticationInterceptor authenticate request for transaction: " + transactionId, logMap);
        var companyNumberInTransaction = getCompanyNumberInTransaction(request, transactionId);
        if (companyNumberInTransaction.isEmpty()) {
            logger.errorContext(reqId, "OfficersCRUDAuthenticationInterceptor unauthorised - no company number in transaction", null, logMap);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
       
        var companyNumberInScope = getCompanyNumberInScope(request);
        logger.debugContext(reqId, "OfficersCRUDAuthenticationInterceptor authenticate request for company number: " + companyNumberForDisplay(companyNumberInScope), logMap);
        if (companyNumberInScope == null) {
            logger.errorContext(reqId, "OfficersCRUDAuthenticationInterceptor unauthorised - no company number in scope", null, logMap);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        if (!companyNumberInTransaction.get().equals(companyNumberInScope)) {
            logger.errorContext(reqId, "OfficersCRUDAuthenticationInterceptor unauthorised - company number in transaction does not match company number in scope", null, logMap);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

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

        // Check the user has the company_officers=create permission
        boolean hasCompanyOfficersUpdatePermission = tokenPermissions.hasPermission(
                Key.COMPANY_OFFICERS, Value.UPDATE);

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
        authInfoMap.put("has_company_officers_update_permission",
                hasCompanyOfficersUpdatePermission);
        authInfoMap.put("has_company_officers_readprotected_permission",
                hasCompanyOfficersReadProtectedPermission);
        authInfoMap.put("company_number_in_transaction",
                companyNumberForDisplay(companyNumberInTransaction.get()));

        if (hasCompanyOfficersDeletePermission && hasCompanyOfficersCreatePermission && hasCompanyOfficersUpdatePermission && hasCompanyOfficersReadProtectedPermission) {
            logger.debugContext(reqId,
                    "OfficersCRUDAuthenticationInterceptor authorised with company_officers=readprotected, " +
                            "company_officers=delete, company_officers=update and company_officers=create permissions",
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

    private Optional<String> getCompanyNumberInTransaction(HttpServletRequest request, String transactionId) {
        String passthroughHeader = request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());
        String reqId = request.getHeader(ERIC_REQUEST_ID_KEY);
        var logMap = new HashMap<String, Object>();
        logMap.put(TRANSACTION_ID_KEY, transactionId);

        final var transaction = transactionService.getTransaction(transactionId, passthroughHeader);
        if (transaction == null) {
            logger.debugContext(reqId, "Call to Transaction Service returned null transaction object " + transactionId, logMap);
            return Optional.empty();
        }
        logMap.put("company_number_in_transaction", companyNumberForDisplay(transaction.getCompanyNumber()));
        logger.debugContext(reqId, "Transaction successfully retrieved " + transactionId, logMap);
        return Optional.ofNullable(transaction.getCompanyNumber());
    }

    String getCompanyNumberInScope (HttpServletRequest request) {
        final Map<String, List<String>> privileges = getERICTokenPermissions(request);
        var privilegesList = privileges.get(COMPANY_NUMBER_KEY);
        if (privilegesList == null) {
            return null;
        }
        return privilegesList.get(0);
    }

    private Map<String, List<String>> getERICTokenPermissions(HttpServletRequest request) {
        String tokenPermissionsHeader = request.getHeader(ERIC_AUTHORISED_TOKEN_PERMISSIONS);
        Map<String, List<String>> permissions = new HashMap<>();
        if (tokenPermissionsHeader != null) {
            for (String pair : tokenPermissionsHeader.split(" ")) {
                String[] parts = pair.split("=");
                if (parts.length == 2) {
                    permissions.put(parts[0], Arrays.asList(parts[1].split(",")));
                }
            }
        }
        return permissions;
    }

    private String companyNumberForDisplay(String companyNumber) {
        return StringUtils.isEmpty(companyNumber) ? "None" : companyNumber;
    }
}
