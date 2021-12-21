package net.protolauncher.mojang.auth;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import net.protolauncher.util.Network;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

public class MicrosoftAuth {

    // Variables
    private Gson gson;
    private String clientId; // The Azure Client ID to use for authentication
    private URL redirectUrl; // The return redirect URL (must be registered on the Azure Client ID)
    private String oauthUrl; // The Microsoft OAuth2 URL
    private String oauthTokenUrl; // The Microsoft OAuth2 Token URL
    private String xblUrl; // The Xbox Live OAuth2 URL
    private String xstsUrl; // The "XSTS" URL
    private String mcsUrl; // The Minecraft Services OAuth2 URL

    // Constructor
    public MicrosoftAuth(Gson gson, String clientId, URL redirectUrl, String oauthUrl, String oauthTokenUrl, String xblUrl, String xstsUrl, String mcsUrl) {
        this.gson = gson;
        this.clientId = clientId;
        this.redirectUrl = redirectUrl;
        this.oauthUrl = oauthUrl;
        this.oauthTokenUrl = oauthTokenUrl;
        this.xblUrl = xblUrl;
        this.xstsUrl = xstsUrl;
        this.mcsUrl = mcsUrl;
    }

    // Getters
    public Gson getGson() {
        return gson;
    }
    public String getClientId() {
        return clientId;
    }
    public URL getRedirectUrl() {
        return redirectUrl;
    }
    public String getOauthUrl() {
        return oauthUrl;
    }
    public String getOauthTokenUrl() {
        return oauthTokenUrl;
    }
    public String getXblUrl() {
        return xblUrl;
    }
    public String getXstsUrl() {
        return xstsUrl;
    }
    public String getMcsUrl() {
        return mcsUrl;
    }

