package net.protolauncher.ui.view.dialog;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.protolauncher.ui.ViewScene;
import net.protolauncher.ui.components.PLButton;
import net.protolauncher.ui.dialog.LoginDialog;
import net.protolauncher.ui.view.AbstractView;

public class LoginDialogView extends AbstractView<HBox> {

    // References
    private final LoginDialog dialog;

    // Components
    private VBox vboxContainer;
    private PLButton btnMojang;
    private PLButton btnMicrosoft;

    // Constructor
    public LoginDialogView(LoginDialog dialog) {
        super(new HBox(), "Colors.css", "Components.css", "view/dialog/LoginDialogView.css");
        this.dialog = dialog;
        this.getLayout().setId("ldv-layout");
        this.construct();
        this.register();
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Container
        vboxContainer = new VBox();
        vboxContainer.setId("ldv-container");

        // Mojang
        btnMojang = new PLButton("Mojang Login");
        btnMojang.getButton().setId("ldv-button-mojang");
        btnMojang.getButton().setOnAction(this::mojangButtonPressed);

        // Microsoft
        btnMicrosoft = new PLButton("Microsoft Login");
        btnMicrosoft.getButton().setId("ldv-button-microsoft");
        btnMicrosoft.getButton().setOnAction(this::microsoftButtonPressed);
    }

    @Override
    protected void register() {
        HBox layout = this.getLayout();
        vboxContainer.getChildren().addAll(btnMicrosoft, btnMojang);
        layout.getChildren().add(vboxContainer);
    }

    /**
     * Handles the Mojang button being pressed.
     */
    private void mojangButtonPressed(ActionEvent event) {
        Scene scene = dialog.getScene();
        if (scene instanceof ViewScene viewScene) {
            viewScene.addView(new MojangLoginDialogView(dialog));
            viewScene.removeView(this);
        }
    }

    /**
     * Handles the Microsoft button being pressed.
     */
    private void microsoftButtonPressed(ActionEvent event) {
        Scene scene = dialog.getScene();
        if (scene instanceof ViewScene viewScene) {
            viewScene.addView(new MicrosoftLoginDialogView(dialog));
            viewScene.removeView(this);
        }
    }

}
