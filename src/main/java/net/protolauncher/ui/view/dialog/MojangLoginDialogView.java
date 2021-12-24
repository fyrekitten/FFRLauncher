package net.protolauncher.ui.view.dialog;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import net.protolauncher.App;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.api.User;
import net.protolauncher.ui.components.PLButton;
import net.protolauncher.ui.dialog.LoginDialog;
import net.protolauncher.ui.task.LauncherTask;
import net.protolauncher.ui.view.AbstractView;

import static net.protolauncher.App.LOGGER;

public class MojangLoginDialogView extends AbstractView<VBox> {

    // References
    private final LoginDialog dialog;
    private ProtoLauncher launcher;

    // Components
    private VBox vboxTopContainer;
    private Region regSeparator;
    private HBox hboxBottomContainer;
    private Label lblEmail;
    private TextField txtEmail;
    private Label lblPassword;
    private PasswordField pwdPassword;
    private Label lblStatus;
    private PLButton btnLogin;
    private Region btnSeparator;
    private PLButton btnCancel;

    // Constructor
    public MojangLoginDialogView(LoginDialog dialog) {
        super(new VBox(), "Colors.css", "Components.css", "view/dialog/LoginDialogView.css", "view/dialog/MojangLoginDialogView.css");
        this.dialog = dialog;
        this.construct();
        this.register();

        // Add login button keybind
        dialog.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.ENTER), () -> btnLogin.getButton().fire());
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Fetch launcher
        this.launcher = App.getInstance().getLauncher();

        // Top Container
        vboxTopContainer = new VBox();
        vboxTopContainer.setId("mldv-top-container");

        // Separator
        regSeparator = new Region();
        regSeparator.setId("mldv-separator");
        VBox.setVgrow(regSeparator, Priority.ALWAYS);

        // Bottom Container
        hboxBottomContainer = new HBox();
        hboxBottomContainer.setId("mldv-bottom-container");

        // Email Label
        lblEmail = new Label("Email or Username");
        lblEmail.setId("mldv-email-label");

        // Email Field
        txtEmail = new TextField();
        txtEmail.setId("mldv-email");

        // Password Label
        lblPassword = new Label("Password");
        lblPassword.setId("mldv-password-label");

        // Password Field
        pwdPassword = new PasswordField();
        pwdPassword.setId("mldv-password");

        // Status
        lblStatus = new Label("Ready to login.");
        lblStatus.getStyleClass().add("good");
        lblStatus.setId("mldv-status-label");

        // Login Button
        btnLogin = new PLButton("Login");
        btnLogin.getButton().setId("mldv-button-login");
        btnLogin.getButton().setOnAction(this::loginButtonPressed);

        // Separator
        btnSeparator = new Region();
        btnSeparator.setId("mldv-button-separator");

        // Cancel Button
        btnCancel = new PLButton("Cancel");
        btnCancel.getStyleClass().add("red");
        btnCancel.setId("mldv-button-cancel");
        btnCancel.getButton().setOnAction(this::cancelButtonPressed);
    }

    @Override
    protected void register() {
        VBox layout = this.getLayout();
        vboxTopContainer.getChildren().addAll(lblEmail, txtEmail, lblPassword, pwdPassword, lblStatus);
        layout.getChildren().add(vboxTopContainer);
        layout.getChildren().add(regSeparator);
        hboxBottomContainer.getChildren().addAll(btnLogin, btnSeparator, btnCancel);
        layout.getChildren().add(hboxBottomContainer);
    }

    /**
     * Handles the login button being pressed.
     */
    private void loginButtonPressed(ActionEvent event) {
        // Disable buttons
        btnLogin.setDisable(true);
        btnCancel.setDisable(true);
        txtEmail.setDisable(true);
        pwdPassword.setDisable(true);

        // Give status update
        lblStatus.getStyleClass().remove("bad");
        if (lblStatus.getStyleClass().contains("good")) {
            lblStatus.getStyleClass().add("good");
        }
        lblStatus.setText("Authenticating...");

        // The task for logging in a Mojang user.
        LauncherTask<User> loginTask = new LauncherTask<>() {
            @Override
            protected User call() throws Exception {
                return launcher.addUserMojang(txtEmail.getText(), pwdPassword.getText());
            }
        };

        // Handle success
        loginTask.setOnSucceeded(event1 -> {
            dialog.setUserData(loginTask.getValue());
            dialog.hide();
        });

        // Handle failure
        loginTask.setOnFailed(event1 -> {
            LOGGER.debug("Login failed: " + loginTask.getException().getMessage());
            lblStatus.setText("Login failed: " + loginTask.getException().getMessage());
            lblStatus.getStyleClass().remove("good");
            lblStatus.getStyleClass().add("bad");

            // Re-enable buttons
            btnLogin.setDisable(false);
            btnCancel.setDisable(false);
            txtEmail.setDisable(false);
            pwdPassword.setDisable(false);
        });

        // Run the login thread
        new Thread(loginTask).start();
    }

    /**
     * Handles the cancel button being pressed.
     */
    private void cancelButtonPressed(ActionEvent event) {
        dialog.hide();
    }

}