    // Setters
    public void setGson(Gson gson) {
        this.gson = gson;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public void setRedirectUrl(URL redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
    public void setOauthUrl(String oauthUrl) {
        this.oauthUrl = oauthUrl;
    }
    public void setOauthTokenUrl(String oauthTokenUrl) {
        this.oauthTokenUrl = oauthTokenUrl;
    }
    public void setXblUrl(String xblUrl) {
        this.xblUrl = xblUrl;
    }
    public void setXstsUrl(String xstsUrl) {
        this.xstsUrl = xstsUrl;
    }
    public void setMcsUrl(String mcsUrl) {
        this.mcsUrl = mcsUrl;
    }

    /**
     * Uses the Microsoft auth code (acquired by prompting the user for Microsoft login)
     * to get an access token and a refresh token for Xbox Live authentication.
     *
     * @param authCode The authentication code.
     * @return A new {@link MicrosoftResponse}.
     * @throws IOException Thrown if authenticating fails.
     */
    public MicrosoftResponse authenticateMicrosoft(String authCode) throws IOException {
        // Create Encoded Parameters
        String encoded = "client_id=" + clientId +
                "&code=" + authCode +
                "&grant_type=authorization_code" +
                "&redirect_uri=" + redirectUrl;

        // Create connection
        HttpsURLConnection connection = Network.createConnection(new URL(oauthTokenUrl), "POST", false);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        try (OutputStream outstream = connection.getOutputStream()) {
            byte[] content = encoded.getBytes();
            outstream.write(content, 0, content.length);
        }

        // Fetch response
        String response = Network.stringify(Network.send(connection, false));

        try {
            return gson.fromJson(response, MicrosoftResponse.class);
        } catch (JsonParseException e) {
            throw new IOException("Internal error while authenticating! Contact developer. Response Content:\n\n" + response);
        }
    }

    /**
     * Refreshes the Microsoft access token (acquired by prompting the user for Microsoft login)
     * to allow for re-logging in with Xbox Live authentication.
     *
     * @param refreshToken The refresh token as acquired by Microsoft login.
     * @return A new {@link MicrosoftResponse}.
     * @throws IOException Thrown if refreshing fails.
     */
    public MicrosoftResponse refreshMicrosoft(String refreshToken) throws IOException {
        // Create Encoded Parameters
        String encoded = "client_id=" + clientId +
                "&refresh_token=" + refreshToken +
                "&grant_type=refresh_token" +
                "&redirect_uri=" + redirectUrl;

        // Create connection
        HttpsURLConnection connection = Network.createConnection(new URL(oauthTokenUrl), "POST", false);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        try (OutputStream outstream = connection.getOutputStream()) {
            byte[] content = encoded.getBytes();
            outstream.write(content, 0, content.length);
        }

        // Fetch response
        String response = Network.stringify(Network.send(connection));

        try {
            return gson.fromJson(response, MicrosoftResponse.class);
        } catch (JsonParseException e) {
            throw new IOException("Internal error while authenticating! Contact developer. Response Content:\n\n" + response);
        }
    }

    /**
     * Authenticates with Xbox Live using a Microsoft access token.
     *
     * @param accessToken The Microsoft access token.
     * @return A new {@link XboxLiveResponse}.
     * @throws IOException Thrown if there is a network error. This will throw a 401 error if the access token needs to be refreshed.
     */
    public XboxLiveResponse authenticateXboxLive(String accessToken) throws IOException {
        HttpsURLConnection connection = Network.createConnection(new URL(xblUrl), "POST", true);
        try (OutputStream outstream = connection.getOutputStream()) {
            JsonObject data = new JsonObject();
            JsonObject properties = new JsonObject();
            properties.addProperty("AuthMethod", "RPS");
            properties.addProperty("SiteName", "user.auth.xboxlive.com");
            properties.addProperty("RpsTicket", "d=" + accessToken);
            data.add("Properties", properties);
            data.addProperty("RelyingParty", "http://auth.xboxlive.com");
            data.addProperty("TokenType", "JWT");

            byte[] content = data.toString().getBytes();
            outstream.write(content, 0, content.length);
        }

        String response = Network.stringify(Network.send(connection));
        try {
            return gson.fromJson(response, XboxLiveResponse.class);
        } catch (JsonParseException e) {
            throw new IOException("Internal error while authenticating! Contact developer. Response Content:\n\n" + response);
        }
    }

    /**
     * Authenticates with XSTS using an Xbox Live token.
     *
     * @param xboxLiveToken The Xbox Live token.
     * @return A new {@link XboxLiveResponse}. Ensure to check the "xErr" on a 401 response. See <a href="https://wiki.vg/Microsoft_Authentication_Scheme#Authenticate_with_XSTS">https://wiki.vg/Microsoft_Authentication_Scheme</a>
     * @throws IOException Thrown if there is a network error.
     */
    public XboxLiveResponse authenticateXsts(String xboxLiveToken) throws IOException {
        HttpsURLConnection connection = Network.createConnection(new URL(xstsUrl), "POST", true);
        try (OutputStream outstream = connection.getOutputStream()) {
            JsonObject data = new JsonObject();
            JsonObject properties = new JsonObject();
            properties.addProperty("SandboxId", "RETAIL");
            JsonArray userTokens = new JsonArray();
            userTokens.add(xboxLiveToken);
            properties.add("UserTokens", userTokens);
            data.add("Properties", properties);
            data.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
            data.addProperty("TokenType", "JWT");

            byte[] content = data.toString().getBytes();
            outstream.write(content, 0, content.length);
        }

        String response = Network.stringify(Network.send(connection, false));
        try {
            return gson.fromJson(response, XboxLiveResponse.class);
        } catch (JsonParseException e) {
            throw new IOException("Internal error while authenticating! Contact developer. Response Content:\n\n" + response);
        }
    }

    /**
     * Acquires a good ol' Minecraft accessToken using the XSTS token and the 'uhs' response option.
     *
     * @param xstsToken The XSTS token.
     * @param uhs The UHS token from the xbox live or XSTS response.
     * @return A new {@link MinecraftResponse}.
     * @throws IOException Thrown if there is a network error.
     */
    public MinecraftResponse authenticateMinecraft(String xstsToken, String uhs) throws IOException {
        HttpsURLConnection connection = Network.createConnection(new URL(mcsUrl + "authentication/login_with_xbox"), "POST", true);
        try (OutputStream outstream = connection.getOutputStream()) {
            JsonObject data = new JsonObject();
            data.addProperty("identityToken", "XBL3.0 x=" + uhs + ";" + xstsToken);

            byte[] content = data.toString().getBytes();
            outstream.write(content, 0, content.length);
        }

        String response = Network.stringify(Network.send(connection, false));
        try {
            return gson.fromJson(response, MinecraftResponse.class);
        } catch (JsonParseException e) {
            throw new IOException("Internal error while authenticating! Contact developer. Response Content:\n\n" + response);
        }
    }

    /**
     * Verifies that the account owns Minecraft: Java Edition.
     *
     * @param accessToken The Mojang accessToken.
     * @return True if the accounts owns it, false if not.
     * @throws IOException Thrown if there is a network issue.
     */
    public boolean verifyOwnership(String accessToken) throws IOException {
        HttpsURLConnection connection = Network.createConnection(new URL(mcsUrl + "entitlements/mcstore"), "GET", true);
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);

        String response = Network.stringify(Network.send(connection, false));
        JsonObject obj = gson.fromJson(response, JsonObject.class);
        if (obj.get("items") != null) {
            JsonArray arr = obj.getAsJsonArray("items");
            boolean product = false;
            boolean game = false;
            for (JsonElement element : arr) {
                if (element instanceof JsonObject item) {
                    if (item.get("name") != null && item.get("name").getAsString().equals("product_minecraft")) {
                        product = true;
                    } else if (item.get("name") != null && item.get("name").getAsString().equals("game_minecraft")) {
                        game = true;
                    }
                }
            }
            return product && game;
        } else {
            return false;
        }
    }

    /**
     * Represents a response from the Microsoft login.
     */
    public static class MicrosoftResponse extends Error {

        // JSON Properties
        @SerializedName("token_type")
        private String tokenType;
        @SerializedName("expires_in")
        private String expiresIn;
        private String scope;
        @SerializedName("access_token")
        private String accessToken;
        @SerializedName("refresh_token")
        private String refreshToken;
        @SerializedName("user_id")
        private String userId;
        private String foci;

        // Suppress default constructor
        private MicrosoftResponse() { }

        // Getters
        public String getTokenType() {
            return tokenType;
        }
        public String getExpiresIn() {
            return expiresIn;
        }
        public String getScope() {
            return scope;
        }
        public String getAccessToken() {
            return accessToken;
        }
        public String getRefreshToken() {
            return refreshToken;
        }
        public String getUserId() {
            return userId;
        }
        public String getFoci() {
            return foci;
        }

    }

    /**
     * Represents a response from the Xbox Live login.
     */
    public static class XboxLiveResponse extends Error {

        // JSON Properties
        @SerializedName("IssueInstant")
        private String issueInstant;
        @SerializedName("NotAfter")
        private String notAfter;
        @SerializedName("Token")
        private String token;
        @SerializedName("DisplayClaims")
        private DisplayClaims displayClaims;
        @SerializedName("Identity")
        private String identity;
        @SerializedName("XErr")
        private String xerr;
        @SerializedName("Message")
        private String message;
        @SerializedName("Redirect")
        private String redirect;

        // Suppress default constructor
        private XboxLiveResponse() { }

        // Getters
        public String getIssueInstant() {
            return issueInstant;
        }
        public String getNotAfter() {
            return notAfter;
        }
        public String getToken() {
            return token;
        }
        public DisplayClaims getDisplayClaims() {
            return displayClaims;
        }
        public String getIdentity() {
            return identity;
        }
        public String getXerr() {
            return xerr;
        }
        public String getMessage() {
            return message;
        }
        public String getRedirect() {
            return redirect;
        }

        /**
         * Represents the display claims object of an Xbox Live response.
         */
        public static class DisplayClaims {

            // JSON Properties
            private Uhs[] xui;

            // Suppress default constructor
            private DisplayClaims() { }

            // Getters
            public Uhs[] getXui() {
                return xui;
            }

            /**
             * Represents a UHS object of the display claims object of an Xbox Live Response.
             */
            public static class Uhs {

                // JSON Properties
                private String uhs;

                // Suppress default constructor
                private Uhs() { }

                // Getters
                public String getUhs() {
                    return uhs;
                }

            }

        }

    }

    /**
     * Represents a response form the Minecraft Service login.
     */
    public static class MinecraftResponse extends Error {

        // JSON Properties
        private String username;
        private JsonElement[] roles;
        @SerializedName("access_token")
        private String accessToken;
        @SerializedName("token_type")
        private String tokenType;
        @SerializedName("expires_in")
        private String expiresIn;

        // Suppress default constructor
        private MinecraftResponse() { }

        // Getters
        public String getUsername() {
            return username;
        }
        public JsonElement[] getRoles() {
            return roles;
        }
        public String getAccessToken() {
            return accessToken;
        }
        public String getTokenType() {
            return tokenType;
        }
        public String getExpiresIn() {
            return expiresIn;
        }

    }

    /**
     * Represents an errored response.
     */
    public static class Error {

        // JSON Properties
        private String error;
        @SerializedName("error_description")
        private String errorDescription;
        @SerializedName("correlation_id")
        private String correlationId;

        // Suppress default constructor
        private Error() { }

        // Getters
        public String getError() {
            return error;
        }
        public String getErrorDescription() {
            return errorDescription;
        }
        public String getCorrelationId() {
            return correlationId;
        }

    }

}
