package net.protolauncher.mods.version;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a basic amount of information about a modded game version.
 * Stems from the {@link ModdedVersionManifest}.
 * <br/><br/>
 * ModdedVersionInfo objects use the following format:
 * <pre>
 * [
 *     {
 *         "mcv": "MINECRAFT_VERSION",
 *         "lv": "LOADER_VERSION",
 *         "stable": TRUE/FALSE,
 *         "url": "THE_URL_TO_A_FILE_TO_DOWNLOAD",
 *         "type": "{@link ModdedVersionType}",
 *         "flags": 0, // Used for special cases; is a bitwise enum value based on the version type.
 *         "optional": {} // Optional data that doesn't have a consistent format.
 *     }
 * ]
 * </pre>
 * This may be modified in the future to allow for different loaders and provide extra data.
 * <br/><br/>
 * The <code>optional</code> field should only be used when the <code>flags</code> field has a value other than <code>0</code>.
 */
public class ModdedVersionInfo {

    // JSON Properties
    protected String mcv;
    protected String lv;
    protected boolean stable;
    protected String url;
    protected ModdedVersionType type;
    protected int flags;
    @Nullable
    protected JsonObject optional;

    // Suppress default constructor
    private ModdedVersionInfo() { }

    // Getters
    public String getMcv() {
        return mcv;
    }
    public String getLv() {
        return lv;
    }
    public boolean isStable() {
        return stable;
    }
    public String getUrl() {
        return url;
    }
    public ModdedVersionType getType() {
        return type;
    }
    public int getFlags() {
        return flags;
    }
    @Nullable
    public JsonObject getOptional() {
        return optional;
    }

}
