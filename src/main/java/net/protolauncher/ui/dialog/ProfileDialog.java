package net.protolauncher.ui.dialog;

import javafx.stage.Window;
import net.protolauncher.ui.ViewScene;
import net.protolauncher.ui.view.dialog.ProfileDialogView;

public class ProfileDialog extends Dialog {

    // Constructor
    public ProfileDialog(Window owner) {
        super(owner, "ProtoLauncher: Profile Editor", 800, 450, 800, 450);
        ProfileDialogView view = new ProfileDialogView(this);
        ((ViewScene) this.getScene()).addView(view);
    }

}
