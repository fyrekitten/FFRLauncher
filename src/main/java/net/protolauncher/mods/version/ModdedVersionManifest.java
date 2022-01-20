package net.protolauncher.mods.version;

import java.util.List;

/**
 * Represents a ProtoLauncher Modded Version Manifest.
 * <br/><br/>
 * ModdedVersionManifests use the following format:
 * <pre>
 * {
 *     "versions": [] // An array of {@link ModdedVersionInfo} objects
 * }
 * </pre>
 * This may be modified in the future to allow for different loaders and provide extra data.
 */
public class ModdedVersionManifest {

    // JSON Properties
    private List<ModdedVersionInfo> versions;

    // Suppress default constructor
    private ModdedVersionManifest() { }

    // Getters
    public List<ModdedVersionInfo> getVersions() {
        return versions;
    }

    /**
     * Fetches all versions of the corresponding type from the manifest.
     *
     * @param type The type to filter by.
     * @return A filtered list of {@link ModdedVersionInfo}'s.
     */
    public List<ModdedVersionInfo> getVersionsOfType(ModdedVersionType type) {
        return versions.stream().filter(x -> x.type == type).toList();
    }

    /**
     * Fetches all versions that contain the given Minecraft version from the manifest.
     * @param mcv The Minecraft version to get the infos for.
     * @return A filtered list of {@link ModdedVersionInfo}'s.
     */
    public List<ModdedVersionInfo> getVersionsWithMcv(String mcv) {
        return versions.stream().filter(x -> x.mcv.equals(mcv)).toList();
    }

    /**
     * Fetches all versions of the corresponding type that contain the given Minecraft version from the manifest.
     * Useful for fetching the latest version of a loader for a given Minecraft version.
     *
     * @param type The type to filter by.
     * @param mcv The Minecraft version to get the infos for.
     * @return A filtered list of {@link ModdedVersionInfo}'s.
     */
    public List<ModdedVersionInfo> getVersionsOfTypeWithMcv(ModdedVersionType type, String mcv) {
        return versions.stream().filter(x -> x.type == type && x.mcv.equals(mcv)).toList();
    }

    /**
     * Attempts to fetch a {@link ModdedVersionInfo} of the given type by the given Minecraft version id and loader version id from the manifest.
     * @param type The type to filter by.
     * @param mcv The Minecraft version id of the {@link ModdedVersionInfo} to fetch.
     * @param lv The loader version id of the {@link ModdedVersionInfo} to fetch.
     * @return A {@link ModdedVersionInfo} or null if not found.
     */
    public ModdedVersionInfo getVersion(ModdedVersionType type, String mcv, String lv) {
        return versions.stream().filter(x -> x.type == type && x.mcv.equals(mcv) && x.lv.equals(lv)).findFirst().orElse(null);
    }

}
