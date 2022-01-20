package net.protolauncher.api;

import com.google.gson.annotations.Since;
import net.protolauncher.api.Config.FileLocation;
import net.protolauncher.mods.version.ModdedVersionInfo;
import net.protolauncher.mods.version.ModdedVersionType;
import net.protolauncher.mojang.version.VersionInfo;
import net.protolauncher.mojang.version.VersionType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public class Profile {

    // JSON Properties
    @Since(1.0)
    private String name;
    @Since(1.0)
    private String uuid;
    @Since(1.0)
    private String owner;
    @Since(1.0)
    private String path;
    @Since(1.0)
    private Instant lastLaunched;
    @Since(1.0)
    private Version version;
    @Since(1.0)
    private LaunchSettings launchSettings;
    @Since(1.0)
    private ProfileSettings profileSettings;

    /**
     * Constructs a new profile with default settings.
     *
     * @param name The name of this profile.
     * @param mcv The {@link VersionInfo} to use for this profile.
     * @param owner The {@link User} who owns this profile.
     */
    public Profile(String name, VersionInfo mcv, User owner) throws IOException {
        this.name = name;
        this.uuid = UUID.randomUUID().toString().replace("-", "");
        this.owner = owner.getUuid();
        this.path = getDefaultLocation(name, owner.getUuid()).toAbsolutePath().toString();
        Files.createDirectories(Path.of(this.path));
        this.lastLaunched = Instant.MIN;
        this.version = new Version(mcv);
        this.launchSettings = new LaunchSettings();
        this.profileSettings = new ProfileSettings();
    }

    // Getters
    public String getName() {
        return name;
    }
    public String getUuid() {
        return uuid;
    }
    public String getOwner() {
        return owner;
    }
    public String getPath() {
        return path;
    }
    public Instant getLastLaunched() {
        return lastLaunched;
    }
    public Version getVersion() {
        return version;
    }
    public LaunchSettings getLaunchSettings() {
        return launchSettings;
    }
    public ProfileSettings getProfileSettings() {
        return profileSettings;
    }

    // Setters
    public Profile setName(String name) {
        this.name = name;
        return this;
    }
    public Profile setOwner(String owner) {
        this.owner = owner;
        return this;
    }
    public Profile setPath(String path) throws IOException {
        // Delete the old folder if it's empty
        Path origPath = Path.of(this.path);
        if (Files.isDirectory(origPath, LinkOption.NOFOLLOW_LINKS) && Files.list(origPath).findAny().isEmpty()) {
            Files.delete(origPath);
        }

        // Create the new path
        Path newPath = Path.of(path);
        Files.createDirectories(newPath);

        // Set it
        this.path = newPath.toAbsolutePath().toString();
        return this;
    }
    public Profile setLastLaunched(Instant lastLaunched) {
        this.lastLaunched = lastLaunched;
        return this;
    }
    public Profile setVersion(Version version) {
        this.version = version;
        return this;
    }
    public Profile setLaunchSettings(LaunchSettings launchSettings) {
        this.launchSettings = launchSettings;
        return this;
    }
    public Profile setProfileSettings(ProfileSettings profileSettings) {
        this.profileSettings = profileSettings;
        return this;
    }

    /**
     * Resolves the default folder location for a profile.
     *
     * @param name The name of the profile.
     * @param owner The UUID (as a string) of the owner of the profile.
     * @return A {@link Path} to the default folder location for the profile.
     * @throws IOException Thrown if a filesystem check goes wrong.
     */
    public static Path getDefaultLocation(String name, String owner) throws IOException {
        Path loc = FileLocation.PROFILES_FOLDER.resolve(owner + "/" + name + "/");
        int count = 0;
        while (Files.exists(loc, LinkOption.NOFOLLOW_LINKS)) {
            count++;
            loc = FileLocation.PROFILES_FOLDER.resolve(owner + "/" + name + "-" + count + "/");
        }
        return loc;
    }

    /**
     * Represents the game version that this profile is configured to launch.
     */
    public static class Version {

        // JSON properties
        @Since(1.0)
        private VersionType type;
        @Since(1.0)
        private String minecraft;
        @Since(1.0)
        @Nullable
        private ModdedVersionType moddedType;
        @Since(1.0)
        @Nullable
        private String modded;
        @Since(1.0)
        private boolean latest;

        /**
         * Constructs a new profile version with the given {@link VersionInfo}.
         * @param mcv The {@link VersionInfo} to use for this profile.
         */
        public Version(VersionInfo mcv) {
            this.type = mcv.getType();
            this.minecraft = mcv.getId();
            this.moddedType = null;
            this.modded = null;
            this.latest = false;
        }

        // Getters
        public VersionType getType() {
            return type;
        }
        public String getMinecraft() {
            return minecraft;
        }
        @Nullable
        public ModdedVersionType getModdedType() {
            return moddedType;
        }
        @Nullable
        public String getModded() {
            return modded;
        }
        public boolean isLatest() {
            return latest;
        }

        // Setters
        public Version setVersion(VersionInfo mcv) {
            this.type = mcv.getType();
            this.minecraft = mcv.getId();
            return this;
        }
        public Version setModdedVersion(@Nullable ModdedVersionInfo modded) {
            this.moddedType = modded != null ? modded.getType() : null;
            this.modded = modded != null ? modded.getLv() : null;
            return this;
        }
        public Version setLatest(boolean latest) {
            this.latest = latest;
            return this;
        }

    }

    /**
     * Represents various settings regarding how the game is launched from this profile.
     */
    public static class LaunchSettings {

        // JSON Properties
        @Since(1.0)
        private int gameResolutionX;
        @Since(1.0)
        private int gameResolutionY;
        @Since(1.0)
        @Nullable
        private String javaPath;
        @Since(1.0)
        @Nullable
        private String jvmArguments;

        /**
         * Constructs new launch settings with default settings.
         */
        public LaunchSettings() {
            this.gameResolutionX = -1;
            this.gameResolutionY = -1;
            this.javaPath = null;
            this.jvmArguments = null;
        }

        // Getters
        public int getGameResolutionX() {
            return gameResolutionX;
        }
        public int getGameResolutionY() {
            return gameResolutionY;
        }
        @Nullable
        public String getJavaPath() {
            return javaPath;
        }
        @Nullable
        public String getJvmArguments() {
            return jvmArguments;
        }

        // Setters
        public LaunchSettings setGameResolutionX(int gameResolutionX) {
            this.gameResolutionX = gameResolutionX;
            return this;
        }
        public LaunchSettings setGameResolutionY(int gameResolutionY) {
            this.gameResolutionY = gameResolutionY;
            return this;
        }
        public LaunchSettings setJavaPath(String javaPath) {
            this.javaPath = javaPath;
            return this;
        }
        public LaunchSettings setJvmArguments(String jvmArguments) {
            this.jvmArguments = jvmArguments;
            return this;
        }

    }

    /**
     * Represents various settings regarding how the profile itself is configured.
     */
    public static class ProfileSettings {

        // JSON Properties
        @Since(1.0)
        private boolean global;
        @Since(1.0)
        @Nullable
        private String previewIconPath;

        /**
         * Constructs new profile settings with default settings.
         */
        public ProfileSettings() {
            this.global = false;
            this.previewIconPath = null;
        }

        // Getters
        public boolean isGlobal() {
            return global;
        }
        @Nullable
        public String getPreviewIconPath() {
            return previewIconPath;
        }

        // Setters
        public ProfileSettings setGlobal(boolean global) {
            this.global = global;
            return this;
        }
        public ProfileSettings setPreviewIconPath(String previewIconPath) {
            this.previewIconPath = previewIconPath;
            return this;
        }

    }

}
