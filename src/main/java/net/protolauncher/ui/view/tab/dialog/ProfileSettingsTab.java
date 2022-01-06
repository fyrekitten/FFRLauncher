package net.protolauncher.ui.view.tab.dialog;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import net.protolauncher.App;
import net.protolauncher.api.Profile;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.ui.dialog.ProfileDialog;
import net.protolauncher.ui.view.AbstractView;

public class ProfileSettingsTab extends AbstractView<VBox> {

    // References
    private final ProfileDialog dialog;
    private final ProtoLauncher launcher;

    // Components
    private VBox vboxGlobalContainer;
    private Label lblGlobal;
    private CheckBox chkGlobal;

    // Constructor
    public ProfileSettingsTab(ProfileDialog dialog) {
        super(new VBox());
        this.dialog = dialog;
        this.launcher = App.getInstance().getLauncher();
        this.getLayout().setId("pst-layout");
        this.construct();
        this.register();
    }

    // Getters
    public CheckBox getGlobal() {
        return chkGlobal;
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Global Option
        vboxGlobalContainer = new VBox();
        vboxGlobalContainer.setId("pst-global-container");
        vboxGlobalContainer.getStyleClass().add("option");

        // Global Label
        lblGlobal = new Label("Global");
        lblGlobal.setId("pst-global-label");

        // Global CheckBox
        chkGlobal = new CheckBox("Global");
        chkGlobal.setId("pst-global");
        chkGlobal.setTooltip(new Tooltip("Whether this profile is visible for all users or not."));

        // Check for an existing profile and apply autofills
        Profile existingProfile = (Profile) this.dialog.getUserData();
        if (existingProfile != null) {
            chkGlobal.setSelected(existingProfile.getProfileSettings().isGlobal());
        }
    }

    @Override
    protected void register() {
        VBox layout = this.getLayout();
        vboxGlobalContainer.getChildren().addAll(lblGlobal, chkGlobal);
        layout.getChildren().add(vboxGlobalContainer);
    }

}
