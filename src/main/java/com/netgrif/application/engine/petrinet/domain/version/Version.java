package com.netgrif.application.engine.petrinet.domain.version;

import com.netgrif.application.engine.petrinet.domain.VersionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

import static com.netgrif.application.engine.petrinet.domain.VersionType.MAJOR;
import static com.netgrif.application.engine.petrinet.domain.VersionType.MINOR;

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

    private static final String VERSION_STRING_REGEX = "[0-9]+\\.[0-9]+\\.[0-9]+";

    private static final String VERSION_DELIMITER = ".";

    private long major;

    private long minor;

    private long patch;

    /**
     * Creates version initialized to 1.0.0
     * */
    public Version() {
        major = 1;
        minor = 0;
        patch = 0;
    }

    /**
     * Example output {@code "1.1.2"}
     * */
    @Override
    public String toString() {
        return major + VERSION_DELIMITER + minor + VERSION_DELIMITER + patch;
    }

    /**
     * Increments version number corresponding to provided {@link VersionType}
     *
     * @param type {@link VersionType} of number to be incremented
     * */
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

    public static Version of(String versionString) {
        if (versionString == null || !versionString.matches("[0-9]+\\.[0-9]+\\.[0-9]+")) {
            return null;
        }
        String[] versionParts = versionString.split("\\.");
        return new Version(Integer.parseInt(versionParts[0]), Integer.parseInt(versionParts[1]), Integer.parseInt(versionParts[2]));
    }

    @Override
    public Version clone() {
        Version clone = new Version();
        clone.setMajor(this.major);
        clone.setMinor(this.minor);
        clone.setPatch(this.patch);
        return clone;
    }

    /**
     * Compares versions by attributes.
     *
     * @param o object, that should be instance of {@link Version}
     *
     * @return true if the provided object is not null, of correct type and matches in attribute numbers
     * */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Version compareWith = (Version) o;
        return major == compareWith.getMajor() && minor == compareWith.getMinor() && patch == compareWith.getPatch() ;
    }

    /**
     * Creates new {@link Version} of the provided version string
     *
     * @param versionString version string by which the {@link Version} is created. Must be in correct format
     * ({@link Version#VERSION_STRING_REGEX})
     * */
    public static Version of(String versionString) {
        if (versionString == null || !versionString.matches(VERSION_STRING_REGEX)) {
            throw new IllegalArgumentException("Version string is invalid");
        }
        String[] versionParts = versionString.split(String.format("\\%s", VERSION_DELIMITER));
        return new Version(Integer.parseInt(versionParts[0]), Integer.parseInt(versionParts[1]),
                Integer.parseInt(versionParts[2]));
    }
}