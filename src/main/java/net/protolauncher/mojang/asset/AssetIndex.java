package net.protolauncher.mojang.asset;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import net.protolauncher.mojang.Artifact;
import net.protolauncher.mojang.version.Version;
import net.protolauncher.util.Network;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

    /**
     * Resolves the file for this asset index from the given parent folder.
     *
     * @param id The id of the asset index.
     * @param parent The parent folder for all things assets.
     * @return The path for the file of this asset index.
     */
    public static Path resolveFilePath(String id, Path parent) {
        return parent.resolve("indexes/" + id + ".json");
    }

    /**
     * Loads an asset index, downloading the file if necessary.
     * This is the only way to create an asset index.
     *
     * @param folderLocation The folder location for all things assets.
     * @param gson The gson to use for parsing the asset index.
     * @param artifact The asset index artifact as obtained from a {@link Version}.
     * @return A new {@link AssetIndex}.
     * @throws IOException Thrown if there was an exception while trying to download or parse.
     */
    public static AssetIndex load(
        Path folderLocation,
        Gson gson,
        Artifact artifact
    ) throws IOException {
        // Check for existing asset index file
        String id = artifact.getId();
        Path path = resolveFilePath(id, folderLocation);
        AssetIndex instance;

        // If it doesn't exist, download a new one
        // Otherwise, load the existing file
        if (!Files.exists(path)) {
            // Download asset index
            assert artifact.getUrl() != null;
            String assetIndexString = Network.stringify(Network.fetch(new URL(artifact.getUrl())));
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, assetIndexString, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Parse asset index
            instance = gson.fromJson(assetIndexString, AssetIndex.class);
        } else {
            instance = gson.fromJson(Files.newBufferedReader(path), AssetIndex.class);
        }
        return instance;
    }

}
