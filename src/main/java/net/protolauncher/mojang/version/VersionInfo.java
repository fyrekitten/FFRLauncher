package net.protolauncher.mojang.version;

import java.util.Date;

/**
 * Represents a basic amount of information about a game version.
 * Stems from the {@link VersionManifest}.
 */
public class VersionInfo {

    // JSON Properties
    protected String id;
    protected VersionType type;
    protected String url;
    protected Date time;
    protected Date releaseTime;
    protected String sha1;
    protected int complianceLevel;

    // Suppress default constructor
    private VersionInfo() { }

    // Getters
    public String getId() {
        return id;
    }
    public VersionType getType() {
        return type;
    }
    public String getUrl() {
        return url;
    }
    public Date getTime() {
        return time;
    }
    public Date getReleaseTime() {
        return releaseTime;
    }
    public String getSha1() {
        return sha1;
    }
    public int getComplianceLevel() {
        return complianceLevel;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }
    public void setType(VersionType type) {
        this.type = type;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Copies this version info and returns a new object with the same data.
     * @return A new {@link VersionInfo} with the same data as this one.
     */
    public VersionInfo copy() {
        VersionInfo clone = new VersionInfo();
        clone.id = this.id;
        clone.type = this.type;
        clone.url = this.url;
        clone.time = this.time;
        clone.releaseTime = this.releaseTime;
        clone.sha1 = this.sha1;
        clone.complianceLevel = this.complianceLevel;
        return clone;
    }

}
