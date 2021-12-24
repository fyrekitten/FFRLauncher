package net.protolauncher.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.protolauncher.api.Config.Endpoints;
import net.protolauncher.api.Config.FileLocation;
import net.protolauncher.api.User.MicrosoftInfo;
import net.protolauncher.api.function.DownloadProgressConsumer;
import net.protolauncher.api.function.StepInfoConsumer;
import net.protolauncher.api.function.StepProgressConsumer;
import net.protolauncher.api.gson.DurationTypeAdapter;
import net.protolauncher.api.gson.InstantTypeAdapter;
import net.protolauncher.mojang.Artifact;
import net.protolauncher.mojang.asset.Asset;
import net.protolauncher.mojang.asset.AssetIndex;
import net.protolauncher.mojang.auth.MicrosoftAuth;
import net.protolauncher.mojang.auth.MicrosoftAuth.MicrosoftResponse;
import net.protolauncher.mojang.auth.MicrosoftAuth.MinecraftResponse;
import net.protolauncher.mojang.auth.MicrosoftAuth.XboxLiveResponse;
import net.protolauncher.mojang.auth.MojangAPI;
import net.protolauncher.mojang.auth.Yggdrasil;
import net.protolauncher.mojang.library.Library;
import net.protolauncher.mojang.rule.Action;
import net.protolauncher.mojang.rule.Rule;
import net.protolauncher.mojang.version.Version;
import net.protolauncher.mojang.version.VersionInfo;
import net.protolauncher.mojang.version.VersionManifest;
import net.protolauncher.mojang.version.VersionType;
import net.protolauncher.util.Network;
import net.protolauncher.util.SystemInfo;
import net.protolauncher.util.Validation;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.GZIPInputStream;

/**
 * The ProtoLauncher API. The structure of this class may seem weird to some, so here is some explanation:
 */
public class ProtoLauncher {

    // Logging
    private final Logger logger;

    // Launcher Variables
    private Gson gson;
    private Config config;
    private List<User> users;
    private HashMap<String, List<Profile>> profiles;

    // Mojang Variables
    @Nullable
    private VersionManifest versionManifest;
    private MojangAPI mojangApi;
    private Yggdrasil yggdrasil;
    private MicrosoftAuth microsoftAuth;

    /**
     * Constructs a new ProtoLauncher API as well as the GSON builder for it.
     */
    public ProtoLauncher() {
        // Prepare logger
        logger = LogManager.getLogger("ProtoLauncher");
        logger.debug("Logger created. Preparing ProtoLauncher API...");

        // Create a new GSON builder
        GsonBuilder builder = new GsonBuilder();

        // Set needed options
        builder.serializeNulls();
        builder.disableHtmlEscaping();
        builder.setVersion(1.0);
        builder.registerTypeAdapter(Instant.class, new InstantTypeAdapter());
        builder.registerTypeAdapter(Duration.class, new DurationTypeAdapter());

        // Create gson
        gson = builder.create();

        // Create new configuration
        config = new Config();

        // Prepare the lists
        users = new ArrayList<>();
        profiles = new HashMap<>();

        // Prepare mojang api
        mojangApi = new MojangAPI(gson, config.getEndpoints().getMojangApi().toString(), config.getEndpoints().getMinecraftServicesApi().toString());

        // Prepare yggdrasil
        yggdrasil = new Yggdrasil(gson, config.getEndpoints().getYggdrasilApi().toString(), config.getClientToken());

        // Prepare microsoft auth
        Endpoints.MicrosoftApi microsoftApiEndpoints = config.getEndpoints().getMicrosoftApi();
        microsoftAuth = new MicrosoftAuth(
            gson,
            microsoftApiEndpoints.getClientId(),
            microsoftApiEndpoints.getRedirectUrl(),
            microsoftApiEndpoints.getOauthAuthorizeUrl().toString(),
            microsoftApiEndpoints.getOauthTokenUrl().toString(),
            microsoftApiEndpoints.getXblUrl().toString(),
            microsoftApiEndpoints.getXstsUrl().toString(),
            config.getEndpoints().getMinecraftServicesApi().toString()
        );
        logger.debug("ProtoLauncher API ready.");
    }

    // Getters
    public Logger getLogger() {
        return logger;
    }
    public Config getConfig() {
        return config;
    }
    public Gson getGson() {
        return gson;
    }
    @Nullable
    public VersionManifest getVersionManifest() {
        return versionManifest;
    }
    public Yggdrasil getYggdrasil() {
        return yggdrasil;
    }
    public MicrosoftAuth getMicrosoftAuth() {
        return microsoftAuth;
    }

    /**
     * Loads the {@link Config}, creating a new one if one does not already exist.
     *
     * @throws IOException Thrown if loading the configuration goes horribly wrong.
     */
    public void loadConfig() throws IOException {
        logger.debug("Loading configuration...");
        Path path = FileLocation.CONFIG;

        // Check if one exists, and if not, generate a new one
        if (!Files.exists(path)) {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            this.saveConfig();
        } else {
            config = gson.fromJson(Files.newBufferedReader(path), Config.class);
        }

        // Apply gson configuration values
        GsonBuilder gsonBuilder = gson.newBuilder();
        if (config.shouldPrettyPrint()) {
            gsonBuilder.setPrettyPrinting();
        }
        gson = gsonBuilder.create();

        // Update Mojang API
        mojangApi.setGson(gson);
        mojangApi.setMojangApi(config.getEndpoints().getMojangApi().toString());
        mojangApi.setMinecraftServicesApi(config.getEndpoints().getMinecraftServicesApi().toString());

        // Update Yggdrasil
        yggdrasil.setGson(gson);
        yggdrasil.setApi(config.getEndpoints().getYggdrasilApi().toString());
        yggdrasil.setClientToken(config.getClientToken());

        // Update Microsoft Auth
        Endpoints.MicrosoftApi microsoftApiEndpoints = config.getEndpoints().getMicrosoftApi();
        microsoftAuth.setGson(gson);
        microsoftAuth.setClientId(microsoftApiEndpoints.getClientId());
        microsoftAuth.setRedirectUrl(microsoftApiEndpoints.getRedirectUrl());
        microsoftAuth.setOauthUrl(microsoftApiEndpoints.getOauthAuthorizeUrl().toString());
        microsoftAuth.setOauthTokenUrl(microsoftApiEndpoints.getOauthTokenUrl().toString());
        microsoftAuth.setXblUrl(microsoftApiEndpoints.getXblUrl().toString());
        microsoftAuth.setXstsUrl(microsoftApiEndpoints.getXstsUrl().toString());
        microsoftAuth.setMcsUrl(config.getEndpoints().getMinecraftServicesApi().toString());
        logger.debug("Configuration loaded.");
    }

