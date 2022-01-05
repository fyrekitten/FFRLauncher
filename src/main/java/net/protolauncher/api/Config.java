package net.protolauncher.api;

import com.google.gson.annotations.Since;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Contains various configurable values that determine how the API and the launcher behaves.
 * User settings are also contained within this class.
 */
public class Config {

    // JSON Properties
    /**
     * The UUID (as a string) used by Mojang to identify this instance of the launcher for
     * frequent requests and authentication.
     */
    @Since(1.0)
    private String clientToken;
    /**
     * The UUID (as a string) of the user currently logged in to the launcher.
     */
    @Since(1.0)
    @Nullable
    private String currentUserUuid;
    /**
     * The UUID (as a string) of the profile currently selected in the launcher.
     */
    @Since(1.0)
    @Nullable
    private String currentProfileUuid;
    /**
     * The {@link Instant} in which the manifest was last updated.
     */
    @Since(1.0)
    private Instant lastManifestUpdate;
    /**
     * The maximum {@link Duration} that the manifest can be in age.
     */
    @Since(1.0)
    private Duration maxManifestAge;
    /**
     * Contains a list of endpoints used throughout the entire launcher.
     */
    @Since(1.0)
    private Endpoints endpoints;
    /**
     * Whether to pretty print all JSON parsing or not.
     */
    @Since(1.0)
    private boolean prettyPrint;
    /**
     * Whether to validate all downloaded files against their SHA1 or not.
     */
    @Since(1.0)
    private boolean validate;
    /**
     * Whether this is the first launch of the launcher or not.
     */
    @Since(1.0)
    private boolean firstLaunch;

    /**
     * Constructs a new settings with default values.
     */
    public Config() {
        this.reset();
    }

    // Getters
    public String getClientToken() {
        return clientToken;
    }
    @Nullable
    public String getCurrentUserUuid() {
        return currentUserUuid;
    }
    @Nullable
    public String getCurrentProfileUuid() {
        return currentProfileUuid;
    }
    public Instant getLastManifestUpdate() {
        return lastManifestUpdate;
    }
    public Duration getMaxManifestAge() {
        return maxManifestAge;
    }
    public Endpoints getEndpoints() {
        return endpoints;
    }
    public boolean shouldPrettyPrint() {
        return prettyPrint;
    }
    public boolean shouldValidate() {
        return validate;
    }
    public boolean isFirstLaunch() {
        return firstLaunch;
    }

