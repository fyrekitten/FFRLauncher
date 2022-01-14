package net.protolauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import net.protolauncher.api.Profile;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.api.User;
import net.protolauncher.ui.ViewScene;
import net.protolauncher.ui.dialog.LaunchDialog;
import net.protolauncher.ui.view.dialog.LaunchDialogView;

import java.util.List;
import java.util.Objects;

public class AppCLI extends Application {

    // Constants
    public static final String VERSION = ProtoLauncher.getVersion();

    /**
     * Handles the startup of the JavaFX application.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(true);

        // Handle commands
        List<String> args = this.getParameters().getUnnamed();
        if (this.getParameters().getRaw().size() == 1 || args.contains("-h") || args.contains("--help") || args.contains("help")) {
            System.out.println("Commands:");
            System.out.printf("%-15s %s\n", "help", "Shows this help screen.");
            System.out.printf("%-15s %s\n", "version", "Prints the launcher version.");
            System.out.printf("%-15s %s\n", "launch", "Launches a profile. Run command with no parameters for usage.");
        } else if (args.contains("-v") || args.contains("--version") || args.contains("version")) {
            System.out.println("ProtoLauncher v" + VERSION);
        } else if (args.contains("-launch") || args.contains("--launch") || args.contains("launch")) {
            try {
                this.launch(primaryStage, this.getParameters());
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: Error popup.
                System.exit(0);
            }
            return;
        }

        // Exit
        System.exit(0);
    }

    /**
     * Launches the launcher using the profile given in the parameters.
     */
    private void launch(Stage primaryStage, Parameters params) throws Exception {
        if (!params.getNamed().containsKey("owner") || !params.getNamed().containsKey("uuid")) {
            System.out.println("Command Usage: launch --owner=<owner uuid> --uuid=<profile uuid>");
            System.out.println("Params:");
            System.out.printf("%-15s %s\n", "--owner=<owner uuid>", "The UUID of the owner of the profile to launch.");
            System.out.printf("%-15s %s\n", "--uuid=<profile uuid>", "The UUID of the profile to launch.");
            System.exit(0);
            return;
        }

        // Prepare ProtoLauncher
        ProtoLauncher launcher = new ProtoLauncher();
        launcher.loadConfig();
        launcher.loadVersionManifest((total, transferred) -> { });
        launcher.loadUsers();
        launcher.loadProfiles();

        // Fetch owner
        User owner = launcher.getUser(params.getNamed().get("owner"));
        if (owner == null) {
            throw new Exception("Invalid owner UUID: User not found!");
        }
        launcher.getLoggerWrapper().debug("Found profile owner " + owner.getUsername() + " (" + owner.getUuid() + ")");

        // Switch to user
        if (!Objects.equals(launcher.getConfig().getCurrentUserUuid(), owner.getUuid())) {
            launcher.switchUser(owner);
        }

        // Fetch Profile
        Profile profile = launcher.getProfile(owner.getUuid(), params.getNamed().get("uuid"));
        if (profile == null) {
            throw new Exception("Invalid profile UUID: Profile not found!");
        }
        launcher.getLoggerWrapper().debug("Found profile " + profile.getName() + " (" + profile.getUuid() + ")");

        // Switch to profile
        if (!Objects.equals(launcher.getConfig().getCurrentProfileUuid(), profile.getUuid())) {
            launcher.switchProfile(profile);
        }

        // Create a new app
        App app = new App();
        app.setStage(primaryStage);
        app.setLauncher(launcher);

        // Open launch dialog
        LaunchDialog dialog = new LaunchDialog(null);
        LaunchDialogView view = new LaunchDialogView(dialog, profile);
        ((ViewScene) dialog.getScene()).addView(view);
        dialog.show();
        view.performLaunch();
    }

}
