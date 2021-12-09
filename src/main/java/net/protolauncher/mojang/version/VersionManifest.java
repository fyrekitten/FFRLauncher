package net.protolauncher.mojang.version;

import com.google.gson.Gson;
import net.protolauncher.util.ISavable;
import net.protolauncher.util.Network;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * Represents the <a href="https://minecraft.fandom.com/wiki/Version_manifest.json">Mojang Version Manifest</a>
 */
public class VersionManifest implements ISavable {

    // Savable Variables
    private static Path path;
    private static Gson gson;

    // JSON Properties
    private Latest latest;
    private List<VersionInfo> versions;

    // Singleton
    private static VersionManifest instance;

    // Suppress default constructor
    private VersionManifest() { }

    // Savable Implementation
    @Override
    public Type getType() {
        return VersionManifest.class;
    }

    @Override
    public Gson getGson() {
        return gson;
    }

    @Override
    public Path getPath() {
        return path;
    }

    // Getters
    public VersionInfo getLatestRelease() {
        return this.getVersion(latest.release);
    }
    public VersionInfo getLatestSnapshot() {
        return this.getVersion(latest.snapshot);
    }
    public String getLatestReleaseId() {
        return latest.release;
    }
    public String getLatestSnapshotId() {
        return latest.snapshot;
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
     * Loads the manifest from the given url, updating the stored file if necessary.
     * This is the only way to create a version manifest.
     *
     * @param path The location of the version manifest file.
     * @param gson The gson to use for parsing the version manifest.
     * @param manifestUrl The url used if fetching the version manifest is necessary.
     * @param nextUpdate The next time that the version manifest needs to be updated.
     * @return A new {@link VersionManifest.LoadResult} containing the loaded version manifest
     *         and a boolean indicating whether it was updated or not.
     * @throws IOException Thrown if there was an exception while trying to load.
     */
    public static VersionManifest.LoadResult load(
        Path path,
        Gson gson,
        URL manifestUrl,
        Instant nextUpdate
    ) throws IOException {
        VersionManifest.path = path;
        VersionManifest.gson = gson;

        boolean updated;
        if (!Files.exists(path) || Instant.now().isAfter(nextUpdate)) {
            // Download manifest
            String manifestString = Network.stringify(Network.fetch(manifestUrl));

            // Parse manifest
            instance = gson.fromJson(manifestString, VersionManifest.class);
            instance.save();
            updated = true;
        } else {
            if (instance == null) {
                instance = new VersionManifest();
            }
            instance.load();
            updated = false;
        }
        return new LoadResult(instance, updated);
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

    /**
     * Represents a load result from loading in a new version manifest.
     */
    public record LoadResult(VersionManifest manifest, boolean updated) { }

}
