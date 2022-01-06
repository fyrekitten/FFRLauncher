package net.protolauncher.ui.view.tab;

import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import net.protolauncher.App;
import net.protolauncher.api.Profile;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.api.User;
import net.protolauncher.mojang.version.VersionType;
import net.protolauncher.ui.ViewScene;
import net.protolauncher.ui.components.PLButton;
import net.protolauncher.ui.components.PLPlayButton;
import net.protolauncher.ui.dialog.LaunchDialog;
import net.protolauncher.ui.view.AbstractView;
import net.protolauncher.ui.view.dialog.LaunchDialogView;

import java.util.List;

public class PlayTab extends AbstractView<VBox> {

    // References
    private final ProtoLauncher launcher;

    // Components
    private Region regSpacer;
    private HBox hboxBtnContainer;
    private PLButton btnTest;
    private PLButton btnPlay;
    private PLButton btnMod;

    // Constructor
    public PlayTab() {
        super(new VBox());
        this.launcher = App.getInstance().getLauncher();
        this.getLayout().setId("plt-layout");
        this.construct();
        this.register();
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Fetch profiles
        User currentUser = launcher.getCurrentUser();
        List<Profile> userProfiles = null;
        if (currentUser != null) {
            userProfiles = launcher.getProfilesWithGlobals(currentUser.getUuid());
        }
        Profile snapshotProfile = null;
        if (userProfiles != null) {
            snapshotProfile = userProfiles.stream()
                    .filter(p -> (p.getVersion().getType() == VersionType.SNAPSHOT && p.getVersion().isLatest()))
                    .findFirst().orElse(null);
        }
        Profile releaseProfile = null;
        if (userProfiles != null) {
            releaseProfile = launcher.getCurrentProfile();
        }
        Profile modProfile = null;
        if (userProfiles != null) {
            // TODO: Modding profile.
        }

        // Spacer
        regSpacer = new Region();
        VBox.setVgrow(regSpacer, Priority.ALWAYS);

        // Button Container
        hboxBtnContainer = new HBox();
        hboxBtnContainer.setId("plt-button-container");

        // Test Button
        if (snapshotProfile != null) {
            btnTest = new PLPlayButton("LET'S TEST", "Snapshot " + snapshotProfile.getVersion().getMinecraft());
            btnTest.setId("plt-btn-test");
            Profile finalSnapshotProfile = snapshotProfile;
            btnTest.getButton().setOnAction(event -> this.testButtonPressed(event, finalSnapshotProfile));
        } else {
            btnTest = new PLButton("LET'S TEST");
            btnTest.setId("plt-btn-test");
            btnTest.setDisable(true);
        }

        // Play Button
        if (releaseProfile != null) {
            btnPlay = new PLPlayButton("LET'S PLAY", releaseProfile.getName());
            btnPlay.setId("plt-btn-play");
            Profile finalReleaseProfile = releaseProfile;
            btnPlay.getButton().setOnAction(event -> this.playButtonPressed(event, finalReleaseProfile));
        } else {
            btnPlay = new PLButton("LET'S PLAY");
            btnPlay.setId("plt-btn-play");
            btnPlay.setDisable(true);
        }

        // Mod Button
        if (modProfile != null) {
            // TODO: Modding profile.
        } else {
            btnMod = new PLButton("LET'S MOD");
            btnMod.setId("plt-btn-mod");
            btnMod.setDisable(true);
        }
    }

    @Override
    protected void register() {
        VBox layout = this.getLayout();
        layout.getChildren().add(regSpacer);
        hboxBtnContainer.getChildren().addAll(btnTest, btnPlay, btnMod);
        layout.getChildren().add(hboxBtnContainer);
    }

    /**
     * Handles the test button being pressed.
     */
    private void testButtonPressed(ActionEvent event, Profile profile) {
        this.launch(profile);
    }

    /**
     * Handles the play button being pressed.
     */
    private void playButtonPressed(ActionEvent event, Profile profile) {
        this.launch(profile);
    }

    /**
     * Handles the mod button being pressed.
     */
    private void modButtonPressed(ActionEvent event, Profile profile) {
        // TODO: Modding profile.
    }

    /**
     * Opens a launch dialog with the given profile.
     * @param profile The profile to launch.
     */
    private void launch(Profile profile) {
        LaunchDialog dialog = new LaunchDialog(App.getInstance().getStage());
        LaunchDialogView view = new LaunchDialogView(dialog, profile);
        ((ViewScene) dialog.getScene()).addView(view);
        dialog.show();
        view.performLaunch();
    }

}