    /**
     * Saves the configuration, presumably after somebody's changed it.
     *
     * @throws IOException Thrown if saving the configuration goes horribly wrong.
     */
    public void saveConfig() throws IOException {
        logger.debug("Saving configuration...");
        Path path = FileLocation.CONFIG;
        Files.writeString(path, gson.toJson(config), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        logger.debug("Configuration saved.");
    }

    /**
     * Loads the list of {@link User}s, saving an empty list if the file does not already exist.
     *
     * @throws IOException Thrown if loading the users list goes horribly wrong.
     */
    public void loadUsers() throws IOException {
        logger.debug("Loading users...");
        Path path = FileLocation.USERS;

        // Check if it exists, and if not, make a new list
        if (!Files.exists(path)) {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            this.saveUsers();
        } else {
            users = gson.fromJson(Files.newBufferedReader(path), new TypeToken<List<User>>() { }.getType());
        }
        logger.debug("Users loaded.");
    }

    /**
     * Saves the users list, presumably after somebody's changed it.
     *
     * @throws IOException Thrown if saving the users list goes horribly wrong.
     */
    public void saveUsers() throws IOException {
        logger.debug("Saving users...");
        Path path = FileLocation.USERS;
        Files.writeString(path, gson.toJson(users), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        logger.debug("Users saved.");
    }

    /**
     * Returns the size of the user's array.
     * @return The size of the user's array.
     */
    public int getUserCount() {
        return users.size();
    }

    /**
     * Attempts to get a user by the provided uuid.
     * @param uuid The UUID to get.
     * @return The {@link User} or null if not found.
     */
    @Nullable
    public User getUser(String uuid) {
        return users.stream().filter(user -> user.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    /**
     * Searches the user list to find the user marked as the current user via the configuration.
     * @return The current {@link User} or null if they do not exist.
     */
    @Nullable
    public User getCurrentUser() {
        String currentUserUuid = config.getCurrentUserUuid();
        if (currentUserUuid == null) {
            return null;
        } else {
            return this.getUser(currentUserUuid);
        }
    }

    /**
     * Returns the entire list of users.
     * @return The entire list of users.
     */
    public List<User> getAllUsers() {
        return users;
    }

    /**
     * Adds the user to the launcher and switches to it.
     *
     * @param user The {@link User} to add.
     * @param makeDefaultProfiles Whether to create the default profiles for this user or not.
     * @throws IOException Thrown if something goes wrong saving or switching the user.
     */
    public void addUser(User user, boolean makeDefaultProfiles) throws IOException {
        logger.debug("Adding a new user: " + user.getUsername() + " (" + user.getUuid() + ")");

        // Check for existing user, update details
        User existing = this.getUser(user.getUuid());
        if (existing != null) {
            existing.setUsername(user.getUsername());
            existing.setMicrosoftInfo(user.getMicrosoftInfo());
            existing.setUserProperties(user.getUserProperties());
            existing.setAccessToken(user.getAccessToken());
            logger.debug("User " + existing.getUsername() + " (" + existing.getUuid() + ") already exists and has been updated.");
            this.saveUsers();
            this.switchUser(null);
            this.switchUser(existing);
            return;
        }

        // Add user
        users.add(user);
        this.saveUsers();

        // Make default profiles
        if (makeDefaultProfiles) {
            this.createLatestSnapshotProfile(user);
            this.createLatestReleaseProfile(user);
        }

        // Switch user
        logger.debug("User added.");
        this.switchUser(user);
    }

    /**
     * Logs a user in using the provided username and password, then adds them to the launcher and switches to them.
     *
     * @param username The username.
     * @param password The password.
     * @return A new {@link User}.
     * @throws IOException Thrown if anything goes wrong in the login, creation, or switching process.
     */
    public User addUserMojang(String username, String password) throws IOException {
        logger.debug("Performing Mojang login...");

        // Fetch Yggdrasil response
        Yggdrasil.Response response = yggdrasil.authenticate(username, password);
        if (response.getError() != null) {
            throw new IOException(response.getErrorMessage());
        }

        // Parse properties
        String properties;
        if (response.getUser() != null && response.getUser().getProperties() != null){
            properties = yggdrasil.deserializeProperties(response.getUser().getProperties());
        } else {
            properties = "{}";
        }
        logger.debug("Login completed successfully.");

        // Create and add a new user
        User user = new User(response.getSelectedProfile().getName(), response.getSelectedProfile().getId(), properties, response.getAccessToken());
        this.addUser(user, true);

        // Return the user
        return user;
    }

    /**
     * Logs a user in using the provided authCode as fetched from a Microsoft OAuth2 login prompt,
     * then adds them to the launcher and switches to them.
     *
     * @param authCode The authCode, as fetched from a Microsoft OAuth2 login prompt.
     * @return A new {@link User}.
     * @throws IOException Thrown if anything goes wrong in the login, authentication, creation, or switching process.
     */
    public User addUserMicrosoft(String authCode) throws IOException {
        logger.debug("Performing Microsoft login...");

        // Authenticate with Microsoft
        logger.debug("Authenticating with Microsoft...");
        MicrosoftResponse microsoftResponse = microsoftAuth.authenticateMicrosoft(authCode);
        if (microsoftResponse.getError() != null) {
            throw new IOException("Internal error while authenticating with Microsoft! " + microsoftResponse.getErrorDescription());
        }

        // Authenticate with Xbox Live
        logger.debug("Authenticating with Xbox Live...");
        XboxLiveResponse xboxLiveResponse;
        try {
            xboxLiveResponse = microsoftAuth.authenticateXboxLive(microsoftResponse.getAccessToken());
        } catch (Exception e) {
            if (e.getMessage().startsWith("401")) {
                microsoftResponse = microsoftAuth.refreshMicrosoft(microsoftResponse.getRefreshToken());
                if (microsoftResponse.getError() != null) {
                    throw new IOException("Internal error while refreshing the access token! " + microsoftResponse.getErrorDescription());
                } else {
                    xboxLiveResponse = microsoftAuth.authenticateXboxLive(microsoftResponse.getAccessToken());
                }
            } else {
                throw e;
            }
        }
        String uhs = xboxLiveResponse.getDisplayClaims().getXui()[0].getUhs();

        // Authenticate with XSTS
        logger.debug("Authenticating with XSTS...");
        XboxLiveResponse xstsResponse = microsoftAuth.authenticateXsts(xboxLiveResponse.getToken());

        // Authenticate with Minecraft
        logger.debug("Authenticating with Minecraft...");
        MinecraftResponse minecraftResponse = microsoftAuth.authenticateMinecraft(xstsResponse.getToken(), uhs);

        // Create information
        MicrosoftInfo microsoftInfo = new MicrosoftInfo(
            microsoftResponse.getAccessToken(),
            microsoftResponse.getRefreshToken(),
            xboxLiveResponse.getToken(),
            uhs,
            xstsResponse.getToken(),
            System.currentTimeMillis() + (Long.parseLong(minecraftResponse.getExpiresIn()) * 1000 * 60)
        );

        // Ensure game ownership
        logger.debug("Verifying game ownership...");
        if (!microsoftAuth.verifyOwnership(minecraftResponse.getAccessToken())) {
            throw new IOException("User does not own the game!");
        }

        // Get profile
        logger.debug("Fetching user profile...");
        MojangAPI.ProfileInformationResponse profile = mojangApi.getProfileInformation(minecraftResponse.getAccessToken());
        logger.debug("Login completed successfully.");

        // Create and add a new user
        User user = new User(profile.getName(), profile.getId(), "{}", minecraftResponse.getAccessToken());
        user.setMicrosoftInfo(microsoftInfo);
        this.addUser(user, true);

        // Return the user
        return user;
    }

    /**
     * Refreshes a user's Microsoft login information.
     *
     * @param user The user to refresh login info for.
     * @return null if the refresh failed, otherwise the user with an updated {@link MicrosoftInfo} and accessToken.
     * @throws IOException Thrown if something goes wrong with a refresh request.
     */
    @Nullable
    public User refreshUserMicrosoft(User user) throws IOException {
        logger.debug("Refreshing Microsoft login for " + user.getUsername() + " (" + user.getUuid() + ")");

        // Get Microsoft info
        MicrosoftInfo mci = user.getMicrosoftInfo();
        if (mci == null) {
            return null;
        }

        // Authenticate with Microsoft
        logger.debug("Refreshing Microsoft...");
        MicrosoftResponse microsoftResponse = microsoftAuth.refreshMicrosoft(mci.getRefreshToken());
        if (microsoftResponse.getError() != null) {
            logger.debug("Microsoft refresh failed, user is invalid!");
            return null;
        }

        // Authenticate with Xbox Live
        logger.debug("Refreshing Xbox Live...");
        XboxLiveResponse xboxLiveResponse;
        try {
            xboxLiveResponse = microsoftAuth.authenticateXboxLive(microsoftResponse.getAccessToken());
        } catch (Exception e) {
            logger.debug("Microsoft or Xbox Live refresh failed, user is invalid!");
            return null;
        }
        String uhs = xboxLiveResponse.getDisplayClaims().getXui()[0].getUhs();

        // Authenticate with XSTS
        logger.debug("Refreshing XSTS...");
        XboxLiveResponse xstsResponse = microsoftAuth.authenticateXsts(xboxLiveResponse.getToken());

        // Authenticate with Minecraft
        logger.debug("Refreshing Minecraft...");
        MinecraftResponse minecraftResponse = microsoftAuth.authenticateMinecraft(xstsResponse.getToken(), uhs);

        // Update Microsoft Login Information
        mci.setAccessToken(microsoftResponse.getAccessToken());
        mci.setRefreshToken(microsoftResponse.getRefreshToken());
        mci.setXblToken(xboxLiveResponse.getToken());
        mci.setXblUhs(uhs);
        mci.setXstsToken(xstsResponse.getToken());
        mci.setDateExpires(System.currentTimeMillis() + (Long.parseLong(minecraftResponse.getExpiresIn()) * 1000 * 60));
        user.setMicrosoftInfo(mci);

        // Update access token
        user.setAccessToken(minecraftResponse.getAccessToken());
        logger.debug("Microsoft login refreshed.");

        // Return the updated user
        return user;
    }

    /**
     * Switches the launcher from one current user to another.
     *
     * @param user The user to switch to.
     * @throws IOException Thrown if something goes wrong switching users.
     */
    public void switchUser(@Nullable User user) throws IOException {
        if (user != null) {
            logger.debug("Switching users to " + user.getUsername() + " (" + user.getUuid() + ")...");
        } else {
            logger.debug("Switching to no users...");
        }

        // If the user is null, remove the current user
        // Otherwise, switch users
        if (user == null) {
            config.setCurrentUserUuid(null);
            this.switchProfile(null);
        } else {
            config.setCurrentUserUuid(user.getUuid());

            // Find last launched profile
            List<Profile> userProfiles = this.getProfiles(user.getUuid());
            if (userProfiles != null) {
                Profile profile = userProfiles.stream().max(Comparator.comparing(Profile::getLastLaunched)).orElse(null);

                // Switch to profile
                this.switchProfile(profile);

                // Check latest profiles
                this.checkLatestProfiles(user.getUuid());
            } else {
                this.switchProfile(null);
            }
        }
        logger.debug("User switched.");

        // Save config
        this.saveConfig();
    }

    /**
     * Removes the given user from the launcher and switches to the next possible user.
     *
     * @param user The user to remove.
     * @throws IOException Thrown if removing the user or switching the current user goes wrong.
     */
    public void removeUser(User user) throws IOException {
        logger.debug("Removing user " + user.getUsername() + " (" + user.getUuid() + ")...");

        // Remove the user
        if (user.getMicrosoftInfo() == null) {
            yggdrasil.invalidate(user.getAccessToken());
        }
        users.remove(user);
        logger.debug("User removed.");
        this.saveUsers();

        // Switch to the next possible user
        if (users.size() > 0) {
            this.switchUser(users.get(0));
        } else {
            this.switchUser(null);
        }
    }

    /**
     * Validates the given user's profile.
     * @param uuid The UUID of the user to validate.
     * @return True if the user is still valid, false if not.
     */
    public boolean validateUser(String uuid) throws IOException {
        // Fetch the user
        User user = this.getUser(uuid);
        if (user == null) {
            return false;
        }

        logger.debug("Validating user " + user.getUsername() + " (" + user.getUuid() + ")...");

        // Track whether we changed something about the user or not.
        boolean changed = false;

        // If the user is a Mojang account, check against the access token
        if (user.getMicrosoftInfo() == null) {
            logger.debug("Validating Mojang login...");
            boolean isValid = yggdrasil.validate(user.getAccessToken());
            // If it's not valid, attempt to refresh it
            if (!isValid) {
                logger.debug("Mojang login is invalid, attempting to refresh...");
                try {
                    Yggdrasil.Response response = yggdrasil.refresh(user.getAccessToken());
                    if (response.getError() != null) {
                        logger.debug("Mojang refresh failed, user is invalid!");
                        return false;
                    } else {
                        logger.debug("Mojang login refreshed.");
                        changed = true;
                        user.setAccessToken(response.getAccessToken());
                    }
                } catch (Exception e) {
                    logger.debug("Mojang refresh failed, user is invalid!");
                    return false;
                }
            }
        } else {
            logger.debug("Validating Microsoft login...");
            MicrosoftInfo mci = user.getMicrosoftInfo();
            boolean isValid = System.currentTimeMillis() < mci.getDateExpires();
            // If it's not valid, attempt to refresh it (this process is a pain)
            if (!isValid) {
                logger.debug("Microsoft login is invalid, attempting to refresh...");
                User updatedUser = this.refreshUserMicrosoft(user);
                if (updatedUser == null) {
                    logger.debug("Microsoft refresh failed, user is invalid.");
                    return false;
                }
                changed = true;
                user.setMicrosoftInfo(updatedUser.getMicrosoftInfo());
                user.setAccessToken(updatedUser.getAccessToken());
            }

            // Verify game ownership
            try {
                isValid = microsoftAuth.verifyOwnership(user.getAccessToken());
            } catch (IOException e) {
                logger.debug("Microsoft login is invalid, attempting to refresh...");
                User updatedUser = this.refreshUserMicrosoft(user);
                if (updatedUser == null) {
                    logger.debug("Microsoft refresh failed, user is invalid.");
                    return false;
                }
                changed = true;
                user.setMicrosoftInfo(updatedUser.getMicrosoftInfo());
                user.setAccessToken(updatedUser.getAccessToken());
                try {
                    isValid = microsoftAuth.verifyOwnership(user.getAccessToken());
                } catch (IOException e2) {
                    logger.debug("Failed to verify ownership, user is invalid.");
                    return false;
                }
            }
            if (!isValid) {
                logger.debug("User does not own the game anymore and is therefore invalid!");
                return false;
            }
        }

        // Fetch profile and update username
        logger.debug("Fetching profile...");
        MojangAPI.ProfileInformationResponse profile = mojangApi.getProfileInformation(user.getAccessToken());
        if (!user.getUsername().equals(profile.getName())) {
            logger.debug("Username changed from " + user.getUsername() + " to " + profile.getName() + "!");
            changed = true;
            user.setUsername(profile.getName());
        }

        // If we changed, save
        if (changed) {
            logger.debug("User changes detected.");
            this.saveUsers();
        }

        // Return true
        logger.debug("User is valid.");
        return true;
    }

    /**
     * Fetches the file path to a user's avatar.
     *
     * @param uuid The uuid of the user.
     * @return The file path.
     * @throws IOException Thrown if something goes wrong fetching the avatar.
     */
    public Path fetchUserAvatar(@Nullable String uuid) throws IOException {
        String id = uuid != null ? uuid : "MHF_Steve";
        Path location = FileLocation.CACHE_FOLDER.resolve("avatars/" + id + ".png");
        if (!Files.exists(location)) {
            Files.createDirectories(location.getParent());
            URL endpoint = new URL(config.getEndpoints().getAvatarApi().toString().replace("%uuid%", id));
            Network.download(endpoint, location);
        }
        return location;
    }

    /**
     * Loads the map of {@link Profile}s, saving an empty map if the file does not already exist.
     *
     * @throws IOException Thrown if loading the profiles map goes horribly wrong.
     */
    public void loadProfiles() throws IOException {
        logger.debug("Loading profiles...");
        Path path = FileLocation.PROFILES;

        // Check if it exists, and if not, make a new list
        if (!Files.exists(path)) {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            this.saveProfiles();
        } else {
            profiles = gson.fromJson(Files.newBufferedReader(path), new TypeToken<HashMap<String, List<Profile>>>() { }.getType());
        }
        logger.debug("Profiles loaded.");
    }

    /**
     * Saves the profiles map, presumably after somebody's changed it.
     *
     * @throws IOException Thrown if saving the profiles map goes horribly wrong.
     */
    public void saveProfiles() throws IOException {
        logger.debug("Saving profiles...");
        Path path = FileLocation.PROFILES;
        Files.writeString(path, gson.toJson(profiles), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        logger.debug("Profiles saved.");
    }

    /**
     * Returns the total size of all profiles, adding each user's list together.
     *
     * @return The size of all profiles.
     */
    public int getProfileCount() {
        return profiles.values().stream().map(List::size).reduce(0, Integer::sum);
    }

    /**
     * Gets a list of profiles by the provided owner.
     *
     * @param owner The UUID of the {@link User} whose profiles to get.
     * @return A list of {@link Profile}s owned by the given user, or null if there are none.
     */
    @Nullable
    public List<Profile> getProfiles(String owner) {
        return profiles.getOrDefault(owner, null);
    }

    /**
     * Gets a profile by the provided owner and the profile UUID.
     *
     * @param owner The UUID of the {@link User} who owns this profile.
     * @param uuid The UUID of the profile.
     * @return The {@link Profile} or null if not found.
     */
    public Profile getProfile(String owner, String uuid) {
        List<Profile> userProfiles = this.getProfiles(owner);
        if (userProfiles == null) {
            return null;
        }
        return userProfiles.stream().filter(profile -> profile.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    /**
     * Searches the profiles map to find the profile marked as the current profile via the configuration.
     * If there is not a current user, will short-circut and immediately return null.
     *
     * @return The current {@link Profile} or null if it does not exist.
     */
    @Nullable
    public Profile getCurrentProfile() {
        String currentUserUuid = config.getCurrentUserUuid();
        if (currentUserUuid == null) {
            return null;
        }
        String currentProfileUuid = config.getCurrentProfileUuid();
        if (currentProfileUuid == null) {
            return null;
        }
        return this.getProfile(currentUserUuid, currentProfileUuid);
    }

    /**
     * Adds the profile to the launcher and switches to it.
     * @param profile The {@link Profile} to add.
     * @throws IOException Thrown if something goes wrong saving or switching the profile.
     */
    public void addProfile(Profile profile) throws IOException {
        logger.debug("Adding a new profile: " + profile.getName());

        // Get existing profiles
        List<Profile> userProfiles = this.getProfiles(profile.getOwner());
        if (userProfiles == null) {
            userProfiles = new ArrayList<>();
        }

        // Add to profiles
        userProfiles.add(0, profile);
        profiles.put(profile.getOwner(), userProfiles);
        this.saveProfiles();

        // Switch profile
        logger.debug("Profile added.");
        this.switchProfile(profile);
    }

    /**
     * Switches the launcher from one current profile to another.
     *
     * @param profile The profile to switch to.
     * @throws IOException Thrown if something goes wrong switching profiles.
     */
    public void switchProfile(@Nullable Profile profile) throws IOException {
        if (profile != null) {
            logger.debug("Switching profiles to " + profile.getName() + "...");
        } else {
            logger.debug("Switching to no profiles...");
        }

        // If the profile is null, remove the current profile
        // Otherwise, switch profiles
        if (profile == null) {
            config.setCurrentProfileUuid(null);
        } else {
            config.setCurrentProfileUuid(profile.getUuid());
        }
        logger.debug("Profile switched.");

        // Save config
        this.saveConfig();
    }

    /**
     * Removes the given profile from the launcher and switches to the next possible profile.
     *
     * @param profile The profile to remove.
     * @throws IOException Thrown if removing the profile or switching the current profile goes wrong.
     */
    public void removeProfile(Profile profile) throws IOException {
        logger.debug("Removing profile " + profile.getName() + "...");

        // Remove the profile
        List<Profile> userProfiles = this.getProfiles(profile.getOwner());
        if (userProfiles == null) {
            return;
        }
        userProfiles.removeIf(p -> p.getUuid().equals(profile.getUuid()));
        if (userProfiles.size() == 0) {
            profiles.remove(profile.getOwner());
        } else {
            profiles.put(profile.getOwner(), userProfiles);
        }
        logger.debug("Profile removed.");
        this.saveProfiles();

        // Switch to the next possible profile
        if (userProfiles.size() > 0) {
            this.switchProfile(userProfiles.get(0));
        } else {
            this.switchProfile(null);
        }
    }

    /**
     * Creates and saves the 'Latest Release' profile for the given user.
     *
     * @param owner The {@link User} that should own this profile.
     * @throws IOException Thrown if adding the profile goes wrong.
     * @return The created {@link Profile} or null if it failed.
     */
    @Nullable
    public Profile createLatestReleaseProfile(User owner) throws IOException {
        logger.debug("Creating latest release profile...");

        // We can't create it if the version manifest hasn't been loaded
        if (versionManifest == null) {
            return null;
        }

        // Attempt to find an existing profile
        List<Profile> userProfiles = this.getProfiles(owner.getUuid());
        if (userProfiles != null) {
            Profile existing = userProfiles.stream().filter(profile ->
                profile.getVersion().getType() == VersionType.RELEASE && profile.getVersion().isLatest()
            ).findFirst().orElse(null);
            if (existing != null) {
                return existing;
            }
        }

        // Create profile
        Profile profile = new Profile("Latest Release", versionManifest.getLatestRelease(), owner);
        profile.setVersion(profile.getVersion().setLatest(true));

        // Add profile
        this.addProfile(profile);

        // Return the profile
        return profile;
    }

    /**
     * Creates and saves the 'Latest Snapshot' profile for the given user.
     *
     * @param owner The {@link User} that should own this profile.
     * @throws IOException Thrown if adding the profile goes wrong.
     * @return The created {@link Profile} or null if it failed.
     */
    @Nullable
    public Profile createLatestSnapshotProfile(User owner) throws IOException {
        logger.debug("Creating latest snapshot profile...");

        // We can't create it if the version manifest hasn't been loaded
        if (versionManifest == null) {
            return null;
        }

        // Attempt to find an existing profile
        List<Profile> userProfiles = this.getProfiles(owner.getUuid());
        if (userProfiles != null) {
            Profile existing = userProfiles.stream().filter(profile ->
                profile.getVersion().getType() == VersionType.SNAPSHOT && profile.getVersion().isLatest()
            ).findFirst().orElse(null);
            if (existing != null) {
                return existing;
            }
        }

        // Force the version info the mark this as type snapshot (to prevent a latest release to mark this profile as existing)
        VersionInfo modifiedVersionInfo = versionManifest.getLatestSnapshot().copy();
        modifiedVersionInfo.setType(VersionType.SNAPSHOT);

        // Create profile
        Profile profile = new Profile("Latest Snapshot", modifiedVersionInfo, owner);
        profile.setVersion(profile.getVersion().setLatest(true));

        // Add profile
        this.addProfile(profile);

        // Return the profile
        return profile;
    }

    /**
     * Checks all the 'latest' profiles and updates their MCV if it is outdated for the given user.
     * Requires the version manifest to be loaded. See {@link ProtoLauncher#loadVersionManifest(DownloadProgressConsumer)}.
     *
     * @param owner The UUID of the {@link User} to check the latest profiles for.
     * @throws IOException Thrown if checking the latest profiles fails.
     */
    public void checkLatestProfiles(String owner) throws IOException {
        logger.debug("Checking latest profiles for version updates.");

        // We can't check if the version manifest hasn't been loaded
        if (versionManifest == null) {
            return;
        }

        // Get profiles
        List<Profile> userProfiles = this.getProfiles(owner);
        if (userProfiles == null) {
            return;
        }

        // Check loop
        boolean updated = false;
        for (int i = 0; i < userProfiles.size(); i++) {
            Profile profile = userProfiles.get(i);
            Profile.Version ver = profile.getVersion();
            if (!ver.isLatest()) {
                continue;
            }

            // Check type and update accordingly
            if (ver.getType() == VersionType.RELEASE) {
                VersionInfo latestReleaseInfo = versionManifest.getLatestRelease();
                if (!ver.getMinecraft().equals(latestReleaseInfo.getId())) {
                    logger.debug("Profile " + profile.getName() + " has been updated to " + latestReleaseInfo.getId());
                    updated = true;
                    profile.setVersion(ver.setVersion(latestReleaseInfo));
                    userProfiles.set(i, profile);
                }
            } else if (ver.getType() == VersionType.SNAPSHOT) {
                VersionInfo latestSnapshotInfo = versionManifest.getLatestSnapshot();
                if (!ver.getMinecraft().equals(latestSnapshotInfo.getId())) {
                    logger.debug("Profile " + profile.getName() + " has been updated to " + latestSnapshotInfo.getId());
                    updated = true;
                    VersionInfo latestSnapshotInfoCopy = versionManifest.getLatestSnapshot().copy();
                    latestSnapshotInfoCopy.setType(VersionType.SNAPSHOT);
                    profile.setVersion(ver.setVersion(latestSnapshotInfoCopy));
                    userProfiles.set(i, profile);
                }
            }
        }

        // Save if updated
        if (updated) {
            profiles.put(owner, userProfiles);
            this.saveProfiles();
        }
    }

    /**
     * Loads the {@link VersionManifest}, downloading it if necessary.
     *
     * @param downloadProgress Called to show the download progress.
     * @throws IOException Thrown if something goes wrong loading or downloading the version manifest.
     */
    public void loadVersionManifest(DownloadProgressConsumer downloadProgress) throws IOException {
        logger.debug("Loading version manifest...");
        URL url = config.getEndpoints().getVersionManifest();
        Path path = FileLocation.VERSION_MANIFEST;

        // Check if it needs to be downloaded and, if it does, then download it
        Instant nextManifestUpdate = config.getLastManifestUpdate().plus(config.getMaxManifestAge());
        if (!Files.exists(path) || Instant.now().isAfter(nextManifestUpdate)) {
            logger.debug("Version manifest update requested. Downloading...");
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            long size = Network.fetchFileSize(url);
            Network.download(url, path, progress -> downloadProgress.accept(size, progress));
            config.setLastManifestUpdate(Instant.now());
            this.saveConfig();
        }

        // Parse the manifest
        versionManifest = gson.fromJson(Files.newBufferedReader(path), VersionManifest.class);
        logger.debug("Version manifest loaded.");
    }

    /**
     * Downloads the version file from the given {@link VersionInfo}.
     *
     * @param info The information to download the version file from.
     * @param downloadProgress Called to show the download progress.
     * @return The loaded {@link Version}.
     * @throws IOException Thrown if something goes wrong loading or downloading the version.
     */
    public Version downloadVersion(VersionInfo info, DownloadProgressConsumer downloadProgress) throws IOException {
        logger.debug("Downloading version " + info.getId() + "...");
        String id = info.getId();
        Path folder = FileLocation.VERSIONS_FOLDER.resolve(id + "/");
        Path file = folder.resolve(id + ".json");

        // Check if it needs to be downloaded and, if it does, then download it
        if (!Files.exists(file)) {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            URL url = new URL(info.getUrl());
            long size = Network.fetchFileSize(url);
            Network.download(url, file, progress -> downloadProgress.accept(size, progress));
        }

        // Validate
        logger.debug("Validating...");
        if (config.shouldValidate() && !Validation.validate(file, info.getSha1())) {
            // TODO: Retry download.
            throw new IOException("Validation failed!");
        }

        // Load version
        logger.debug("Version loaded.");
        return gson.fromJson(Files.newBufferedReader(file), Version.class);
    }

    /**
     * Downloads the client JAR file for the given version.
     *
     * @param version The {@link Version} to download the client for.
     * @param downloadProgress Called to show the download progress.
     * @throws IOException Thrown if something goes wrong downloading the client.
     */
    public void downloadVersionClient(Version version, DownloadProgressConsumer downloadProgress) throws IOException {
        logger.debug("Downloading client for " + version.getId() + "...");
        String id = version.getId();
        Path folder = FileLocation.VERSIONS_FOLDER.resolve(id + "/");
        Path file = folder.resolve(id + ".jar");
        Artifact artifact = version.getDownloads().getClient();

        // Check if it needs to be downloaded and, if it does, then download it
        if (!Files.exists(file)) {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            URL url = new URL(artifact.getUrl());
            long size = artifact.getSize();
            Network.download(url, file, progress -> downloadProgress.accept(size, progress));
        }

        // Validate
        logger.debug("Validating...");
        if (config.shouldValidate() && !Validation.validate(file, artifact.getSha1())) {
            // TODO: Retry download.
            throw new IOException("Validation failed!");
        }
        logger.debug("Client downloaded.");
    }

    /**
     * Downloads Java 8 for the appropriate system platform.
     *
     * @param stepProgress The progress of the 'steps' of the download (download, then extraction).
     * @param downloadProgress Called to show the download progress.
     * @return The {@link Path} to the Java executable.
     * @throws IOException Thrown if something goes wrong downloading Java.
     * @throws ArchiveException Thrown if something goes wrong during the extraction process.
     */
    public Path downloadJava(StepProgressConsumer stepProgress, DownloadProgressConsumer downloadProgress) throws IOException, ArchiveException {
        logger.debug("Downloading Java 8...");
        final int totalSteps = 2;
        int currentStep = 0;
        stepProgress.accept(totalSteps, ++currentStep);
        Path folder = FileLocation.JAVA_8_FOLDER;
        Files.createDirectories(folder);

        // Fetch correct URL
        URL url;
        boolean isTarFile = false;
        switch (SystemInfo.OS_NAME) {
            case "windows":
                if (SystemInfo.OS_BIT.equals("32")) {
                    url = config.getEndpoints().getJava8Win32();
                } else {
                    url = config.getEndpoints().getJava8Win64();
                }
                break;
            case "mac":
                url = config.getEndpoints().getJava8Mac();
                isTarFile = true;
                break;
            case "linux":
                url = config.getEndpoints().getJava8Linux();
                isTarFile = true;
                break;
            default:
                throw new IOException("Unrecognized systems do not support auto-download of legacy Java.");
        }

        // Download file
        Path compressedFile = folder.resolve("jre-1.8" + (isTarFile ? ".tar.gz" : ".zip"));
        if (!Files.exists(compressedFile)) {
            long size = Network.fetchFileSize(url);
            Network.download(url, compressedFile, progress -> downloadProgress.accept(size, progress));
        }
        stepProgress.accept(totalSteps, ++currentStep);

        // Extract file
        logger.debug("Extracting...");
        Path javaPath;
        if (SystemInfo.OS_NAME.equals("windows")) {
            javaPath = folder.resolve("bin/java.exe");
        } else {
            javaPath = folder.resolve("bin/java");
        }
        if (!Files.exists(javaPath)) {
            // Create archive stream
            ArchiveInputStream archive;
            if (isTarFile) {
                // Extract tar from tar.gz
                Path tarPath = folder.resolve("jre-1.8.tar");
                if (!Files.exists(tarPath)) {
                    GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(Files.newInputStream(compressedFile)));
                    Files.copy(gzipInputStream, tarPath);
                    gzipInputStream.close();
                }

                // Create archive
                archive = new ArchiveStreamFactory().createArchiveInputStream("tar", new BufferedInputStream(Files.newInputStream(tarPath)));
            } else {
                // Create archive
                archive = new ArchiveStreamFactory().createArchiveInputStream("zip", new BufferedInputStream(Files.newInputStream(compressedFile)));
            }

            // Extract files
            ArchiveEntry entry;
            while ((entry = archive.getNextEntry()) != null) {

                // Extract file
                String path = entry.getName().substring(entry.getName().indexOf('/') + 1);
                Path entryPath = folder.resolve(path);
                if (!entry.isDirectory()) {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(archive, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // Close archive
            archive.close();
        }

        // Determine if the java file still doesn't exist, return if it still exists
        if (!Files.exists(javaPath)) {
            throw new IOException("Unable to find java location!");
        }
        stepProgress.accept(totalSteps, ++currentStep);
        logger.debug("Java 8 downloaded.");

        // Return the java path
        return javaPath;
    }

    /**
     * Downloads all the libraries for the given {@link Version}.
     *
     * @param version The {@link Version} to download libraries for.
     * @param stepProgress Called for every library to give a total amount of steps.
     * @param stepInfo Called to provide the name of each library.
     * @param downloadProgress Called to show download progress.
     * @return A filtered and downloaded list of {@link Library}'s.
     * @throws IOException Thrown if something goes wrong downloading any file operation or download.
     */
    public List<Library> downloadLibraries(Version version, StepProgressConsumer stepProgress, StepInfoConsumer stepInfo, DownloadProgressConsumer downloadProgress) throws IOException {
        logger.debug("Downloading libraries...");
        Path versionFolder = FileLocation.VERSIONS_FOLDER.resolve(version.getId() + "/");
        Path nativesFolder = versionFolder.resolve("natives/");
        Files.createDirectories(nativesFolder);

        // Filter libraries
        List<Library> libraries = version.getLibraries().stream().filter(library -> {
            // If there are no rules, return true
            if (library.getRules() == null || library.getRules().size() == 0) {
                return true;

            // If the rules resolve to allow, return true
            } else if (Rule.determine(library.getRules().toArray(Rule[]::new)) == Action.ALLOW) {
                return true;

            // Otherwise, return false
            } else {
                return false;
            }
        }).toList();

        // Main download loop
        final int totalSteps = libraries.size();
        int currentStep = 0;
        for (Library library : libraries) {
            // Update progress
            stepProgress.accept(totalSteps, ++currentStep);
            stepInfo.accept(library.getNameDetails()[1]);

            // Don't try and download libraries that don't have downloads
            if (library.getDownloads() == null) {
                continue;
            }

            // Get library artifact
            Artifact jarArtifact = library.getDownloads().getArtifact();
            if (jarArtifact != null) {
                assert jarArtifact.getPath() != null; // This won't be null for a library jar

                // Download if it does not already exist
                Path jarPath = FileLocation.LIBRARIES_FOLDER.resolve(jarArtifact.getPath());
                if (!Files.exists(jarPath)) {
                    Files.createDirectories(jarPath.getParent());
                    URL url = new URL(jarArtifact.getUrl());
                    long size = jarArtifact.getSize();
                    Network.download(url, jarPath, progress -> downloadProgress.accept(size, progress));
                }

                // Validate
                if (config.shouldValidate() && !Validation.validate(jarPath, jarArtifact.getSha1())) {
                    // TODO: Retry download.
                    throw new IOException("Validation failed!");
                }
            }

            // Download and extract natives if they exist
            Artifact natArtifact = library.getTargetedNatives();
            if (natArtifact != null) {
                assert natArtifact.getPath() != null; // This won't be null for a native
                assert natArtifact.getUrl() != null; // This won't be null for a native

                // Download if it does not already exist
                Path natPath = FileLocation.LIBRARIES_FOLDER.resolve(natArtifact.getPath());
                if (!Files.exists(natPath)) {
                    Files.createDirectories(natPath.getParent());
                    URL url = new URL(natArtifact.getUrl());
                    long size = natArtifact.getSize();
                    Network.download(url, natPath, progress -> downloadProgress.accept(size, progress));
                }

                // Validate
                if (config.shouldValidate() && !Validation.validate(natPath, natArtifact.getSha1())) {
                    // TODO: Retry download.
                    throw new IOException("Validation failed!");
                }

                // Extract the native
                this.extractNative(natPath, nativesFolder, library.getExtract() != null ? library.getExtract().get("exclude") : null);
            }
        }
        logger.debug("Libraries downloaded.");

        // Filter the libraries to exclude any native-only libraries (so it only returns 'true' libraries)
        return libraries.stream().filter(library -> {
            return library.getDownloads() != null && library.getDownloads().getArtifact() != null;
        }).toList();
    }

    /**
     * Takes in a source jar file and extracts it to the destination path avoiding the exclusions list.
     *
     * @param source The source jar file
     * @param destination The destination directory
     * @param exclusions A list of file exclusions
     */
    private void extractNative(Path source, Path destination, @Nullable String[] exclusions) throws IOException {
        logger.debug("Extracting native...");

        // Prepare jar file
        JarFile jar = new JarFile(source.toFile());
        Enumeration<JarEntry> entries = jar.entries();

        // Main extract loop
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            // Ignore non-dll files
            if (!entry.getName().endsWith(".dll")) {
                continue;
            }

            // Handle edge case where lwjgl.dll doesn't work with MC 1.16 if we don't remove the 64-bit version on a 32-bit PC
            if (SystemInfo.OS_ARCH.equals("x86") && entry.getName().equals("lwjgl.dll")) {
                continue;
            }

            // Create entry file location
            Path file = Paths.get(destination.toString(), entry.getName());

            // If the native already exists, ignore it and continue on
            if (Files.exists(file)) {
                continue;
            }

            // Process exclusions
            if (exclusions != null && exclusions.length > 0) {
                boolean exclude = false;
                for (String exclusion : exclusions) {
                    Path excludedFile = Path.of(exclusion);

                    // Compare the excluded filename to the jar file name and if they're equal don't extract
                    if (file.getFileName().equals(excludedFile.getFileName())) {
                        exclude = true;
                        break;

                        // Compare the excluded file parent's folder name to the jar file's folder name and if they're equal don't extract
                    } else if (file.getParent() != null && file.getParent().getFileName().equals(excludedFile.getFileName())) {
                        exclude = true;
                        break;
                    }
                }
                if (exclude) {
                    continue;
                }
            }

            // Copy native to destination
            Files.copy(jar.getInputStream(entry), file);
        }

        // Close jar file
        jar.close();
        logger.debug("Native extracted.");
    }

    /**
     * Downloads all the assets for the given {@link Version}.
     *
     * @param version The {@link Version} to download assets for.
     * @param profileFolder The {@link Path} to the profile where the game is to be run for legacy assets.
     * @param stepProgress Called for every asset to give the total amount of steps.
     * @param stepInfo Called to provide the name of each asset.
     * @param downloadProgress Called to show download progress.
     * @return The {@link AssetIndex} for this {@link Version}.
     * @throws IOException Thrown if something goes wrong for any file operation or download.
     */
    public AssetIndex downloadAssets(Version version, Path profileFolder, StepProgressConsumer stepProgress, StepInfoConsumer stepInfo, DownloadProgressConsumer downloadProgress) throws IOException {
        logger.debug("Downloading assets...");
        Path assetsFolder = FileLocation.ASSETS_FOLDER;
        Path objectsFolder = assetsFolder.resolve("objects/");
        Path virtualFolder = assetsFolder.resolve("virtual/legacy/");
        Path logConfigsFolder = assetsFolder.resolve("log_configs/");
        Path resourcesFolder = profileFolder.resolve("resources/");
        Path indexFile = assetsFolder.resolve("indexes/" + version.getAssetIndex().getId() + ".json");

        // Download the index file if it does not exist
        if (!Files.exists(indexFile)) {
            Files.createDirectories(indexFile.getParent());
            URL url = new URL(version.getAssetIndex().getUrl());
            Network.download(url, indexFile);
        }

        // Validate
        if (config.shouldValidate() && !Validation.validate(indexFile, version.getAssetIndex().getSha1())) {
            // TODO: Retry download.
            throw new IOException("Validation failed!");
        }

        // Parse the index file
        AssetIndex index = gson.fromJson(Files.newBufferedReader(indexFile), AssetIndex.class);

        // Create the directories if needed (so we're not checking every loop)
        boolean isVirtual = Boolean.TRUE.equals(index.isVirtual());
        if (isVirtual) {
            Files.createDirectories(virtualFolder);
        }
        boolean mapToResources = Boolean.TRUE.equals(index.mapToResources());
        if (mapToResources) {
            Files.createDirectories(resourcesFolder);
        }

        // Main download loop
        final int totalSteps = index.getObjects().entrySet().size() * 3 + 1; // 3 steps per asset, 1 log file download
        int currentStep = 0;
        for (Entry<String, Asset> entry : index.getObjects().entrySet()) {
            Asset asset = entry.getValue();

            // Update progress
            stepProgress.accept(totalSteps, ++currentStep);
            stepInfo.accept(asset.getHash());

            // Download asset if it does not already exist
            String assetLocation = asset.getId() + "/" + asset.getHash();
            Path assetPath = objectsFolder.resolve(assetLocation);
            if (!Files.exists(assetPath)) {
                Files.createDirectories(assetPath.getParent());
                URL url = new URL(config.getEndpoints().getAssetApi() + assetLocation);
                long size = asset.getSize();
                Network.download(url, assetPath, progress -> downloadProgress.accept(size, progress));
            }

            // Update progress
            stepProgress.accept(totalSteps, ++currentStep);

            // If the asset is virtual, copy the file to the virtual location
            if (isVirtual) {
                Path assetPathVirtual = virtualFolder.resolve(entry.getKey());
                if (!Files.exists(assetPathVirtual)) {
                    Files.createDirectories(assetPathVirtual.getParent());
                    Files.copy(assetPath, assetPathVirtual);
                }
            }

            // Update progress
            stepProgress.accept(totalSteps, ++currentStep);

            // If map to resources, copy the file to the resources location
            if (mapToResources) {
                Path assetResourcesPath = resourcesFolder.resolve(entry.getKey());
                if (!Files.exists(assetResourcesPath)) {
                    Files.createDirectories(assetResourcesPath.getParent());
                    Files.copy(assetPath, assetResourcesPath);
                }
            }
        }

        // Update progress
        stepProgress.accept(totalSteps, ++currentStep);
        stepInfo.accept("Logging Files");

        // Download log files
        if (version.getLogging() != null) {
            Artifact artifact = version.getLogging().getClient().getFile();
            Path logFilePath = logConfigsFolder.resolve(artifact.getId());
            if (!Files.exists(logFilePath)) {
                Files.createDirectories(logFilePath.getParent());
                URL url = new URL(artifact.getUrl());
                long size = artifact.getSize();
                Network.download(url, logFilePath, progress -> downloadProgress.accept(size, progress));
            }
        }
        logger.debug("Assets downloaded.");

        // Return the index
        return index;
    }

    /**
     * Launches Minecraft.
     *
     * @param user The {@link User} to launch with.
     * @param profile The {@link Profile} that is being launched.
     * @param version The {@link Version} to launch as provided by {@link ProtoLauncher#downloadVersion(VersionInfo, DownloadProgressConsumer)}
     * @param libraries The {@link Library} array as provided by {@link ProtoLauncher#downloadLibraries(Version, StepProgressConsumer, StepInfoConsumer, DownloadProgressConsumer)}
     * @param assetIndex The {@link AssetIndex} as provided by {@link ProtoLauncher#downloadAssets(Version, Path, StepProgressConsumer, StepInfoConsumer, DownloadProgressConsumer)}
     * @param javaPath The (optional) Java path as provided by {@link ProtoLauncher#downloadJava(StepProgressConsumer, DownloadProgressConsumer)}
     * @param launcherVersion The version of the launcher.
     * @return A new {@link Process} for Minecraft.
     * @throws IOException Thrown if something goes terribly wrong.
     */
    public Process launch(User user, Profile profile, Version version, List<Library> libraries, AssetIndex assetIndex, @Nullable Path javaPath, String launcherVersion) throws IOException {
        logger.debug("Launching...");

        // Prepare run directory
        Path runFolder = Path.of(profile.getPath()).toAbsolutePath();
        Files.createDirectories(runFolder);

        // Prepare jar location
        Path versionFolder = FileLocation.VERSIONS_FOLDER.resolve(version.getId() + "/");
        Path versionJarFile = versionFolder.resolve(version.getId() + ".jar");

        // Prepare classpath
        Path librariesFolder = FileLocation.LIBRARIES_FOLDER;
        String[] classpath = new String[libraries.size() + 1];
        for (int i = 0; i < classpath.length - 1; i++) {
            Library library = libraries.get(i);
            String path;
            if (library.getDownloads() == null || library.getDownloads().getArtifact() == null || library.getDownloads().getArtifact().getPath() == null) {
                // Construct path manually.
                String[] details = library.getNameDetails();
                path = details[0].replace(".", "/") + "/" + details[1] + "/" + details[1] + "-" + details[2] + ".jar";
            } else {
                path = library.getDownloads().getArtifact().getPath();
            }
            classpath[i] = librariesFolder.resolve(path).toAbsolutePath().toString();
        }
        classpath[classpath.length - 1] = versionJarFile.toAbsolutePath().toString();

        // Prepare launch arguments
        String arguments = "";
        if (version.getMinecraftArguments() != null) {
            arguments += "-Djava.library.path=${natives_directory} -cp ${classpath}" + ' ' + version.getMainClass() + ' ' + version.getMinecraftArguments();
        } else {
            arguments += version.getArguments().getJvm() + ' ' + version.getMainClass() + ' ' + version.getArguments().getGame();
        }

        // Replace argument variables
        arguments = arguments.replace("${auth_username}", user.getUsername());
        arguments = arguments.replace("${auth_player_name}", user.getUsername());
        arguments = arguments.replace("${version_name}", version.getId());
        arguments = arguments.replace("${game_directory}", '"' + runFolder.toString() + '"');
        arguments = arguments.replace("${assets_root}", '"' + FileLocation.ASSETS_FOLDER.toAbsolutePath().toString() + '"');
        assert version.getAssetIndex().getId() != null; // This won't be null for an asset index
        arguments = arguments.replace("${assets_index_name}", version.getAssetIndex().getId());
        if (Boolean.TRUE.equals(assetIndex.mapToResources())) {
            arguments = arguments.replace("${game_assets}", '"' + runFolder.resolve("assets/").toAbsolutePath().toString() + '"');
        } else if (Boolean.TRUE.equals(assetIndex.isVirtual())) {
            arguments = arguments.replace("${game_assets}", '"' + FileLocation.ASSETS_FOLDER.resolve("virtual/legacy/").toString() + '"');
        } else {
            arguments = arguments.replace("${game_assets}", '"' + FileLocation.ASSETS_FOLDER.toAbsolutePath().toString() + '"');
        }
        arguments = arguments.replace("${auth_uuid}", user.getUuid());
        arguments = arguments.replace("${auth_access_token}", user.getAccessToken());
        arguments = arguments.replace("${auth_session}", "token:" + user.getAccessToken() + ":" + user.getUuid());
        arguments = arguments.replace("${user_type}", "mojang");
        arguments = arguments.replace("${user_properties}", user.getUserProperties());
        arguments = arguments.replace("${version_type}", version.getType().toString().toLowerCase());
        arguments = arguments.replace("${natives_directory}", '"' + versionFolder.resolve("natives/").toAbsolutePath().toString() + '"');
        arguments = arguments.replace("${launcher_name}", "ProtoLauncher");
        arguments = arguments.replace("${launcher_version}", launcherVersion);
        arguments = arguments.replace("${classpath}", '"' + String.join(";", classpath) + '"');

        // Add resolution arguments
        if (profile.getLaunchSettings().getGameResolutionX() != -1) {
            arguments += " --width=" + profile.getLaunchSettings().getGameResolutionX();
        }
        if (profile.getLaunchSettings().getGameResolutionY() != -1) {
            arguments += " --height=" + profile.getLaunchSettings().getGameResolutionY();
        }

        // Add JVM arguments
        if (profile.getLaunchSettings().getJvmArguments() != null) {
            arguments = profile.getLaunchSettings().getJvmArguments() + " " + arguments;
        }

        // Prepare the launch command
        String command;
        if (javaPath == null) {
            command = "java -Xdiag " + arguments;
        } else {
            command = javaPath + " -Xdiag " + arguments;
        }

        // Launch the game
        ProcessBuilder builder = new ProcessBuilder(command.split(" "));
        builder.directory(runFolder.toFile());
        logger.debug("Launched.");
        return builder.inheritIO().start();
    }

}
