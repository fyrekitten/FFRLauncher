package net.protolauncher.ui.view.dialog;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import net.protolauncher.App;
import net.protolauncher.api.Profile;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.api.User;
import net.protolauncher.log4j.FeedbackLoggerWrapper;
import net.protolauncher.log4j.ILogListener;
import net.protolauncher.mojang.asset.AssetIndex;
import net.protolauncher.mojang.library.Library;
import net.protolauncher.mojang.version.Version;
import net.protolauncher.mojang.version.VersionInfo;
import net.protolauncher.ui.dialog.Alert;
import net.protolauncher.ui.dialog.LaunchDialog;
import net.protolauncher.ui.task.LauncherTask;
import net.protolauncher.ui.view.AbstractView;
import net.protolauncher.ui.view.dialog.AlertView.AlertButton;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static net.protolauncher.App.LOGGER;

public class LaunchDialogView extends AbstractView<VBox> implements ILogListener {

    // References
    private final LaunchDialog dialog;
    private final Profile profile;
    private final ProtoLauncher launcher;
    private final User currentUser;

    // Variables
    private double currentStep = 0.0;
    private final double totalSteps = 5.0;
    private boolean launching = false;
    private Version version;
    private Path javaPath = null;
    private List<Library> libraries;
    private AssetIndex assetIndex;

    // Components
    private ProgressBar pgbProgressBar1;
    private ProgressBar pgbProgressBar2;
    private ProgressBar pgbProgressBar3;
    private Label lblStatus;

    // Constructor
    public LaunchDialogView(LaunchDialog dialog, Profile profile) {
        super(new VBox(), "Colors.css", "Components.css", "view/dialog/LaunchDialogView.css");
        this.dialog = dialog;
        this.profile = profile;
        this.launcher = App.getInstance().getLauncher();
        this.currentUser = this.launcher.getCurrentUser();
        this.getLayout().setId("ladv-layout");
        this.construct();
        this.register();
        this.dialog.setOnHiding(event -> FeedbackLoggerWrapper.removeListener(this));
        FeedbackLoggerWrapper.registerListener(this);
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Progress Bar 1
        pgbProgressBar1 = new ProgressBar(0);
        pgbProgressBar1.setId("ladv-progress-bar-1");

        // Progress Bar 2
        pgbProgressBar2 = new ProgressBar(0);
        pgbProgressBar2.setId("ladv-progress-bar-2");

        // Progress Bar 3
        pgbProgressBar3 = new ProgressBar(0);
        pgbProgressBar3.setId("ladv-progress-bar-3");

        // Status Label
        lblStatus = new Label("Preparing...");
        lblStatus.setId("ladv-status-label");
    }

    @Override
    protected void register() {
        VBox layout = this.getLayout();
        layout.getChildren().addAll(pgbProgressBar3, pgbProgressBar2, pgbProgressBar1, lblStatus);
    }

    /**
     * Performs the launch.
     */
    public void performLaunch() {
        if (launching) {
            return;
        }
        launching = true;
        LOGGER.debug("Performing launch on profile " + profile.getName() + " (" + profile.getUuid() + ") for user " + currentUser.getUsername() + " (" + currentUser.getUuid() + ")...");
        this.internal_launchTask_downloadVersion();
    }

