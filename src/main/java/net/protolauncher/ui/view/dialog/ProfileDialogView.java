package net.protolauncher.ui.view.dialog;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.layout.BorderPane;
import net.protolauncher.App;
import net.protolauncher.api.Profile;
import net.protolauncher.api.Profile.Version;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.ui.dialog.Alert;
import net.protolauncher.ui.dialog.ProfileDialog;
import net.protolauncher.ui.view.AbstractButtonView;
import net.protolauncher.ui.view.AbstractTabView;
import net.protolauncher.ui.view.AbstractView;
import net.protolauncher.ui.view.dialog.AlertView.AlertButton;
import net.protolauncher.ui.view.tab.dialog.ProfileInfoTab;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Objects;

import static net.protolauncher.App.LOGGER;

public class ProfileDialogView extends AbstractView<BorderPane> {

    // References
    private final ProfileDialog dialog;

    // Components
    private ProfileDialogTabView atvTabs;
    private ProfileDialogButtonView abvButtons;

    // Constructor
    public ProfileDialogView(ProfileDialog dialog) {
        super(new BorderPane(),
            "Colors.css",
            "Components.css",
            "view/dialog/ProfileDialogView.css",
            "view/tab/dialog/ProfileInfoTab.css"
        );
        this.dialog = dialog;
        this.getLayout().setId("pdv-layout");
        this.construct();
        this.register();
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        this.atvTabs = new ProfileDialogTabView(dialog);
        this.abvButtons = new ProfileDialogButtonView(dialog);
    }

    @Override
    protected void register() {
        BorderPane layout = this.getLayout();
        layout.setCenter(atvTabs.getLayout());
        layout.setBottom(abvButtons.getLayout());
    }

