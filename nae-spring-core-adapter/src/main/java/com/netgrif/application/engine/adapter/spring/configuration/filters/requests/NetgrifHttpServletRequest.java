package com.netgrif.application.engine.adapter.spring.configuration.filters.requests;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.HashMap;
import java.util.Map;

public class NetgrifHttpServletRequest extends HttpServletRequestWrapper {

    private final Map<String, Object> additionalParams;

    public NetgrifHttpServletRequest(HttpServletRequest request) {
        this(request, new HashMap<>());
    }

    public NetgrifHttpServletRequest(HttpServletRequest request, Map<String, Object> additionalParams) {
        super(request);
        this.additionalParams = additionalParams;
    }

    public Object getAdditionalParameter(String name) {
        return this.additionalParams.get(name);
    }

    public void addAdditionalParameter(String name, Object value) {
        this.additionalParams.put(name, value);
    }
}
