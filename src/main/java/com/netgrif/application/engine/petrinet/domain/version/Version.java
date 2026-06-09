package com.netgrif.application.engine.petrinet.domain.version;

import com.netgrif.application.engine.petrinet.domain.VersionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

import static com.netgrif.application.engine.petrinet.domain.VersionType.MAJOR;
import static com.netgrif.application.engine.petrinet.domain.VersionType.MINOR;

@Slf4j
@Data
@AllArgsConstructor
public class Version implements Serializable {

    /**
     * @deprecated since 6.0.3 - please use {@link #LATEST} instead
     */
    @Deprecated(since = "6.0.3", forRemoval = false)
    public static final String NEWEST = "^";

    public static final String LATEST = "latest";
    private static final long serialVersionUID = -4714902376220642455L;

    private long major;

    private long minor;

    private long patch;

    public Version() {
        major = 1;
        minor = 0;
        patch = 0;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }

    public void increment(VersionType type) {
        if (type == MAJOR) {
            major += 1;
            minor = 0;
            patch = 0;
        } else if (type == MINOR) {
            minor += 1;
            patch = 0;
        } else {
            patch += 1;
        }
    }

    @Override
    public Version clone() {
        Version clone = new Version();
        clone.setMajor(this.major);
        clone.setMinor(this.minor);
        clone.setPatch(this.patch);
        return clone;
    }

    public static Version from(String stringVersion) {
        try {
            Version version = new Version();
            String[] split = stringVersion.split("\\.");
            version.setMajor(Long.parseLong(split[0]));
            version.setMinor(Long.parseLong(split[1]));
            version.setPatch(Long.parseLong(split[2]));
            return version;
        } catch (Exception e) {
            log.error("Could not parse version " + stringVersion + " caused by:", e);
            throw new IllegalArgumentException("Could not parse version " + stringVersion);
        }
    }
}