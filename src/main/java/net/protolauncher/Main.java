package net.protolauncher;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import javafx.application.Application;
import javafx.application.Platform;

/**
 * This class is necessary to ensure that Maven dependencies
 * get injected into the runtime correctly, and allows IDEs
 * to handle importing much easier than a complicated javafx:run
 * setup with Maven; excluding the fact IntelliJ doesn't
 * properly handle importing libraries when short-cutting
 * the main method for JavaFX anyways.
 */
public class Main {

    private static final String APPLICATION_ID = "FE80177C-CC8D-4E13-BF99-B3149890FB43";

    public static void main(String[] args) {
        boolean alreadyRunning;
        try {
            JUnique.acquireLock(APPLICATION_ID, s-> {
                if (App.getInstance() != null) {
                    App app = App.getInstance();
                    if (app.getStage() != null) {
                        Platform.runLater(() -> {
                            app.getStage().setAlwaysOnTop(true);
                            app.getStage().setAlwaysOnTop(false);
                            app.getStage().requestFocus();
                        });
                    }
                }
                return null;
            });
            alreadyRunning = false;
        } catch (AlreadyLockedException e) {
            JUnique.sendMessage(APPLICATION_ID, "OPEN_REQUEST");
            alreadyRunning = true;
        }
        if (!alreadyRunning) {
            if (args.length > 0 && (args[0].equals("cli") || args[0].contains("protolauncher://"))) {
                Application.launch(AppCLI.class, args);
            } else {
                Application.launch(App.class, args);
            }
        } else {
            System.exit(0);
        }
    }

}
