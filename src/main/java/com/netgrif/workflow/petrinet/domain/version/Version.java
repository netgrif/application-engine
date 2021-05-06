package com.netgrif.workflow.petrinet.domain.version;

import com.netgrif.workflow.petrinet.domain.VersionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import static com.netgrif.workflow.petrinet.domain.VersionType.MAJOR;
import static com.netgrif.workflow.petrinet.domain.VersionType.MINOR;

@Data
@AllArgsConstructor
public class Version {

    public static final String NEWEST = "^";

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
}