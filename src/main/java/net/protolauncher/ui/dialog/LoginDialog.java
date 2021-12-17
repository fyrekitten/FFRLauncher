package net.protolauncher.ui.dialog;

import javafx.stage.Window;
import net.protolauncher.ui.ViewScene;
import net.protolauncher.ui.view.dialog.LoginDialogView;

public class LoginDialog extends Dialog {

    // Constructor
    public LoginDialog(Window owner) {
        super(owner, "ProtoLauncher: Add User", 600, 300, 600, 300);
        LoginDialogView view = new LoginDialogView(this);
        ((ViewScene) this.getScene()).addView(view);
    }

}
