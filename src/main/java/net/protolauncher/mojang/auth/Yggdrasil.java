package net.protolauncher.mojang.auth;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.protolauncher.util.Network;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;

public class Yggdrasil {

    // Variables
    private Gson gson;
    private String api;
    private String clientToken;

    /**
     * Constructs a new Yggdrasil API.
     *
     * @param gson The GSON parser to use when parsing requests.
     * @param api The API endpoint including the end slash.
     * @param clientToken The client token to use for all requests.
     */
    public Yggdrasil(Gson gson, String api, String clientToken) {
        this.gson = gson;
        this.api = api;
        this.clientToken = clientToken;
    }

    // Getters
    public Gson getGson() {
        return gson;
    }
    public String getApi() {
        return api;
    }
    public String getClientToken() {
        return clientToken;
    }

    // Setters
    public void setGson(Gson gson) {
        this.gson = gson;
    }
    public void setApi(String api) {
        this.api = api;
    }
    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    /**
     * Sends an authentication JSON request to the authentication API and returns the response.
     *
     * @param username The username to authenticate with.
     * @param password The password to authenticate with.
     * @return A {@link Yggdrasil.Response}.
     * @throws IOException Thrown if there is a network issue.
     */
    public Response authenticate(String username, String password) throws IOException {
        HttpsURLConnection connection = Network.createConnection(new URL(api + "authenticate"), "POST", true);
        try (OutputStream outstream = connection.getOutputStream()) {
            JsonObject data = new JsonObject();

            // Add Agent
            JsonObject agent = new JsonObject();
            agent.addProperty("name", "Minecraft");
            agent.addProperty("version", 1);
            data.add("agent", agent);

            // Add Username, Password, and ClientToken
            data.addProperty("username", username);
            data.addProperty("password", password);
            data.addProperty("clientToken", clientToken);

            // Request User
            data.addProperty("requestUser", true);

            // Write JSON
            byte[] content = data.toString().getBytes();
            outstream.write(content, 0, content.length);
        }
        return gson.fromJson(Network.stringify(Network.send(connection, false)), Response.class);
    }

    /**
     * Returns a boolean indicating whether the provided access token is valid or not.
     *
     * @param accessToken The access token to check if valid.
     * @return Whether the provided access token is valid or not.
     * @throws IOException Thrown if there is a network issue.
     */
    public boolean validate(String accessToken) throws IOException {
        HttpsURLConnection connection = Network.createConnection(new URL(api + "validate"), "POST", true);
        try (OutputStream outstream = connection.getOutputStream()) {
            JsonObject data = new JsonObject();

            // Add AccessToken & ClientToken
            data.addProperty("accessToken", accessToken);
            data.addProperty("clientToken", clientToken);

            // Write JSON
            byte[] content = data.toString().getBytes();
            outstream.write(content, 0, content.length);
        }
        return connection.getResponseCode() == 204;
    }

    /**
     * Attempts to invalidate the provided access token and returns whether it failed or not.
     *
     * @param accessToken The access token to invalidate.
     * @return Whether the invalidation succeeded or not.
     * @throws IOException Thrown if there is a network issue.
     */
    public boolean invalidate(String accessToken) throws IOException {
        HttpsURLConnection connection = Network.createConnection(new URL(api + "invalidate"), "POST", true);
        try (OutputStream outstream = connection.getOutputStream()) {
            JsonObject data = new JsonObject();

            // Add AccessToken & ClientToken
            data.addProperty("accessToken", accessToken);
            data.addProperty("clientToken", clientToken);

            // Write JSON
            byte[] content = data.toString().getBytes();
            outstream.write(content, 0, content.length);
        }

        // Get response
        String response = Network.stringify(Network.send(connection));
        return response.isEmpty();
    }

    /**
     * Attempts to refresh the provided access token.
     *
     * @param accessToken The token that should be refreshed.
     * @return A {@link Yggdrasil.Response}.
     * @throws IOException Thrown if there is a network issue.
     */
    public Response refresh(String accessToken) throws IOException {
        HttpsURLConnection connection = Network.createConnection(new URL(api + "refresh"), "POST", true);
        try (OutputStream outstream = connection.getOutputStream()) {
            JsonObject data = new JsonObject();

            // Add AccessToken & ClientToken
            data.addProperty("accessToken", accessToken);
            data.addProperty("clientToken", clientToken);

            // Write JSON
            byte[] content = data.toString().getBytes();
            outstream.write(content, 0, content.length);
        }
        return gson.fromJson(Network.stringify(Network.send(connection, false)), Response.class);
    }

    /**
     * Deserializes the userProperties object for use in launching 1.7.
     *
     * @param properties The raw properties JsonElement.
     * @return A string representing the serialized json properties.
     */
    public String deserializeProperties(JsonElement properties) {
        HashMap<String, JsonArray> finalProperties = new HashMap<>();
        if (properties.isJsonArray()) {
            JsonArray arr = (JsonArray) properties;
            for (JsonElement element : arr) {
                if (!element.isJsonObject()) {
                    continue;
                }

                JsonObject obj = element.getAsJsonObject();
                if (obj.get("name") != null && obj.get("value") != null) {
                    String key = obj.get("name").getAsString();
                    JsonArray value = new JsonArray();
                    value.add(obj.get("value"));
                    finalProperties.put(key, value);
                }
            }
        } else if (properties.isJsonObject()) {
            finalProperties = gson.fromJson(properties, new TypeToken<HashMap<String, String>>() {}.getType());
        }
        // We create a new GSON here as we need the default GSON properties to parse this correctly.
        return (new Gson()).toJson(finalProperties);
    }

    /**
     * Represents a login response (also used for refreshing).
     */
    public static class Response extends Error {

        // JSON Properties
        private String accessToken;
        private String clientToken;
        private SelectedProfile selectedProfile;
        @Nullable
        private User user;

        // Suppress default constructor
        private Response() { }

        // Getters
        public String getAccessToken() {
            return accessToken;
        }
        public String getClientToken() {
            return clientToken;
        }
        public SelectedProfile getSelectedProfile() {
            return selectedProfile;
        }
        @Nullable
        public User getUser() {
            return user;
        }

        /**
         * Represents the selected profile of a response.
         */
        public static class SelectedProfile {

            // JSON Properties
            private String id;
            private String name;

            // Suppress default constructor
            private SelectedProfile() { }

            // Getters
            public String getId() {
                return id;
            }
            public String getName() {
                return name;
            }

        }

        /**
         * Represents the user object of a response, if requestUser was added as an option.
         */
        public static class User {

            // JSON Properties
            @Nullable
            private JsonElement properties;

            // Suppress default constructor
            private User() { }

            // Getters
            @Nullable
            public JsonElement getProperties() {
                return properties;
            }

        }

    }

    /**
     * Represents an errored response.
     */
    public static class Error {

        // JSON Properties
        private String error;
        private String errorMessage;
        private String cause;

        // Suppress default constructor
        private Error() { }

        // Getters
        public String getError() {
            return error;
        }
        public String getErrorMessage() {
            return errorMessage;
        }
        public String getCause() {
            return cause;
        }

    }

}
