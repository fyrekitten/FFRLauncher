package net.protolauncher.mojang.version;

import net.protolauncher.mojang.Artifact;
import net.protolauncher.mojang.library.Library;
import org.jetbrains.annotations.Nullable;

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
