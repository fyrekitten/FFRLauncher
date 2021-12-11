package net.protolauncher.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.protolauncher.api.Config.FileLocation;
import net.protolauncher.exception.NotInitializedException;
import net.protolauncher.function.DownloadProgressConsumer;
import net.protolauncher.gson.DurationTypeAdapter;
import net.protolauncher.gson.InstantTypeAdapter;
import net.protolauncher.mojang.version.VersionManifest;
import net.protolauncher.util.Network;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;

/**
 * The ProtoLauncher API. The structure of this class may seem weird to some, so here is some explanation:
 * <br/><br/>
 * Many methods in this class depend on other instance variables of the API after it has been created.
 * Normally, people would initialize this in the constructor and all would be fine and dandy. The issue is,
 * ProtoLauncher was designed for user feedback, and JavaFX and the interface will always load before the API.
 * To be able to provide loading feedback and loading steps, the API must be loaded incrementally. To provide
 * a flexible implementation, each step of the loading process is provided as a method within the API. This means
 * that some instance variables may not exist if somebody accidentally calls an API before it has been loaded.
 * As such, there is the {@link NotInitializedException} which will be thrown if a variable has not been
 * initialized, but an API method was called requiring that variable.
 * <br/><br/>
 * Please ensure to initialize the API before using any of its methods. The current initialization process is
 * as follows:
 * <ol>
 *     <li><code>{@link ProtoLauncher#loadConfig()}</code></li>
 *     <li><code>{@link ProtoLauncher#loadVersionManifest(DownloadProgressConsumer)}</code></li>
 * </ol>
 * <strong>Consider every instance variable in this API nullable.</strong>
 */
public class ProtoLauncher {

    // Launcher Variables
    private Gson gson;
    private Config config;

    // Mojang Variables
    private VersionManifest versionManifest;

    /**
     * Constructs a new ProtoLauncher API as well as the GSON builder for it.
     */
    public ProtoLauncher() {
        // Create a new GSON builder
        GsonBuilder builder = new GsonBuilder();

        // Set needed options
        builder.serializeNulls();
        builder.disableHtmlEscaping();
        builder.setVersion(1.0);
        builder.registerTypeAdapter(Instant.class, new InstantTypeAdapter());
        builder.registerTypeAdapter(Duration.class, new DurationTypeAdapter());

        // Create gson
        gson = builder.create();
    }

    // Getters
    public Config getConfig() {
        return config;
    }
    public Gson getGson() {
        return gson;
    }
    public VersionManifest getVersionManifest() {
        return versionManifest;
    }

    /**
     * Loads the configuration, creating a new one if one does not already exist.
     */
    public void loadConfig() throws IOException {
        Path path = FileLocation.CONFIG;

        // Check if one exists, and if not, generate a new one
        if (!Files.exists(path)) {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            config = new Config();
            this.saveConfig();
        } else {
            config = gson.fromJson(Files.newBufferedReader(path), Config.class);
        }

        // Apply gson configuration values
        GsonBuilder gsonBuilder = gson.newBuilder();
        if (config.shouldPrettyPrint()) {
            gsonBuilder.setPrettyPrinting();
        }
        gson = gsonBuilder.create();
    }

    /**
     * Saves the configuration, presumably after somebody's changed it.
     *
     * @throws IOException Thrown if saving the configuration goes horribly wrong.
     */
    public void saveConfig() throws IOException {
        Path path = FileLocation.CONFIG;
        Files.writeString(path, gson.toJson(config), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Loads the Mojang Version Manifest, downloading it if necessary.
     *
     * @param downloadProgressConsumer Called to show the download progress.
     * @throws IOException Thrown if something goes wrong loading or downloading the version manifest.
     */
    public void loadVersionManifest(DownloadProgressConsumer downloadProgressConsumer) throws IOException {
        if (config == null) {
            throw new NotInitializedException();
        }
        URL url = config.getEndpoints().getVersionManifest();
        Path path = FileLocation.VERSION_MANIFEST;

        // Check if it needs to be downloaded and, if it does, then download it
        Instant nextManifestUpdate = config.getLastManifestUpdate().plus(config.getMaxManifestAge());
        if (!Files.exists(path) || Instant.now().isAfter(nextManifestUpdate)) {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            long size = Network.fetchFileSize(url);
            Network.download(url, path, progress -> downloadProgressConsumer.accept(size, progress));
            config.setLastManifestUpdate(Instant.now());
            this.saveConfig();
        }

        // Load the manifest
        versionManifest = gson.fromJson(Files.newBufferedReader(path), VersionManifest.class);
    }

}
