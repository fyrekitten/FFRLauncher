package net.protolauncher.mojang.auth;

import com.google.gson.Gson;
import net.protolauncher.util.Network;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;

public class MojangAPI {

    // Variables
    private Gson gson;
    private String mojangApi;
    private String minecraftServicesApi;

    /**
     * Constructs a new Mojang API.
     *
     * @param gson The GSON parser to use when parsing requests.
     * @param mojangApi The Mojang API endpoint including the end slash.
     * @param minecraftServicesApi The Minecraft Services API endpoint including the end slash.
     */
    public MojangAPI(Gson gson, String mojangApi, String minecraftServicesApi) {
        this.gson = gson;
        this.mojangApi = mojangApi;
        this.minecraftServicesApi = minecraftServicesApi;
    }

    // Getters
    public Gson getGson() {
        return gson;
    }
    public String getMojangApi() {
        return mojangApi;
    }
    public String getMinecraftServicesApi() {
        return minecraftServicesApi;
    }

    // Setters
    public void setGson(Gson gson) {
        this.gson = gson;
    }
    public void setMojangApi(String mojangApi) {
        this.mojangApi = mojangApi;
    }
    public void setMinecraftServicesApi(String minecraftServicesApi) {
        this.minecraftServicesApi = minecraftServicesApi;
    }

    /**
     * Fetches profile information for the given access token.
     * @param accessToken The access token for the user.
     * @return A new {@link ProfileInformationResponse}.
     * @throws IOException Thrown if fetching the information fails.
     */
    public ProfileInformationResponse getProfileInformation(String accessToken) throws IOException {
        HttpsURLConnection connection = Network.createConnection(new URL(minecraftServicesApi + "minecraft/profile"), "GET", true);
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);

        String response = Network.stringify(Network.send(connection, false));
        return gson.fromJson(response, ProfileInformationResponse.class);
    }

    /**
     * Represents a response from the Profile Information MCS endpoint.
     */
    public static class ProfileInformationResponse extends Error {

        // JSON Properties
        private String id;
        private String name;
        private Skin[] skins;
        private Cape[] capes;

        // Suppress default constructor
        private ProfileInformationResponse() { }

        // Getters
        public String getId() {
            return id;
        }
        public String getName() {
            return name;
        }
        public Skin[] getSkins() {
            return skins;
        }
        public Cape[] getCapes() {
            return capes;
        }

    }

    /**
     * Represents a skin from the MCS endpoint.
     */
    public static class Skin {

        // JSON Properties
        private String id;
        private String state;
        private String url;
        private String variant;

        // Suppress default constructor
        private Skin() { }

        // Getters
        public String getId() {
            return id;
        }
        public String getState() {
            return state;
        }
        public String getUrl() {
            return url;
        }
        public String getVariant() {
            return variant;
        }

    }

    /**
     * Represents a cape from the MCS endpoint.
     */
    public static class Cape {

        // JSON Properties
        private String id;
        private String state;
        private String url;
        private String alias;

        // Suppress default constructor
        private Cape() { }

        // Getters
        public String getId() {
            return id;
        }
        public String getState() {
            return state;
        }
        public String getUrl() {
            return url;
        }
        public String getAlias() {
            return alias;
        }

    }

    /**
     * Represents an error from the MCS endpoint.
     */
    public static class Error {

        // JSON Properties
        private String path;
        private String errorMessage;
        private String developerMessage;

        // Suppress default constructor
        private Error() { }

        // Getters
        public String getPath() {
            return path;
        }
        public String getErrorMessage() {
            return errorMessage;
        }
        public String getDeveloperMessage() {
            return developerMessage;
        }

    }

}
