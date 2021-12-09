package net.protolauncher.mojang.library;

import net.protolauncher.mojang.Artifact;
import net.protolauncher.mojang.rule.Rule;
import net.protolauncher.util.SystemInfo;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

/**
 * Represents a library for a game version.
 * Stems from the Mojang Minecraft Client File.
 */
public class Library {

    // JSON Properties
    private LibraryDownloads downloads;
    @Nullable
    private HashMap<String, String[]> extract; // A mapping of what needs to be extracted
    private String name;
    @Nullable
    private HashMap<String, String> natives; // A mapping of os details for a native
    @Nullable
    private List<Rule> rules;

    // Suppress default constructor
    private Library() { }

    // Getters
    public LibraryDownloads getDownloads() {
        return downloads;
    }
    @Nullable
    public HashMap<String, String[]> getExtract() {
        return extract;
    }
    public String getName() {
        return name;
    }
    @Nullable
    public HashMap<String, String> getNatives() {
        return natives;
    }
    @Nullable
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * Splits the name into it separate parts to get the details about the library.
     *
     * @return A string array of length 3 with 0 being the path, 1 being the name, and 2 being the version
     */
    public String[] getNameDetails() {
        String[] parts = new String[3];
        int first = name.indexOf(':');
        int second = name.indexOf(':', first + 1);

        parts[0] = name.substring(0, first);
        parts[1] = name.substring(first + 1, second);
        parts[2] = name.substring(second + 1);

        return parts;
    }

    /**
     * Checks all the natives OS keys against the current {@link SystemInfo#OS_NAME} to see if they should be included.
     *
     * @return The artifact for the targeted natives (there should only be one).
     */
    @Nullable
    public Artifact getTargetedNatives() {
        if (downloads == null || downloads.getClassifiers() == null || natives == null) {
            return null;
        }

        // Get the natives for the specified OS, and if it returns null, then return null
        String targetedNatives = natives.get(SystemInfo.OS_NAME);
        if (targetedNatives == null) {
            return null;
        } else {
            String key = targetedNatives.replace("${arch}", SystemInfo.OS_BIT);
            return downloads.getClassifiers().get(key);
        }
    }

    /**
     * Represents a list of downloads for a library.
     */
    public static class LibraryDownloads {

        // JSON Properties
        private Artifact artifact;
        private HashMap<String, Artifact> classifiers;

        // Suppress default constructor
        private LibraryDownloads() { }

        // Getters
        public Artifact getArtifact() {
            return artifact;
        }
        public HashMap<String, Artifact> getClassifiers() {
            return classifiers;
        }

    }

}
