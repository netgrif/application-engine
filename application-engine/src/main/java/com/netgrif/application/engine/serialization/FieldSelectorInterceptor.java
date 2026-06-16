package com.netgrif.application.engine.serialization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class FieldSelectorInterceptor implements HandlerInterceptor {

    private final FieldSelectorHolder holder;

    public FieldSelectorInterceptor(FieldSelectorHolder holder) {
        this.holder = holder;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String fields = request.getParameter("fields");
        holder.setSelector(FieldSelector.parse(fields));
        return true;
    }
}
