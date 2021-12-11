package net.protolauncher.api;

import com.google.gson.annotations.Since;
import org.jetbrains.annotations.Nullable;

public class User {

    // JSON Properties
    /**
     * The username of this user as provided by Mojang.
     */
    @Since(1.0)
    private String username;
    /**
     * The UUID of this user (as a string) as provided by Mojang.
     */
    @Since(1.0)
    private String uuid;
    /**
     * The user properties for this user for legacy versions of Minecraft.
     */
    @Since(1.0)
    private String userProperties;
    /**
     * The access token of this user as provided by Mojang.
     */
    @Since(1.0)
    private String accessToken;
    /**
     * Microsoft login information for this user if the user does not have a Mojang account.
     */
    @Since(1.0)
    @Nullable
    private MicrosoftInfo microsoftInfo;

    /**
     * Constructs a new user with the provided username, uuid, and access token.
     * @param username The user's username.
     * @param uuid The user's uuid.
     * @param accessToken The user's access token.
     */
    public User(String username, String uuid, String accessToken) {
        this.username = username;
        this.uuid = uuid;
        this.userProperties = "";
        this.accessToken = accessToken;
        this.microsoftInfo = null;
    }

    // Getters
    public String getUsername() {
        return username;
    }
    public String getUuid() {
        return uuid;
    }
    public String getUserProperties() {
        return userProperties;
    }
    public String getAccessToken() {
        return accessToken;
    }
    @Nullable
    public MicrosoftInfo getMicrosoftInfo() {
        return microsoftInfo;
    }

    // Setters
    public User setUsername(String username) {
        this.username = username;
        return this;
    }
    public User setUserProperties(String userProperties) {
        this.userProperties = userProperties;
        return this;
    }
    public User setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }
    public User setMicrosoftInfo(MicrosoftInfo microsoftInfo) {
        this.microsoftInfo = microsoftInfo;
        return this;
    }

    /**
     * Relates to the required information provided by a Microsoft login.
     */
    public static class MicrosoftInfo {

        // JSON Properties
        @Since(1.0)
        private String accessToken;
        @Since(1.0)
        private String refreshToken;
        @Since(1.0)
        private String xblToken;
        @Since(1.0)
        private String xblUhs;
        @Since(1.0)
        private String xstsToken;
        @Since(1.0)
        private long dateExpires;

        /**
         * Constructs a new MicrosoftInfo with all the necessary data.
         *
         * @param accessToken The access token for the Microsoft account
         * @param refreshToken The refresh token for the Microsoft account
         * @param xblToken The Xbox Live access token
         * @param xblUhs The Xbox Live UHS
         * @param xstsToken The XSTS access token
         * @param dateExpires The date in which the tokens will expire
         */
        public MicrosoftInfo(String accessToken, String refreshToken, String xblToken, String xblUhs, String xstsToken, long dateExpires) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.xblToken = xblToken;
            this.xblUhs = xblUhs;
            this.xstsToken = xstsToken;
            this.dateExpires = dateExpires;
        }

        // Getters
        public String getAccessToken() {
            return accessToken;
        }
        public String getRefreshToken() {
            return refreshToken;
        }
        public String getXblToken() {
            return xblToken;
        }
        public String getXblUhs() {
            return xblUhs;
        }
        public String getXstsToken() {
            return xstsToken;
        }
        public long getDateExpires() {
            return dateExpires;
        }

        // Setters
        public MicrosoftInfo setAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

    }

}
