package net.protolauncher.ui.view.dialog;

import net.protolauncher.ui.dialog.ProfileDialog;
import net.protolauncher.ui.view.AbstractTabView;
import net.protolauncher.ui.view.tab.dialog.ProfileInfoTab;

public class ProfileDialogView extends AbstractTabView {

    // References
    private final ProfileDialog dialog;

    // Constructor
    public ProfileDialogView(ProfileDialog dialog) {
        super("Colors.css", "Components.css", "view/dialog/ProfileDialogView.css");
        this.dialog = dialog;
        this.getLayout().setId("pdv-layout");
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Call super
        super.construct();

        // Tabs
        this.constructTab("it", "info", "Info", new ProfileInfoTab());

        // Switch to the default tab
        this.switchTab("info", true);
    }

}