package net.protolauncher.mojang.version;

import com.google.gson.annotations.SerializedName;
import net.protolauncher.mojang.Artifact;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a list of client/server downloads for a game version.
 * Stems from the Mojang Minecraft Client File.
 */
public class VersionDownloads {

    // JSON Properties
    protected Artifact client;
    @SerializedName("client_mappings")
    @Nullable
    protected Artifact clientMappings;
    protected Artifact server;
    @SerializedName("server_mappings")
    @Nullable
    protected Artifact serverMappings;

    // Suppress default constructor
    private VersionDownloads() { }

    // Getters
    public Artifact getClient() {
        return client;
    }
    @Nullable
    public Artifact getClientMappings() {
        return clientMappings;
    }
    public Artifact getServer() {
        return server;
    }
    @Nullable
    public Artifact getServerMappings() {
        return serverMappings;
    }

}
