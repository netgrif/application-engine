package com.netgrif.application.engine.configuration.properties.enumeration;

/**
 * DENY - The page cannot be displayed in a frame, regardless of the site attempting to do so.
 *
 * SAMEORIGIN - The page can only be displayed if all ancestor frames are same origin to the page itself.
 *
 * More <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Frame-Options">Info</a>
 */
public enum XFrameOptionsMode {

    /**
     * Disable X-Frame-Options
     */
    DISABLE,

    /**
     * The page cannot be displayed in a frame, regardless of the site attempting to do so.
     */
    DENY,

    /**
     * The page can only be displayed if all ancestor frames are same origin to the page itself.
     */
    SAMEORIGIN

}
