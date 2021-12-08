package net.protolauncher.mojang.version;

import java.util.Date;

/**
 * Represents a basic amount of information about a game version.
 * Stems from the Mojang Version Manifest.
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

}