    // Setters
    public Config setClientToken(String clientToken) {
        this.clientToken = clientToken;
        return this;
    }
    public Config setCurrentUserUuid(String currentUserUuid) {
        this.currentUserUuid = currentUserUuid;
        return this;
    }
    public Config setCurrentProfileUuid(String currentProfileUuid) {
        this.currentProfileUuid = currentProfileUuid;
        return this;
    }
    public Config setLastManifestUpdate(Instant lastManifestUpdate) {
        this.lastManifestUpdate = lastManifestUpdate;
        return this;
    }
    public Config setMaxManifestAge(Duration maxManifestAge) {
        this.maxManifestAge = maxManifestAge;
        return this;
    }
    public Config setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        return this;
    }
    public Config setValidate(boolean validate) {
        this.validate = validate;
        return this;
    }
    public Config setFirstLaunch(boolean firstLaunch) {
        this.firstLaunch = firstLaunch;
        return this;
    }

    /**
     * Resets the settings to their default values.
     *
     * @return The settings but with their values reset.
     */
    public Config reset() {
        this.clientToken = UUID.randomUUID().toString().replace("-", "");
        this.currentUserUuid = null;
        this.currentProfileUuid = null;
        this.lastManifestUpdate = Instant.MIN;
        this.maxManifestAge = Duration.ofHours(12);
        this.endpoints = new Endpoints();
        this.prettyPrint = false;
        this.validate = true;
        this.firstLaunch = true;
        return this;
    }

    /**
     * Represents all the possible endpoints that the API could need to use.
     */
    public static class Endpoints {

        // JSON Properties
        /**
         * The URL to the <a href="https://minecraft.fandom.com/wiki/Version_manifest.json">Mojang Version Manifest</a>
         */
        @Since(1.0)
        private URL versionManifest;
        /**
         * The URL to the Mojang Asset API.
         */
        @Since(1.0)
        private URL assetApi;
        /**
         * The URL to the official <a href="https://wiki.vg/Mojang_API">Mojang API</a>.
         */
        @Since(1.0)
        private URL mojangApi;
        /**
         * The URL to the official <a href="https://wiki.vg/Mojang_API">Minecraft Services API</a>.
         */
        @Since(1.0)
        private URL minecraftServicesApi;
        /**
         * The URL to the <a href="https://wiki.vg/Authentication">Mojang Yggdrasil Authentication System</a>.
         */
        @Since(1.0)
        private URL yggdrasilApi;
        /**
         * Various endpoints relating to the <a href="https://wiki.vg/Microsoft_Authentication_Scheme">Microsoft Authentication Scheme</a>
         */
        @Since(1.0)
        private MicrosoftApi microsoftApi;
        /**
         * The endpoint for resolving Mojang Avatar URLs.
         */
        @Since(1.0)
        private URL avatarApi;
        /**
         * A direct download to Java 8 for Win 32 systems.
         */
        @Since(1.0)
        private URL java8Win32;
        /**
         * A direct download to Java 8 for Win 64 systems.
         */
        @Since(1.0)
        private URL java8Win64;
        /**
         * A direct download to Java 8 for MacOS systems.
         */
        @Since(1.0)
        private URL java8Mac;
        /**
         * A direct download to Java 8 for Linux systems.
         */
        @Since(1.0)
        private URL java8Linux;

        /**
         * Constructs new endpoints with default values.
         */
        private Endpoints() {
            try {
                this.versionManifest = new URL("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json");
                this.assetApi = new URL("https://resources.download.minecraft.net/");
                this.mojangApi = new URL("https://api.mojang.com/");
                this.minecraftServicesApi = new URL("https://api.minecraftservices.com/");
                this.yggdrasilApi = new URL("https://authserver.mojang.com/");
                this.microsoftApi = new MicrosoftApi();
                this.avatarApi = new URL("https://minotar.net/helm/%uuid%/256");
                this.java8Win32 = new URL("https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u312-b07/OpenJDK8U-jre_x86-32_windows_hotspot_8u312b07.zip");
                this.java8Win64 = new URL("https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u312-b07/OpenJDK8U-jre_x64_windows_hotspot_8u312b07.zip");
                this.java8Mac = new URL("https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u312-b07/OpenJDK8U-jre_x64_mac_hotspot_8u312b07.tar.gz");
                this.java8Linux = new URL("https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u312-b07/OpenJDK8U-jre_x64_linux_hotspot_8u312b07.tar.gz");
            } catch (MalformedURLException e) {
                // Somebody really screwed up if this happens.
                e.printStackTrace();
                System.exit(1);
            }
        }

        // Getters
        public URL getVersionManifest() {
            return versionManifest;
        }
        public URL getAssetApi() {
            return assetApi;
        }
        public URL getMojangApi() {
            return mojangApi;
        }
        public URL getMinecraftServicesApi() {
            return minecraftServicesApi;
        }
        public URL getYggdrasilApi() {
            return yggdrasilApi;
        }
        public MicrosoftApi getMicrosoftApi() {
            return microsoftApi;
        }
        public URL getAvatarApi() {
            return avatarApi;
        }
        public URL getJava8Win32() {
            return java8Win32;
        }
        public URL getJava8Win64() {
            return java8Win64;
        }
        public URL getJava8Mac() {
            return java8Mac;
        }
        public URL getJava8Linux() {
            return java8Linux;
        }

        /**
         * Represents the various components needed to represent the Microsoft API endpoint.
         */
        public static class MicrosoftApi {

            // JSON Properties
            /**
             * The Client ID for the Azure application.
             */
            @Since(1.0)
            private String clientId;
            /**
             * The redirect URL for the Azure application.
             */
            @Since(1.0)
            private URL redirectUrl;
            /**
             * The OAuth2 authorization URL.
             */
            @Since(1.0)
            private URL oauthAuthorizeUrl;
            /**
             * The OAUth2 token URL.
             */
            @Since(1.0)
            private URL oauthTokenUrl;
            /**
             * The Xbox Live authentication URL.
             */
            @Since(1.0)
            private URL xblUrl;
            /**
             * The XSTS authentication URL.
             */
            @Since(1.0)
            private URL xstsUrl;

            /**
             * Constructs a new MicrosoftApi with default values.
             */
            private MicrosoftApi() {
                try {
                    this.clientId = "570b4885-8053-4442-a511-c6cc8df24dcb";
                    this.redirectUrl = new URL("https://login.microsoftonline.com/common/oauth2/nativeclient");
                    this.oauthAuthorizeUrl = new URL("https://login.live.com/oauth20_authorize.srf");
                    this.oauthTokenUrl = new URL("https://login.live.com/oauth20_token.srf");
                    this.xblUrl = new URL("https://user.auth.xboxlive.com/user/authenticate");
                    this.xstsUrl = new URL("https://xsts.auth.xboxlive.com/xsts/authorize");
                } catch (MalformedURLException e) {
                    // Somebody really screwed up if this happens.
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            // Getters
            public String getClientId() {
                return clientId;
            }
            public URL getRedirectUrl() {
                return redirectUrl;
            }
            public URL getOauthAuthorizeUrl() {
                return oauthAuthorizeUrl;
            }
            public URL getOauthTokenUrl() {
                return oauthTokenUrl;
            }
            public URL getXblUrl() {
                return xblUrl;
            }
            public URL getXstsUrl() {
                return xstsUrl;
            }

        }

    }

    /**
     * Represents a list of file locations used throughout the launcher.
     * This class is not really a configurable value, as all locations are hard-coded.
     */
    public static class FileLocation {

        // Launcher
        public static final Path LAUNCHER_FOLDER = Path.of("launcher/");
        public static final Path CACHE_FOLDER = LAUNCHER_FOLDER.resolve("cache/");
        public static final Path CONFIG = LAUNCHER_FOLDER.resolve("config.json");
        public static final Path PROFILES = LAUNCHER_FOLDER.resolve("profiles.json");
        public static final Path USERS = LAUNCHER_FOLDER.resolve("users.json");
        public static final Path JAVA_8_FOLDER = LAUNCHER_FOLDER.resolve("runtime/jre-1.8/");

        // Profiles
        public static final Path PROFILES_FOLDER = Path.of("profiles/");
        public static final Path SHARED_FOLDER = PROFILES_FOLDER.resolve("shared/");

        // Mojang
        public static final Path MOJANG_FOLDER = Path.of("mojang/");
        public static final Path VERSION_MANIFEST = MOJANG_FOLDER.resolve("version_manifest.json");
        public static final Path VERSIONS_FOLDER = MOJANG_FOLDER.resolve("versions/");
        public static final Path LIBRARIES_FOLDER = MOJANG_FOLDER.resolve("libraries/");
        public static final Path ASSETS_FOLDER = MOJANG_FOLDER.resolve("assets/");

    }

}