    /**
     * Task 1: Fetch the version.
     */
    private void internal_launchTask_downloadVersion() {
        LOGGER.info("Launch Task: Fetch Version");
        pgbProgressBar1.setProgress(++currentStep / totalSteps);

        // Mark the profile as the last launched profile
        LOGGER.debug("Marking profile as last launched...");
        try {
            profile.setLastLaunched(Instant.now());
            launcher.updateProfile(profile);
        } catch (IOException e) {
            this.internal_launchFailed(e);
            return;
        }

        // Download version task
        LauncherTask<Version> downloadVersionTask = new LauncherTask<>() {
            @Override
            protected Version call() throws Exception {
                // Get version info
                VersionInfo versionInfo = Objects.requireNonNull(launcher.getVersionManifest()).getVersion(profile.getVersion().getMinecraft());

                // Download the version itself
                Version version1 = launcher.downloadVersion(Objects.requireNonNull(versionInfo), (total, transferred) -> {
                    this.updateProgress(transferred, total);
                });

                // Download the client jar
                launcher.downloadVersionClient(version1, (total, transferred) -> {
                    this.updateProgress(transferred, total);
                });

                // Download and return version
                return version1;
            }
        };

        // Handle success
        downloadVersionTask.setOnSucceeded(event -> {
            version = downloadVersionTask.getValue();
            this.internal_launchTask_downloadJava();
        });

        // Handle failure
        downloadVersionTask.setOnFailed(event -> this.internal_launchFailed(downloadVersionTask.getException()));

        // Handle progress updates
        downloadVersionTask.setProgressHandler(progress -> pgbProgressBar2.setProgress(progress.getWorkDone() / progress.getMax()));

        // Run the download version thread
        Thread downloadVersionThread = new Thread(downloadVersionTask);
        downloadVersionThread.setName("Download Version Task");
        downloadVersionThread.start();
    }

    /**
     * Task 2: Download Java.
     */
    private void internal_launchTask_downloadJava() {
        LOGGER.info("Launch Task: Download Java (if necessary)");
        pgbProgressBar1.setProgress(++currentStep / totalSteps);
        pgbProgressBar2.setProgress(0);
        pgbProgressBar3.setProgress(0);

        // Check if we even need to
        if (!version.getAssets().equals("pre-1.6")) {
            LOGGER.info("Downloading Java is not necessary as this version is compatible with post-1.8 Java versions.");
            this.internal_launchTask_downloadLibraries();
            return;
        }

        // Download Java task
        LauncherTask<Path> downloadJavaTask = new LauncherTask<>() {
            @Override
            protected Path call() throws Exception {
                return launcher.downloadJava((totalSteps1, currentStep1) -> {
                    this.updateProgress(currentStep1, totalSteps1);
                }, (total, transferred) -> {
                    this.updateProgress2(transferred, total);
                });
            }
        };

        // Handle success
        downloadJavaTask.setOnSucceeded(event -> {
            javaPath = downloadJavaTask.getValue();
            this.internal_launchTask_downloadLibraries();
        });

        // Handle failure
        downloadJavaTask.setOnFailed(event -> this.internal_launchFailed(downloadJavaTask.getException()));

        // Handle progress updates
        downloadJavaTask.setProgressHandler(progress -> pgbProgressBar2.setProgress(progress.getWorkDone() / progress.getMax()));
        downloadJavaTask.setProgressHandler2(progress -> pgbProgressBar3.setProgress(progress.getWorkDone() / progress.getMax()));

        // Run the download java thread
        Thread downloadJavaThread = new Thread(downloadJavaTask);
        downloadJavaThread.setName("Download Java Task");
        downloadJavaThread.start();
    }

    /**
     * Task 3: Download libraries.
     */
    private void internal_launchTask_downloadLibraries() {
        LOGGER.info("Launch Task: Download Libraries");
        pgbProgressBar1.setProgress(++currentStep / totalSteps);
        pgbProgressBar2.setProgress(0);
        pgbProgressBar3.setProgress(0);

        // Download libraries task
        LauncherTask<List<Library>> downloadLibrariesTask = new LauncherTask<>() {
            @Override
            protected List<Library> call() throws Exception {
                return launcher.downloadLibraries(version, (totalSteps1, currentStep1) -> {
                    this.updateProgress(currentStep1, totalSteps1);
                }, info -> {
                    LOGGER.info("Downloading " + info + "...");
                }, (total, transferred) -> {
                    this.updateProgress2(transferred, total);
                });
            }
        };

        // Handle success
        downloadLibrariesTask.setOnSucceeded(event -> {
            libraries = downloadLibrariesTask.getValue();
            this.internal_launchTask_downloadAssets();
        });

        // Handle failure
        downloadLibrariesTask.setOnFailed(event -> this.internal_launchFailed(downloadLibrariesTask.getException()));

        // Handle progress updates
        downloadLibrariesTask.setProgressHandler(progress -> pgbProgressBar2.setProgress(progress.getWorkDone() / progress.getMax()));
        downloadLibrariesTask.setProgressHandler2(progress -> pgbProgressBar3.setProgress(progress.getWorkDone() / progress.getMax()));

        // Run the download libraries thread
        Thread downloadLibrariesThread = new Thread(downloadLibrariesTask);
        downloadLibrariesThread.setName("Download Libraries Task");
        downloadLibrariesThread.start();
    }

