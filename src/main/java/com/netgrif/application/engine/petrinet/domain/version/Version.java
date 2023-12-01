package com.netgrif.application.engine.petrinet.domain.version;

import com.netgrif.application.engine.petrinet.domain.VersionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import static com.netgrif.application.engine.petrinet.domain.VersionType.MAJOR;
import static com.netgrif.application.engine.petrinet.domain.VersionType.MINOR;

@Data
@AllArgsConstructor
public class Version {

    /**
     * @deprecated since 6.0.3 - please use {@link #LATEST} instead
     * */
    @Deprecated(since = "6.0.3", forRemoval = false)
    public static final String NEWEST = "^";

    public static final String LATEST = "latest";

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
}