package net.protolauncher.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.protolauncher.api.Config.FileLocation;
import net.protolauncher.function.DownloadProgressConsumer;
import net.protolauncher.function.StepProgressConsumer;
import net.protolauncher.gson.DurationTypeAdapter;
import net.protolauncher.gson.InstantTypeAdapter;
import net.protolauncher.mojang.Artifact;
import net.protolauncher.mojang.version.Version;
import net.protolauncher.mojang.version.VersionInfo;
import net.protolauncher.mojang.version.VersionManifest;
import net.protolauncher.util.Network;
import net.protolauncher.util.SystemInfo;
import net.protolauncher.util.Validation;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.zip.GZIPInputStream;

/**
 * The ProtoLauncher API. The structure of this class may seem weird to some, so here is some explanation:
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

        // Create new configuration
        config = new Config();
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
     * Loads the {@link Config}, creating a new one if one does not already exist.
     *
     * @return The loaded {@link Config}.
     */
    public Config loadConfig() throws IOException {
        Path path = FileLocation.CONFIG;

        // Check if one exists, and if not, generate a new one
        if (!Files.exists(path)) {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
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

        // Return the config
        return config;
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
     * Loads the {@link VersionManifest}, downloading it if necessary.
     *
     * @param downloadProgressConsumer Called to show the download progress.
     * @return The loaded {@link VersionManifest}.
     * @throws IOException Thrown if something goes wrong loading or downloading the version manifest.
     */
    public VersionManifest loadVersionManifest(DownloadProgressConsumer downloadProgressConsumer) throws IOException {
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

        // Return the manifest
        return versionManifest;
    }

    /**
     * Downloads the version file from the given {@link VersionInfo}.
     *
     * @param info The information to download the version file from.
     * @param downloadProgressConsumer Called to show the download progress.
     * @return The loaded {@link Version}.
     * @throws IOException Thrown if something goes wrong loading or downloading the version.
     */
    public Version downloadVersion(VersionInfo info, DownloadProgressConsumer downloadProgressConsumer) throws IOException {
        String id = info.getId();
        Path folder = FileLocation.VERSIONS_FOLDER.resolve(id + "/");
        Path file = folder.resolve(id + ".json");

        // Check if it needs to be downloaded and, if it does, then download it
        if (!Files.exists(file)) {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            URL url = new URL(info.getUrl());
            long size = Network.fetchFileSize(url);
            Network.download(url, file, progress -> downloadProgressConsumer.accept(size, progress));
        }

        // Validate
        if (config.shouldValidate() && !Validation.validate(file, info.getSha1())) {
            // TODO: Retry download.
            throw new IOException("Validation failed!");
        }

        // Load version
        return gson.fromJson(Files.newBufferedReader(file), Version.class);
    }

    /**
     * Downloads the client JAR file for the given version.
     *
     * @param version The {@link Version} to download the client for.
     * @param downloadProgressConsumer Called to show the download progress.
     * @throws IOException Thrown if something goes wrong downloading the client.
     */
    public void downloadVersionClient(Version version, DownloadProgressConsumer downloadProgressConsumer) throws IOException {
        String id = version.getId();
        Path folder = FileLocation.VERSIONS_FOLDER.resolve(id + "/");
        Path file = folder.resolve(id + ".jar");
        Artifact artifact = version.getDownloads().getClient();

        // Check if it needs to be downloaded and, if it does, then download it
        if (!Files.exists(file)) {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            URL url = new URL(artifact.getUrl());
            long size = artifact.getSize();
            Network.download(url, file, progress -> downloadProgressConsumer.accept(size, progress));
        }

        // Validate
        if (config.shouldValidate() && !Validation.validate(file, artifact.getSha1())) {
            // TODO: Retry download.
            throw new IOException("Validation failed!");
        }
    }

    /**
     * Downloads Java 8 for the appropriate system platform.
     *
     * @param stepProgress The progress of the 'steps' of the download (download, then extraction).
     * @param downloadProgressConsumer Called to show the download progress.
     * @return The {@link Path} to the Java executable.
     * @throws IOException Thrown if something goes wrong downloading Java.
     * @throws ArchiveException Thrown if something goes wrong during the extraction process.
     */
    public Path downloadJava(StepProgressConsumer stepProgress, DownloadProgressConsumer downloadProgressConsumer) throws IOException, ArchiveException {
        final int totalSteps = 2;
        int currentStep = 0;
        stepProgress.accept(totalSteps, currentStep++);
        Path folder = FileLocation.JAVA_8_FOLDER;
        Files.createDirectories(folder);

        // Fetch correct URL
        URL url;
        boolean isTarFile = false;
        switch (SystemInfo.OS_NAME) {
            case "windows":
                if (SystemInfo.OS_BIT.equals("32")) {
                    url = config.getEndpoints().getJava8Win32();
                } else {
                    url = config.getEndpoints().getJava8Win64();
                }
                break;
            case "mac":
                url = config.getEndpoints().getJava8Mac();
                isTarFile = true;
                break;
            case "linux":
                url = config.getEndpoints().getJava8Linux();
                isTarFile = true;
                break;
            default:
                throw new IOException("Unrecognized systems do not support auto-download of legacy Java.");
        }

        // Download file
        Path compressedFile = folder.resolve("jre-1.8" + (isTarFile ? ".tar.gz" : ".zip"));
        if (!Files.exists(compressedFile)) {
            long size = Network.fetchFileSize(url);
            Network.download(url, compressedFile, progress -> downloadProgressConsumer.accept(size, progress));
        }
        stepProgress.accept(totalSteps, currentStep++);

        // Extract file
        Path javaPath;
        if (SystemInfo.OS_NAME.equals("windows")) {
            javaPath = folder.resolve("bin/java.exe");
        } else {
            javaPath = folder.resolve("bin/java");
        }
        if (!Files.exists(javaPath)) {
            // Create archive stream
            ArchiveInputStream archive;
            if (isTarFile) {
                // Extract tar from tar.gz
                Path tarPath = folder.resolve("jre-1.8.tar");
                if (!Files.exists(tarPath)) {
                    GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(Files.newInputStream(compressedFile)));
                    Files.copy(gzipInputStream, tarPath);
                    gzipInputStream.close();
                }

                // Create archive
                archive = new ArchiveStreamFactory().createArchiveInputStream("tar", new BufferedInputStream(Files.newInputStream(tarPath)));
            } else {
                // Create archive
                archive = new ArchiveStreamFactory().createArchiveInputStream("zip", new BufferedInputStream(Files.newInputStream(compressedFile)));
            }

            // Extract files
            ArchiveEntry entry;
            while ((entry = archive.getNextEntry()) != null) {

                // Extract file
                String path = entry.getName().substring(entry.getName().indexOf('/') + 1);
                Path entryPath = folder.resolve(path);
                if (!entry.isDirectory()) {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(archive, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // Close archive
            archive.close();
        }

        // Determine if the java file still doesn't exist, return if it still exists
        if (!Files.exists(javaPath)) {
            throw new IOException("Unable to find java location!");
        }
        stepProgress.accept(totalSteps, currentStep++);

        // Return the java path
        return javaPath;
    }

}
