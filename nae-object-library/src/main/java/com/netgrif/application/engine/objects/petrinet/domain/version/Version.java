package com.netgrif.application.engine.objects.petrinet.domain.version;

import com.netgrif.application.engine.objects.petrinet.domain.VersionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

import static com.netgrif.application.engine.objects.petrinet.domain.VersionType.MAJOR;
import static com.netgrif.application.engine.objects.petrinet.domain.VersionType.MINOR;

@Data
@AllArgsConstructor
public class Version implements Serializable, Comparable<Version> {

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

    /**
     * Compares this version to the other version
     *
     * @param other other version to be compared with
     * @return 0 if the versions equal, <0 if this is lower than other, >0 if this is higher than other</0>
     */
    public int compareTo(Version other) {
        if (this.major != other.major) {
            return Long.compare(this.major, other.major);
        }
        if (this.minor != other.minor) {
            return Long.compare(this.minor, other.minor);
        }
        return Long.compare(this.patch, other.patch);
    }

    /**
     * Checks if this version is higher than the other
     * @param other other version to be compared with
     * @return true if this version is higher than the other
     */
    public boolean isHigherThan(Version other) {
        return compareTo(other) > 0;
    }

    /**
     * Checks if this version is lower than the other
     * @param other other version to be compared with
     * @return true if this version is lower than the other
     */
    public boolean isLowerThan(Version other) {
        return compareTo(other) < 0;
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
