package net.protolauncher.mojang.version;

import com.google.gson.Gson;
import net.protolauncher.mojang.Artifact;
import net.protolauncher.mojang.library.Library;
import net.protolauncher.util.Network;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;

/**
 * Represents a <a href="https://minecraft.fandom.com/wiki/Client.json">Mojang Minecraft Client File</a>.
 */
public class Version {

    // JSON Properties
    private VersionArguments arguments;
    private Artifact assetIndex; // This is not the same as the AssetIndex object, it's an artifact linking to it.
    private String assets; // This is not a list of assets, it's the identifier for the type of asset index.
    @Nullable
    private Integer complianceLevel;
    private VersionDownloads downloads;
    private String id;
    @Nullable
    private JavaVersion javaVersion;
    private List<Library> libraries;
    @Nullable
    private Logging logging;
    private String mainClass;
    @Nullable
    private String minecraftArguments;
    private String minimumLauncherVersion;
    private Date releaseTime;
    private Date time;
    private VersionType type;

    // Suppress default constructor
    private Version() { }

    // Getters
    public VersionArguments getArguments() {
        return arguments;
    }
    public Artifact getAssetIndex() {
        return assetIndex;
    }
    public String getAssets() {
        return assets;
    }
    @Nullable
    public Integer getComplianceLevel() {
        return complianceLevel;
    }
    public VersionDownloads getDownloads() {
        return downloads;
    }
    public String getId() {
        return id;
    }
    @Nullable
    public JavaVersion getJavaVersion() {
        return javaVersion;
    }
    public List<Library> getLibraries() {
        return libraries;
    }
    @Nullable
    public Logging getLogging() {
        return logging;
    }
    public String getMainClass() {
        return mainClass;
    }
    @Nullable
    public String getMinecraftArguments() {
        return minecraftArguments;
    }
    public String getMinimumLauncherVersion() {
        return minimumLauncherVersion;
    }
    public Date getReleaseTime() {
        return releaseTime;
    }
    public Date getTime() {
        return time;
    }
    public VersionType getType() {
        return type;
    }

    /**
     * Resolves the folder for this game version from the given parent folder.
     *
     * @param id The id of the game version.
     * @param parent The parent folder containing other game versions.
     * @return The path for the folder of this game version.
     */
    public static Path resolveFolderPath(String id, Path parent) {
        return parent.resolve(id + "/");
    }

    /**
     * Resolves the file for this game version from the given parent folder,
     * usually obtained from {@link Version#resolveFolderPath(String, Path)}.
     *
     * @param id The id of the game version.
     * @param parent The parent folder, presumably gotten from {@link Version#resolveFolderPath(String, Path)}.
     * @return The path for the file of this game version.
     */
    public static Path resolveFilePath(String id, Path parent) {
        return parent.resolve(id + ".json");
    }

    /**
     * Resolves the folder for this game version from the given parent folder.
     * This is a shorthand for calling {@link Version#resolveFolderPath(String, Path)}
     *
     * @param parent The parent folder containing other game versions.
     * @return The path for the folder of this game version.
     */
    public Path resolveFolderPath(Path parent) {
        return resolveFolderPath(id, parent);
    }

    /**
     * Resolves the file for this game version from the given parent folder.
     * This is a shorthand for calling {@link Version#resolveFilePath(String, Path)}.
     *
     * @param parent The parent folder, presumably gotten from {@link Version#resolveFolderPath(Path)}.
     * @return The path for the file of this game version.
     */
    public Path resolveFilePath(Path parent) {
        return resolveFilePath(id, parent);
    }

    /**
     * Loads a game version, downloading the file if necessary.
     * This is the only way to create a game version.
     *
     * @param folderLocation The folder location for all other version files.
     * @param gson The gson to use for parsing the game version.
     * @param versionInfo The info regarding this game version fetched from the {@link VersionManifest}.
     * @return A new {@link Version}.
     * @throws IOException Thrown if there was an exception while trying to download or parse.
     */
    public static Version load(
        Path folderLocation,
        Gson gson,
        VersionInfo versionInfo
    ) throws IOException {
        // Check for existing version file
        String id = versionInfo.getId();
        Path path = resolveFilePath(id, resolveFolderPath(id, folderLocation));
        Version instance;

        // If it doesn't exist, download a new one
        // Otherwise, load the existing file
        if (!Files.exists(path)) {
            // Download version
            String versionString = Network.stringify(Network.fetch(new URL(versionInfo.getUrl())));
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, versionString, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Parse version
            instance = gson.fromJson(versionString, Version.class);
        } else {
            instance = gson.fromJson(Files.newBufferedReader(path), Version.class);
        }
        return instance;
    }

    /**
     * Represents a java version for a Mojang Minecraft Client File.
     */
    public static class JavaVersion {

        // JSON Properties
        private String component;
        private int majorVersion;

        // Suppress default constructor
        private JavaVersion() { }

        // Getters
        public String getComponent() {
            return component;
        }
        public int getMajorVersion() {
            return majorVersion;
        }

    }

    /**
     * Represents the logging configuration for a Mojang Minecraft Client File.
     */
    public static class Logging {

        // JSON Properties
        private Client client;

        // Suppress default constructor
        private Logging() { }

        // Getters
        public Client getClient() {
            return client;
        }

        /**
         * Represents the client logging configuration for a Mojang Minecraft Client File.
         */
        public static class Client {

            // JSON Properties
            private String argument;
            private Artifact file;
            private String type;

            // Suppress default constructor
            private Client() { }

            // Getters
            public String getArgument() {
                return argument;
            }
            public Artifact getFile() {
                return file;
            }
            public String getType() {
                return type;
            }

        }

    }

}
