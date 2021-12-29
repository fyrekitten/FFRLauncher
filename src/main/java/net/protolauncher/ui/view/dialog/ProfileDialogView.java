package net.protolauncher.ui.view.dialog;

import javafx.event.ActionEvent;
import javafx.scene.layout.BorderPane;
import net.protolauncher.ui.dialog.ProfileDialog;
import net.protolauncher.ui.view.AbstractButtonView;
import net.protolauncher.ui.view.AbstractTabView;
import net.protolauncher.ui.view.AbstractView;
import net.protolauncher.ui.view.tab.dialog.ProfileInfoTab;

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
        System.out.println("Save button pressed!");
    }

    /**
     * Handles the cancel button being pressed.
     */
    private void cancelButtonPressed(ActionEvent event) {
        System.out.println("Cancel button pressed!");
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
            this.constructButton("pdv-button-cancel", "cancel", "Cancel", ProfileDialogView.this::cancelButtonPressed);
            this.constructButton("pdv-button-save", "save", "Save", ProfileDialogView.this::saveButtonPressed);


            // Add "red" to cancel button
            this.getButton("cancel").getButton().getStyleClass().add("red");
        }

    }

}