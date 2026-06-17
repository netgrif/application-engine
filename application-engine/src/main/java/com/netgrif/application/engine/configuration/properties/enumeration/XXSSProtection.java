package com.netgrif.application.engine.configuration.properties.enumeration;

/**
 * DISABLE - Disable
 *
 * DISABLE_XSS (0) - Disables XSS filtering.
 *
 * ENABLE (1) - Enables XSS filtering (usually default in browsers). If a cross-site scripting attack is detected, the browser will sanitize the page (remove the unsafe parts).
 *
 * ENABLE_MODE (1; mode=block) - Enables XSS filtering. Rather than sanitizing the page, the browser will prevent rendering of the page if an attack is detected.
 *
 * More <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-XSS-Protection">Info</a>
 */
public enum XXSSProtection {

    DISABLE,
    DISABLE_XSS,
    ENABLE,
    ENABLE_MODE


}
