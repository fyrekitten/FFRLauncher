package net.protolauncher;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.api.User;
import net.protolauncher.log4j.LogPassthroughAppender;
import net.protolauncher.ui.ViewScene;
import net.protolauncher.ui.task.LauncherTask;
import net.protolauncher.ui.view.InitializingView;
import net.protolauncher.ui.view.MainView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

public class App extends Application {

    // Constants
    public static final Logger LOGGER = LogManager.getLogger("JavaFX");
    public static final String VERSION = getLauncherVersion();
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 500;
    private static final int DEF_WIDTH = 900;
    private static final int DEF_HEIGHT = 600;

    // Singleton
    private static App instance;
    private Stage stage;
    private ProtoLauncher launcher;
    @Nullable
    private Process game;

    // Constructor
    public App() {
        instance = this;
    }

    // Getters
    public static App getInstance() {
        return instance;
    }
    public Stage getStage() {
        return stage;
    }
    public ProtoLauncher getLauncher() {
        return launcher;
    }
    @Nullable
    public Process getGame() {
        return game;
    }

    /**
     * Handles the startup of the JavaFX application.
     */
    @Override
    public void start(Stage stage) throws Exception {
        Platform.setImplicitExit(true);
        SvgImageLoaderFactory.install();
        try {
            // Create API
            launcher = new ProtoLauncher();

            // Prepare stage
            this.stage = stage;
            this.stage.setTitle("ProtoLauncher v" + App.VERSION);
            this.stage.setMinWidth(App.MIN_WIDTH);
            this.stage.setMinHeight(App.MIN_HEIGHT);
            this.stage.setWidth(App.DEF_WIDTH);
            this.stage.setHeight(App.DEF_HEIGHT);
            this.stage.getIcons().add(new Image(this.getRequiredResourceAsStream("icon.png")));

            // Load fonts
            this.loadFont("Raleway-Regular.ttf");
            this.loadFont("Raleway-Medium.ttf");
            this.loadFont("Raleway-SemiBold.ttf");
            this.loadFont("Raleway-Bold.ttf");
            this.loadFont("Roboto-Light.ttf");
            this.loadFont("Roboto-Bold.ttf");

            // Create main scene, set the view to a new loading view, set it to the stage, and show it
            ViewScene scene = new ViewScene();
            InitializingView initializingView = new InitializingView();
            scene.addView(initializingView);
            this.stage.setScene(scene);
            this.stage.show();

            // The task for initializing the launcher
            LauncherTask<Void> initializeTask = new LauncherTask<>() {
                @Override
                protected Void call() throws Exception {
                    final int totalSteps = 6;
                    int currentStep = 0;

                    // Load config
                    updateProgress(++currentStep, totalSteps);
                    launcher.loadConfig();

                    // Load version manifest
                    updateProgress(++currentStep, totalSteps);
                    int versionManifestStep = currentStep;
                    launcher.loadVersionManifest((total, transferred) -> {
                        updateProgress(versionManifestStep + (transferred / (double) total), totalSteps);
                    });

                    // Load users
                    updateProgress(++currentStep, totalSteps);
                    launcher.loadUsers();

                    // Load profiles
                    updateProgress(++currentStep, totalSteps);
                    launcher.loadProfiles();

                    // Validate current user
                    updateProgress(++currentStep, totalSteps);
                    User user = launcher.getCurrentUser();
                    if (user != null) {
                        boolean isValid = launcher.validateUser(user.getUuid());
                        // If we have an invalid user on launch, what do we do?
                    }

                    // Done
                    updateProgress(++currentStep, totalSteps);
                    return null;
                }
            };

            // Handle UI updates for the progress bar and messages
            initializeTask.setMessageHandler(LOGGER::info);
            initializeTask.setProgressHandler(progress -> initializingView.setProgress(progress.getWorkDone() / progress.getMax()));

            // Handle success
            initializeTask.setOnSucceeded(event -> {
                // Remove listener
                LogPassthroughAppender.removeListener(initializingView);

                // Set the scene to the main view
                scene.addView(new MainView());
                scene.removeView(initializingView);
            });

            // Handle failure
            initializeTask.setOnFailed(event -> {
                LOGGER.error(initializeTask.getException());
                initializeTask.getException().printStackTrace();
                System.exit(-1);
                // TODO: Show error popup window
            });

            // Run the initialization thread
            new Thread(initializeTask).start();
        } catch (Exception e) {
            // TODO: Show error popup window
            LOGGER.error(e);
            e.printStackTrace();
            Platform.exit();
        }
    }

    /**
     * Fetches the current scene from the {@link Stage} to be a {@link ViewScene}.
     * @return The {@link ViewScene} or null if the scene was not a {@link ViewScene}.
     */
    public ViewScene getSceneAsViewScene() {
        if (stage.getScene() instanceof ViewScene) {
            return (ViewScene) stage.getScene();
        } else{
            LOGGER.error("Scene was not a ViewScene!");
            return null;
        }
    }

    /**
     * Gets the resource and returns the corresponding URL.
     *
     * @param location The location of the resource as relative to <code>/</code>.
     * @return The {@link URL} for the resource.
     */
    public URL getRequiredResource(String location) {
        return Objects.requireNonNull(this.getClass().getResource("/" + location));
    }

    /**
     * Gets the resource and returns the corresponding input stream.
     *
     * @param location The location of the resource as relative to <code>/</code>.
     * @return The {@link InputStream} for the resource.
     */
    public InputStream getRequiredResourceAsStream(String location) {
        return Objects.requireNonNull(this.getClass().getResourceAsStream("/" + location));
    }

    /**
     * Loads the given font from resources by its filename.
     * @param name The filename of the font file as relative from /fonts/
     */
    private void loadFont(String name) {
        Font font = Font.loadFont(this.getRequiredResourceAsStream("fonts/" + name), 10);
        if (font == null) {
            LOGGER.error("Failed to load font " + name);
        } else {
            LOGGER.debug("Loaded font " + name);
        }
    }

    /**
     * Reads the maven.properties file and fetches the "version" property.
     * @return The version.
     */
    private static String getLauncherVersion() {
        try (InputStream stream = ProtoLauncher.class.getResourceAsStream("/maven.properties")) {
            Properties props = new Properties();
            props.load(stream);
            return props.getProperty("version");
        } catch (IOException e) {
            return "UNKNOWN";
        }
    }

}
