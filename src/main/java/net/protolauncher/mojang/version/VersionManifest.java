package net.protolauncher.mojang.version;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents the <a href="https://minecraft.fandom.com/wiki/Version_manifest.json">Mojang Version Manifest</a>
 */
public class VersionManifest {

    // JSON Properties
    private Latest latest;
    private List<VersionInfo> versions;

    // Suppress default constructor
    private VersionManifest() { }

    // Getters
    public VersionInfo getLatestRelease() {
        return this.getVersion(latest.release);
    }
    public VersionInfo getLatestSnapshot() {
        if (latest.snapshot.equals(latest.release)) {
            return this.getVersionsOfType(VersionType.SNAPSHOT).get(0);
        } else {
            return this.getVersion(latest.snapshot);
        }
    }
    public String getLatestReleaseId() {
        return latest.release;
    }
    public String getLatestSnapshotId() {
        if (latest.snapshot.equals(latest.release)) {
            return this.getVersionsOfType(VersionType.SNAPSHOT).get(0).id;
        } else {
            return latest.snapshot;
        }
    }
    public List<VersionInfo> getVersions() {
        return versions;
    }

    /**
     * Fetches all versions of the corresponding type from the manifest.
     *
     * @param type The type to filter by.
     * @return A filtered list of {@link VersionInfo}'s.
     */
    public List<VersionInfo> getVersionsOfType(VersionType type) {
        return versions.stream().filter(x -> x.type == type).toList();
    }

    /**
     * Attempts to fetch a {@link VersionInfo} by the given version id from the manifest.
     * @param id The id of the {@link VersionInfo} to fetch.
     * @return The {@link VersionInfo} or null if not found.
     */
    @Nullable
    public VersionInfo getVersion(String id) {
        return versions.stream().filter(x -> x.id.equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    /**
     * Represents the 'latest' object in the Mojang Version Manifest.
     */
    public static class Latest {

        // JSON Properties
        private String release;
        private String snapshot;

        // Suppress default constructor
        private Latest() { }

    }

}
