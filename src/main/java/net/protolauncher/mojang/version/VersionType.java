package net.protolauncher.mojang.version;

import com.google.gson.annotations.SerializedName;

/**
 * Marks the different types a game version can be.
 * Stems from the Mojang Version Manifest.
 */
public enum VersionType {

    @SerializedName("release")
    RELEASE,

    @SerializedName("snapshot")
    SNAPSHOT,

    @SerializedName("old_beta")
    OLD_BETA,

    @SerializedName("old_alpha")
    OLD_ALPHA

}
