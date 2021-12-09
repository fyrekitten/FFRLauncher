package net.protolauncher.mojang.asset;

/**
 * Represents an asset object from an asset index.
 * Stems from the {@link AssetIndex}.
 */
public class Asset {

    // JSON Properties
    protected String hash;
    protected long size;

    // Suppress default constructor
    private Asset() { }

    // Getters
    public String getHash() {
        return hash;
    }
    public String getId() {
        return hash.substring(0, 2);
    }
    public long getSize() {
        return size;
    }

}
