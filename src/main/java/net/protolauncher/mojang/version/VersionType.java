package net.protolauncher.mojang.version;

import com.google.gson.annotations.SerializedName;

/**
 * Marks the different types a game version can be.
 */
public enum VersionType {

    @SerializedName("release")
    RELEASE("Release"),

    @SerializedName("snapshot")
    SNAPSHOT("Snapshot"),

    @SerializedName("old_beta")
    OLD_BETA("Old Beta"),

    @SerializedName("old_alpha")
    OLD_ALPHA("Old Alpha");

    private final String name;

    VersionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
