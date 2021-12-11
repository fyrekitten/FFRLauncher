package net.protolauncher;

import javafx.application.Application;
import javafx.stage.Stage;
import net.protolauncher.api.ProtoLauncher;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class App extends Application {

    // Constants
    public static final String VERSION = getLauncherVersion();

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
        this.stage = stage;
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