    /**
     * Task 4: Download assets.
     */
    private void internal_launchTask_downloadAssets() {
        LOGGER.info("Launch Task: Download Assets");
        pgbProgressBar1.setProgress(++currentStep / totalSteps);
        pgbProgressBar2.setProgress(0);
        pgbProgressBar3.setProgress(0);

        // Download assets task
        LauncherTask<AssetIndex> downloadAssetsTask = new LauncherTask<>() {
            @Override
            protected AssetIndex call() throws Exception {
                return launcher.downloadAssets(version, Path.of(profile.getPath()), (totalSteps1, currentStep1) -> {
                    this.updateProgress(currentStep1, totalSteps1);
                }, info -> {
                    LOGGER.info("Downloading " + info + "...");
                }, (total, transferred) -> {
                    this.updateProgress2(transferred, total);
                });
            }
        };

        // Handle success
        downloadAssetsTask.setOnSucceeded(event -> {
            assetIndex = downloadAssetsTask.getValue();
            this.internal_launchTask_launch();
        });

        // Handle failure
        downloadAssetsTask.setOnFailed(event -> this.internal_launchFailed(downloadAssetsTask.getException()));

        // Handle progress updates
        downloadAssetsTask.setProgressHandler(progress -> pgbProgressBar2.setProgress(progress.getWorkDone() / progress.getMax()));
        downloadAssetsTask.setProgressHandler2(progress -> pgbProgressBar3.setProgress(progress.getWorkDone() / progress.getMax()));

        // Run the download libraries thread
        Thread downloadAssetsThread = new Thread(downloadAssetsTask);
        downloadAssetsThread.setName("Download Assets Task");
        downloadAssetsThread.start();
    }

    /**
     * Task 5: Launch!
     */
    private void internal_launchTask_launch() {
        LOGGER.info("Launch Task: Launch");
        pgbProgressBar1.setProgress(++currentStep / totalSteps);
        pgbProgressBar2.setProgress(0);
        pgbProgressBar3.setProgress(0);

        // Launch task
        Task<Process> launchTask = new Task<>() {
            @Override
            protected Process call() throws Exception {
                return launcher.launch(
                    currentUser,
                    profile,
                    version,
                    libraries,
                    assetIndex,
                    javaPath,
                    App.VERSION
                );
            }
        };

        // Handle success
        launchTask.setOnSucceeded(event -> {
            // Set process
            App.getInstance().setGame(launchTask.getValue());

            // TODO: Setting; close launcher or no?
            dialog.setUserData(Boolean.TRUE);
            dialog.hide();
            App.getInstance().getStage().hide();
        });

        // Handle failure
        launchTask.setOnFailed(event -> this.internal_launchFailed(launchTask.getException()));

        // Run the launch thread
        Thread launchThread = new Thread(launchTask);
        launchThread.setName("Launch Task");
        launchThread.start();
    }

    /**
     * Handle a failed launch.
     * @param e The error.
     */
    private void internal_launchFailed(Throwable e) {
        LOGGER.error("Launch failed! " + e.getMessage());
        e.printStackTrace();
        Alert alert = new Alert(
            dialog,
        "ProtoLauncher: Launch Failed",
        "There was an error while attempting to launch this profile. You can either try again, or contact the developers if the issue is recurring.",
            e,
            EnumSet.of(AlertButton.OKAY_BAD, AlertButton.RETRY)
        );
        alert.setOnHidden(event -> {
            if (alert.getUserData() != null && alert.getUserData() instanceof AlertButton && alert.getUserData() == AlertButton.RETRY) {
                this.launching = false;
                this.performLaunch();
            } else {
                Platform.runLater(dialog::close);
            }
        });
        alert.show();
    }

    @Override
    public void onLog(String message) {
        Platform.runLater(() -> lblStatus.setText(message));
    }

}
