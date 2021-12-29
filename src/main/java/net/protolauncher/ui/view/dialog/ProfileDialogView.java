package net.protolauncher.ui.view.dialog;

import net.protolauncher.ui.dialog.ProfileDialog;
import net.protolauncher.ui.view.AbstractTabView;
import net.protolauncher.ui.view.tab.dialog.ProfileInfoTab;

public class ProfileDialogView extends AbstractTabView {

    // References
    private final ProfileDialog dialog;

    // Constructor
    public ProfileDialogView(ProfileDialog dialog) {
        super("Colors.css",
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
        // Call super
        super.construct();

        // Tabs
        this.constructTab("it", "info", "Info", new ProfileInfoTab(dialog));

        // Switch to the default tab
        this.switchTab("info", true);
    }

}