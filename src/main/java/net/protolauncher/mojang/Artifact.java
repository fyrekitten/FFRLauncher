package net.protolauncher.mojang;

import org.jetbrains.annotations.Nullable;

/**
 * An artifact basically contains information regarding an online file that needs to be downloaded.
 * This can be anywhere from an asset to a library to a game version.
 * All artifacts must be sha1 validated.
 */
public class Artifact {

    // JSON Properties
    @Nullable
    private String id;
    @Nullable
    private String path;
    private String sha1;
    private long size;
    @Nullable
    private Long totalSize;
    @Nullable
    private String url;

    // Constructor
    public Artifact(@Nullable String id, @Nullable String path, String sha1, long size, @Nullable Long totalSize, @Nullable String url) {
        this.id = id;
        this.path = path;
        this.sha1 = sha1;
        this.size = size;
        this.totalSize = totalSize;
        this.url = url;
    }

    // Getters
    @Nullable
    public String getId() {
        return id;
    }
    @Nullable
    public String getPath() {
        return path;
    }
    public String getSha1() {
        return sha1;
    }
    public long getSize() {
        return size;
    }
    @Nullable
    public Long getTotalSize() {
        return totalSize;
    }
    @Nullable
    public String getUrl() {
        return url;
    }

}
