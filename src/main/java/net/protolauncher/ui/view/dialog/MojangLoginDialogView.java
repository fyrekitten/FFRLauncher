package net.protolauncher.ui.view.dialog;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import net.protolauncher.App;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.api.User;
import net.protolauncher.ui.dialog.LoginDialog;
import net.protolauncher.ui.task.LauncherTask;
import net.protolauncher.ui.view.AbstractButtonView;
import net.protolauncher.ui.view.AbstractView;

import static net.protolauncher.App.LOGGER;

public class MojangLoginDialogView extends AbstractView<VBox> {

    // References
    private final LoginDialog dialog;
    private ProtoLauncher launcher;

    // Components
    private VBox vboxTopContainer;
    private Label lblEmail;
    private TextField txtEmail;
    private Label lblPassword;
    private PasswordField pwdPassword;
    private Label lblStatus;
    private Region regSeparator;
    private MojangLoginDialogButtonView abvButtons;

    // Constructor
    public MojangLoginDialogView(LoginDialog dialog) {
        super(new VBox(), "Colors.css", "Components.css", "view/dialog/LoginDialogView.css", "view/dialog/MojangLoginDialogView.css");
        this.dialog = dialog;
        this.construct();
        this.register();

        // Add login button keybind
        dialog.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.ENTER), () -> abvButtons.getButton("login").getButton().fire());
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

        // Buttons
        abvButtons = new MojangLoginDialogButtonView();
    }

    @Override
    protected void register() {
        VBox layout = this.getLayout();
        vboxTopContainer.getChildren().addAll(lblEmail, txtEmail, lblPassword, pwdPassword, lblStatus);
        layout.getChildren().add(vboxTopContainer);
        layout.getChildren().add(regSeparator);
        layout.getChildren().add(abvButtons.getLayout());
    }

    /**
     * Handles the login button being pressed.
     */
    private void loginButtonPressed(ActionEvent event) {
        // Disable buttons
        abvButtons.getButton("login").setDisable(true);
        abvButtons.getButton("cancel").setDisable(true);
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
            abvButtons.getButton("login").setDisable(false);
            abvButtons.getButton("cancel").setDisable(false);
            txtEmail.setDisable(false);
            pwdPassword.setDisable(false);
        });

        // Run the login thread
        Thread loginThread = new Thread(loginTask);
        loginThread.setName("Login Task");
        loginThread.start();
    }

    /**
     * Handles the cancel button being pressed.
     */
    private void cancelButtonPressed(ActionEvent event) {
        dialog.hide();
    }

    /**
     * Represents the button view for the mojang login dialog view.
     */
    public class MojangLoginDialogButtonView extends AbstractButtonView {

        // Constructor
        public MojangLoginDialogButtonView() {
            super();
            this.getLayout().setId("mldbv-layout");
            this.construct();
            this.register();
        }

        // AbstractView Implementation
        @Override
        protected void construct() {
            // Call super
            super.construct();

            // Buttons
            this.constructButton("mldv-button-login", "login", "Login", MojangLoginDialogView.this::loginButtonPressed);
            this.constructButton("mldv-button-cancel", "cancel", "Cancel", MojangLoginDialogView.this::cancelButtonPressed);

            // Add "red" to cancel button
            this.getButton("cancel").getButton().getStyleClass().add("red");
        }

    }

}
