package net.protolauncher.mojang.asset;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Represents a <a href="https://minecraft.fandom.com/wiki/Client.json#:~:text=%C2%A0-,assetIndex,-%3A">Mojang Asset Index</a>
 */
public class AssetIndex {

    // JSON Properties
    private HashMap<String, Asset> objects;
    @Nullable
    private Boolean virtual;
    @Nullable
    @SerializedName("map_to_resources")
    private Boolean mapToResources;
    @Nullable
    private String id;

    // Suppress default constructor
    private AssetIndex() { }

    // Getters
    public HashMap<String, Asset> getObjects() {
        return objects;
    }
    @Nullable
    public Boolean isVirtual() {
        return virtual;
    }
    @Nullable
    public Boolean mapToResources() {
        return mapToResources;
    }
    @Nullable
    public String getId() {
        return id;
    }

}
