package net.protolauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.ui.MainScene;
import net.protolauncher.ui.views.LoadingView;
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
        try {
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
            MainScene scene = new MainScene();
            scene.setView(new LoadingView());
            this.stage.setScene(scene);
            this.stage.show();
        } catch (Exception e) {
            // TODO: Show error popup window.
            LOGGER.error(e);
            e.printStackTrace();
            Platform.exit();
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
