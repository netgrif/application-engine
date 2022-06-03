package api.petrinet.version;

public final class VersionDto {

    private long major;

    private long minor;

    private long patch;

    public VersionDto() {
    }

    public VersionDto(long major, long minor, long patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public long getMajor() {
        return major;
    }

    public void setMajor(long major) {
        this.major = major;
    }

    public long getMinor() {
        return minor;
    }

    public void setMinor(long minor) {
        this.minor = minor;
    }

    public long getPatch() {
        return patch;
    }

    public void setPatch(long patch) {
        this.patch = patch;
    }
}
