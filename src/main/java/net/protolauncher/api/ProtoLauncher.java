package net.protolauncher.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.protolauncher.api.Config.FileLocation;
import net.protolauncher.function.DownloadProgressConsumer;
import net.protolauncher.function.StepInfoConsumer;
import net.protolauncher.function.StepProgressConsumer;
import net.protolauncher.gson.DurationTypeAdapter;
import net.protolauncher.gson.InstantTypeAdapter;
import net.protolauncher.mojang.Artifact;
import net.protolauncher.mojang.asset.Asset;
import net.protolauncher.mojang.asset.AssetIndex;
import net.protolauncher.mojang.library.Library;
import net.protolauncher.mojang.rule.Action;
import net.protolauncher.mojang.rule.Rule;
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
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
     * @param downloadProgress Called to show the download progress.
     * @return The loaded {@link VersionManifest}.
     * @throws IOException Thrown if something goes wrong loading or downloading the version manifest.
     */
    public VersionManifest loadVersionManifest(DownloadProgressConsumer downloadProgress) throws IOException {
        URL url = config.getEndpoints().getVersionManifest();
        Path path = FileLocation.VERSION_MANIFEST;

        // Check if it needs to be downloaded and, if it does, then download it
        Instant nextManifestUpdate = config.getLastManifestUpdate().plus(config.getMaxManifestAge());
        if (!Files.exists(path) || Instant.now().isAfter(nextManifestUpdate)) {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            long size = Network.fetchFileSize(url);
            Network.download(url, path, progress -> downloadProgress.accept(size, progress));
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
     * @param downloadProgress Called to show the download progress.
     * @return The loaded {@link Version}.
     * @throws IOException Thrown if something goes wrong loading or downloading the version.
     */
    public Version downloadVersion(VersionInfo info, DownloadProgressConsumer downloadProgress) throws IOException {
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
            Network.download(url, file, progress -> downloadProgress.accept(size, progress));
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
     * @param downloadProgress Called to show the download progress.
     * @throws IOException Thrown if something goes wrong downloading the client.
     */
    public void downloadVersionClient(Version version, DownloadProgressConsumer downloadProgress) throws IOException {
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
            Network.download(url, file, progress -> downloadProgress.accept(size, progress));
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
     * @param downloadProgress Called to show the download progress.
     * @return The {@link Path} to the Java executable.
     * @throws IOException Thrown if something goes wrong downloading Java.
     * @throws ArchiveException Thrown if something goes wrong during the extraction process.
     */
    public Path downloadJava(StepProgressConsumer stepProgress, DownloadProgressConsumer downloadProgress) throws IOException, ArchiveException {
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
            Network.download(url, compressedFile, progress -> downloadProgress.accept(size, progress));
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

    /**
     * Downloads all the libraries for the given {@link Version}.
     *
     * @param version The {@link Version} to download libraries for.
     * @param stepProgress Called for every library to give a total amount of steps.
     * @param stepInfo Called to provide the name of each library.
     * @param downloadProgress Called to show download progress.
     * @return A filtered and downloaded list of {@link Library}'s.
     * @throws IOException Thrown if something goes wrong downloading any library.
     */
    public List<Library> downloadLibraries(Version version, StepProgressConsumer stepProgress, StepInfoConsumer stepInfo, DownloadProgressConsumer downloadProgress) throws IOException {
        Path versionFolder = FileLocation.VERSIONS_FOLDER.resolve(version.getId() + "/");
        Path nativesFolder = versionFolder.resolve("natives/");
        Files.createDirectories(nativesFolder);

        // Filter libraries
        List<Library> libraries = version.getLibraries().stream().filter(library -> {
            // If there are no rules, return true
            if (library.getRules() == null || library.getRules().size() == 0) {
                return true;

            // If the rules resolve to allow, return true
            } else if (Rule.determine(library.getRules().toArray(Rule[]::new)) == Action.ALLOW) {
                return true;

            // Otherwise, return false
            } else {
                return false;
            }
        }).toList();

        // Main download loop
        final int totalSteps = libraries.size();
        int currentStep = 0;
        for (Library library : libraries) {
            // Update progress
            stepProgress.accept(totalSteps, currentStep++);
            stepInfo.accept(library.getNameDetails()[1]);

            // Don't try and download libraries that don't have downloads
            if (library.getDownloads() == null) {
                continue;
            }

            // Get library artifact
            Artifact jarArtifact = library.getDownloads().getArtifact();
            if (jarArtifact != null) {
                assert jarArtifact.getPath() != null; // This won't be null for a library jar

                // Download if it does not already exist
                Path jarPath = FileLocation.LIBRARIES_FOLDER.resolve(jarArtifact.getPath());
                if (!Files.exists(jarPath)) {
                    Files.createDirectories(jarPath.getParent());
                    URL url = new URL(jarArtifact.getUrl());
                    long size = jarArtifact.getSize();
                    Network.download(url, jarPath, progress -> downloadProgress.accept(size, progress));
                }

                // Validate
                if (config.shouldValidate() && !Validation.validate(jarPath, jarArtifact.getSha1())) {
                    // TODO: Retry download.
                    throw new IOException("Validation failed!");
                }
            }

            // Download and extract natives if they exist
            Artifact natArtifact = library.getTargetedNatives();
            if (natArtifact != null) {
                assert natArtifact.getPath() != null; // This won't be null for a native
                assert natArtifact.getUrl() != null; // This won't be null for a native

                // Download if it does not already exist
                Path natPath = FileLocation.LIBRARIES_FOLDER.resolve(natArtifact.getPath());
                if (!Files.exists(natPath)) {
                    Files.createDirectories(natPath.getParent());
                    URL url = new URL(natArtifact.getUrl());
                    long size = natArtifact.getSize();
                    Network.download(url, natPath, progress -> downloadProgress.accept(size, progress));
                }

                // Validate
                if (config.shouldValidate() && !Validation.validate(natPath, natArtifact.getSha1())) {
                    // TODO: Retry download.
                    throw new IOException("Validation failed!");
                }

                // Extract the native
                this.extractNative(natPath, nativesFolder, library.getExtract() != null ? library.getExtract().get("exclude") : null);
            }
        }

        // Filter the libraries to exclude any native-only libraries (so it only returns 'true' libraries)
        return libraries.stream().filter(library -> {
            return library.getDownloads() != null && library.getDownloads().getArtifact() != null;
        }).toList();
    }

    /**
     * Takes in a source jar file and extracts it to the destination path avoiding the exclusions list.
     *
     * @param source The source jar file
     * @param destination The destination directory
     * @param exclusions A list of file exclusions
     */
    private void extractNative(Path source, Path destination, @Nullable String[] exclusions) throws IOException {
        // Prepare jar file
        JarFile jar = new JarFile(source.toFile());
        Enumeration<JarEntry> entries = jar.entries();

        // Main extract loop
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            // Ignore non-dll files
            if (!entry.getName().endsWith(".dll")) {
                continue;
            }

            // Handle edge case where lwjgl.dll doesn't work with MC 1.16 if we don't remove the 64-bit version on a 32-bit PC
            if (SystemInfo.OS_ARCH.equals("x86") && entry.getName().equals("lwjgl.dll")) {
                continue;
            }

            // Create entry file location
            Path file = Paths.get(destination.toString(), entry.getName());

            // If the native already exists, ignore it and continue on
            if (Files.exists(file)) {
                continue;
            }

            // Process exclusions
            if (exclusions != null && exclusions.length > 0) {
                boolean exclude = false;
                for (String exclusion : exclusions) {
                    Path excludedFile = Path.of(exclusion);

                    // Compare the excluded filename to the jar file name and if they're equal don't extract
                    if (file.getFileName().equals(excludedFile.getFileName())) {
                        exclude = true;
                        break;

                        // Compare the excluded file parent's folder name to the jar file's folder name and if they're equal don't extract
                    } else if (file.getParent() != null && file.getParent().getFileName().equals(excludedFile.getFileName())) {
                        exclude = true;
                        break;
                    }
                }
                if (exclude) {
                    continue;
                }
            }

            // Copy native to destination
            Files.copy(jar.getInputStream(entry), file);
        }

        // Close jar file
        jar.close();
    }

    public AssetIndex downloadAssets(Version version, Path profileFolder, StepProgressConsumer stepProgress, StepInfoConsumer stepInfo, DownloadProgressConsumer downloadProgress) throws IOException {
        Path assetsFolder = FileLocation.ASSETS_FOLDER;
        Path objectsFolder = assetsFolder.resolve("objects/");
        Path virtualFolder = assetsFolder.resolve("virtual/legacy/");
        Path logConfigsFolder = assetsFolder.resolve("log_configs/");
        Path resourcesFolder = profileFolder.resolve("resources/");
        Path indexFile = assetsFolder.resolve("indexes/" + version.getAssetIndex().getId() + ".json");

        // Download the index file if it does not exist
        if (!Files.exists(indexFile)) {
            Files.createDirectories(indexFile.getParent());
            URL url = new URL(version.getAssetIndex().getUrl());
            Network.download(url, indexFile);
        }

        // Validate
        if (config.shouldValidate() && !Validation.validate(indexFile, version.getAssetIndex().getSha1())) {
            // TODO: Retry download.
            throw new IOException("Validation failed!");
        }

        // Parse the index file
        AssetIndex index = gson.fromJson(Files.newBufferedReader(indexFile), AssetIndex.class);

        // Create the directories if needed (so we're not checking every loop)
        boolean isVirtual = Boolean.TRUE.equals(index.isVirtual());
        if (isVirtual) {
            Files.createDirectories(virtualFolder);
        }
        boolean mapToResources = Boolean.TRUE.equals(index.mapToResources());
        if (mapToResources) {
            Files.createDirectories(resourcesFolder);
        }

        // Main download loop
        final int totalSteps = index.getObjects().entrySet().size() * 3 + 1; // 3 steps per asset, 1 log file download
        int currentStep = 0;
        for (Entry<String, Asset> entry : index.getObjects().entrySet()) {
            Asset asset = entry.getValue();

            // Update progress
            stepProgress.accept(totalSteps, currentStep++);
            stepInfo.accept(asset.getHash());

            // Download asset if it does not already exist
            String assetLocation = asset.getId() + "/" + asset.getHash();
            Path assetPath = objectsFolder.resolve(assetLocation);
            if (!Files.exists(assetPath)) {
                Files.createDirectories(assetPath.getParent());
                URL url = new URL(config.getEndpoints().getAssetApi() + assetLocation);
                long size = asset.getSize();
                Network.download(url, assetPath, progress -> downloadProgress.accept(size, progress));
            }

            // Update progress
            stepProgress.accept(totalSteps, currentStep++);

            // If the asset is virtual, copy the file to the virtual location
            if (isVirtual) {
                Path assetPathVirtual = virtualFolder.resolve(entry.getKey());
                if (!Files.exists(assetPathVirtual)) {
                    Files.createDirectories(assetPathVirtual.getParent());
                    Files.copy(assetPath, assetPathVirtual);
                }
            }

            // Update progress
            stepProgress.accept(totalSteps, currentStep++);

            // If map to resources, copy the file to the resources location
            if (mapToResources) {
                Path assetResourcesPath = resourcesFolder.resolve(entry.getKey());
                if (!Files.exists(assetResourcesPath)) {
                    Files.createDirectories(assetResourcesPath.getParent());
                    Files.copy(assetPath, assetResourcesPath);
                }
            }
        }

        // Update progress
        stepProgress.accept(totalSteps, currentStep++);
        stepInfo.accept("Logging Files");

        // Download log files
        if (version.getLogging() != null) {
            Artifact artifact = version.getLogging().getClient().getFile();
            Path logFilePath = logConfigsFolder.resolve(artifact.getId());
            if (!Files.exists(logFilePath)) {
                URL url = new URL(artifact.getUrl());
                long size = artifact.getSize();
                Network.download(url, logFilePath, progress -> downloadProgress.accept(size, progress));
            }
        }

        // Return the index
        return index;
    }

}