    /**
     * Handles the save button being pressed.
     */
    private void saveButtonPressed(ActionEvent event) {
        // Disable buttons
        abvButtons.getButton("save").setDisable(true);
        abvButtons.getButton("cancel").setDisable(true);

        // Save profile and close
        ProtoLauncher launcher = App.getInstance().getLauncher();
        Profile profile = (Profile) dialog.getUserData();

        // If there's no profile, create one.
        if (profile == null) {
            // Get minimum data, since we'll perform an update shortly
            ProfileInfoTab pit = (ProfileInfoTab) atvTabs.getTab("info");
            if (launcher.getConfig().getCurrentUserUuid() == null) {
                // We can't make a profile if there's not a current user, silly
                dialog.setUserData(Boolean.FALSE);
                dialog.hide();
                return;
            }
            try {
                profile = new Profile(pit.getName(), pit.getVersion(), Objects.requireNonNull(launcher.getCurrentUser()));
            } catch (IOException e) {
                // Man, stuff must be really going wrong. Just abort.
                e.printStackTrace();
                dialog.setUserData(Boolean.FALSE);
                dialog.hide();
                return;
            }
        }

        // The task for saving a profile.
        Profile finalProfile = profile;
        Task<Void> saveProfileTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Update data from all tabs
                ProfileInfoTab pit = (ProfileInfoTab) atvTabs.getTab("info");
                if (!finalProfile.getName().equals(pit.getName())) {
                    finalProfile.setName(pit.getName());
                }
                if (!finalProfile.getVersion().getMinecraft().equals(pit.getVersion().getId())) {
                    Version ver = finalProfile.getVersion();
                    ver.setVersion(pit.getVersion());
                    finalProfile.setVersion(ver);
                }

                // Update profile
                launcher.updateProfile(finalProfile);
                return null;
            }
        };

        // Handle success
        saveProfileTask.setOnSucceeded(event1 -> {
            dialog.setUserData(Boolean.TRUE);
            dialog.hide();
        });

        // Handle failure
        saveProfileTask.setOnFailed(event1 -> {
            LOGGER.debug("Profile update failed: " + saveProfileTask.getException().getMessage());
            dialog.setUserData(Boolean.FALSE);
            dialog.hide();
        });

        // Run the save thread
        Thread saveProfileThread = new Thread(saveProfileTask);
        saveProfileThread.setName("Save Profile Task");
        saveProfileThread.start();
    }

    /**
     * Handles the cancel button being pressed.
     */
    private void cancelButtonPressed(ActionEvent event) {
        dialog.setUserData(Boolean.FALSE);
        dialog.hide();
    }

    /**
     * Handles the delete button being pressed.
     */
    private void deleteButtonPressed(ActionEvent event) {
        // Disable buttons
        abvButtons.getButton("save").setDisable(true);
        abvButtons.getButton("cancel").setDisable(true);

        // Confirmation
        Alert alert = new Alert(
            dialog,
            "ProtoLauncher: Confirm Deletion",
            "Are you sure you want to delete this profile?",
            null,
            EnumSet.of(AlertButton.YES_BAD, AlertButton.NO)
        );
        alert.setOnHidden(event1 -> {
            if (alert.getUserData() != null && alert.getUserData() instanceof AlertButton && alert.getUserData() == AlertButton.YES) {
                // Deletion confirmed; delete profile and close.
                ProtoLauncher launcher = App.getInstance().getLauncher();
                Profile profile = (Profile) dialog.getUserData();

                // If there's no profile, just close
                if (profile == null) {
                    dialog.setUserData(Boolean.FALSE);
                    dialog.hide();
                    return;
                }

                // The task for deleting a profile.
                Task<Void> deleteProfileTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        launcher.removeProfile(profile);
                        return null;
                    }
                };

                // Handle success
                deleteProfileTask.setOnSucceeded(event2 -> {
                    dialog.setUserData(Boolean.TRUE);
                    dialog.hide();
                });

                // Handle failure
                deleteProfileTask.setOnFailed(event2 -> {
                    LOGGER.debug("Profile delete failed: " + deleteProfileTask.getException().getMessage());
                    dialog.setUserData(Boolean.FALSE);
                    dialog.hide();
                });

                // Run the delete thread
                Thread deleteProfileThread = new Thread(deleteProfileTask);
                deleteProfileThread.setName("Delete Profile Task");
                deleteProfileThread.start();
            } else {
                abvButtons.getButton("save").setDisable(false);
                abvButtons.getButton("cancel").setDisable(false);
            }
        });
        alert.show();
    }

    /**
     * Represents the tab view for the profile dialog view.
     */
    public class ProfileDialogTabView extends AbstractTabView {

        // References
        private final ProfileDialog dialog;

        // Constructor
        public ProfileDialogTabView(ProfileDialog dialog) {
            super();
            this.dialog = dialog;
            this.construct();
            this.register();
        }

        // AbstractView Implementation
        @Override
        protected void construct() {
            // Call super
            super.construct();

            // Tabs
            this.constructTab("it", "info", "Info", new ProfileInfoTab(dialog));

            // Switch to the default tab
            this.switchTab("info", true);
        }

    }

    /**
     * Represents the button view for the profile dialog view.
     */
    public class ProfileDialogButtonView extends AbstractButtonView {

        // References
        private final ProfileDialog dialog;

        // Constructor
        public ProfileDialogButtonView(ProfileDialog dialog) {
            super();
            this.dialog = dialog;
            this.getLayout().setId("pdbv-layout");
            this.construct();
            this.register();
        }

        // AbstractView Implementation
        @Override
        protected void construct() {
            // Call super
            super.construct();

            // Buttons
            if (dialog.getUserData() != null) {
                this.constructButton("pdv-button-delete", "delete", "Delete", ProfileDialogView.this::deleteButtonPressed);
                this.getButton("delete").getButton().getStyleClass().add("red");
            }
            this.constructButton("pdv-button-cancel", "cancel", "Cancel", ProfileDialogView.this::cancelButtonPressed);
            this.constructButton("pdv-button-save", "save", "Save", ProfileDialogView.this::saveButtonPressed);
            this.getButton("cancel").getButton().getStyleClass().add("red");
        }

    }

}