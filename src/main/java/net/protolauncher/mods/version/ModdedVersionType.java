package net.protolauncher.mods.version;

import com.google.gson.annotations.SerializedName;

/**
 * Marks the different types a modded game version can be.
 * See each of the following types for more info on each one:
 * <ul>
 *     <li>{@link ModdedVersionType#JAR}</li>
 *     <li>{@link ModdedVersionType#VERSION}</li>
 *     <li>{@link ModdedVersionType#FABRIC}</li>
 *     <li>{@link ModdedVersionType#FORGE}</li>
 * </ul>
 */
public enum ModdedVersionType {

    /**
     * The provided URL is a JAR file that will be extracted and merged into a Mojang-version's JAR file.
     * This practice is discouraged in modern-day modding, use a Mojang-compatible version file instead.
     */
    @SerializedName("jar")
    JAR("JAR"),

    /**
     * A Mojang-compatible version file is the provided URL.
     * Only a version merge is needed.
     */
    @SerializedName("version")
    VERSION("Version"),

    /**
     * A Fabric-compatible profile json file is the provided URL.
     * A version merge and minor modification is needed to make it Mojang-compatible.
     */
    @SerializedName("fabric")
    FABRIC("Fabric"),

    /**
     * A Forge-compatible installer download is the provided URL.
     * A large amount of modification, extraction, and extra work is needed to make it Mojang-compatible.
     */
    @SerializedName("forge")
    FORGE("Forge");

    private final String name;

    ModdedVersionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
