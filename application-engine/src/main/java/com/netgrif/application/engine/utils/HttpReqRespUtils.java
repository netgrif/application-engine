package com.netgrif.application.engine.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.netgrif.application.engine.adapter.spring.configuration.filters.requests.NetgrifHttpServletRequest;
import com.netgrif.application.engine.auth.domain.NetgrifAuthenticationToken;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


public class HttpReqRespUtils {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static String getClientIpAddressIfServletRequestExists() {

        if (RequestContextHolder.getRequestAttributes() == null) {
            return "0.0.0.0";
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return getClientIpAddressIfServletRequestExists(request);
    }

    public static String getClientIpAddressIfServletRequestExists(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ipList = request.getHeader(header);
            if (ipList != null && !ipList.isEmpty() && !"unknown".equalsIgnoreCase(ipList)) {
                return ipList.split(",")[0];
            }
        }

        return request.getRemoteAddr();
    }

    public static HttpServletRequest getRequestIfExists() {
        if (RequestContextHolder.getRequestAttributes() == null) {
            return null;
        }

        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static Realm extractRealmFromRequest(NetgrifHttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object param = request.getAdditionalParameter(HttpRequestParamConstants.REALM);
        return param instanceof Realm realm ? realm : null;
    }

    public static JsonNode extractBodyFromRequest(NetgrifHttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object param = request.getAdditionalParameter(HttpRequestParamConstants.REQUEST_BODY);
        return param instanceof JsonNode node ? node : null;
    }

    public static NetgrifAuthenticationToken extractAuthReqTokenFromRequest(NetgrifHttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object param = request.getAdditionalParameter(HttpRequestParamConstants.AUTH_REQ_TOKEN);
        return param instanceof NetgrifAuthenticationToken token ? token : null;
    }
}
