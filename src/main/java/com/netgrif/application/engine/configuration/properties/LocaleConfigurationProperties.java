package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Data
@Component
@ConfigurationProperties(prefix = "nae.locale")
public class LocaleConfigurationProperties {

    /**
     * Defines the default server time zone
     * */
    private String defaultTimeZone = "UTC";

    /**
     * Defines the default server patters where {@link com.netgrif.application.engine.configuration.locale.LocaleInterceptor}
     * will execute {@link com.netgrif.application.engine.configuration.locale.LocaleInterceptor#preHandle(HttpServletRequest, HttpServletResponse, Object)}
     * function
     * */
    private String defaultPathPatterns = "/api/task/{id}/data";

}
