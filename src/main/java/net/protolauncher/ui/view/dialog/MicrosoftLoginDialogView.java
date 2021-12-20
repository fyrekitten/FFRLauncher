package net.protolauncher.ui.view.dialog;

import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import net.protolauncher.App;
import net.protolauncher.api.Config.Endpoints;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.api.User;
import net.protolauncher.ui.dialog.LoginDialog;
import net.protolauncher.ui.task.LauncherTask;
import net.protolauncher.ui.view.AbstractView;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;

import static net.protolauncher.App.LOGGER;

public class MicrosoftLoginDialogView extends AbstractView<Pane> {

    // References
    private final LoginDialog dialog;

    // Components
    private WebView view;
    private WebEngine engine;

    // Constructor
    public MicrosoftLoginDialogView(LoginDialog dialog) {
        super(new Pane(), "Colors.css", "Components.css", "view/dialog/LoginDialogView.css");
        this.dialog = dialog;

        // Resize dialog
        double prevX = this.dialog.getX();
        double prevY = this.dialog.getY();
        double prevWidth = this.dialog.getWidth();
        double prevHeight = this.dialog.getHeight();
        this.dialog.setMinWidth(500);
        this.dialog.setMinHeight(600);
        this.dialog.setWidth(500);
        this.dialog.setHeight(600);
        this.dialog.setX(prevX + (prevWidth / 2) - (this.dialog.getWidth() / 2));
        this.dialog.setY(prevY + (prevHeight / 2) - (this.dialog.getHeight() / 2));

        // Bind width and height
        view.prefWidthProperty().bind(this.dialog.widthProperty());
        view.prefHeightProperty().bind(this.dialog.heightProperty());
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Fetch Microsoft URLs
        ProtoLauncher launcher = App.getInstance().getLauncher();
        Endpoints endpoints = launcher.getConfig().getEndpoints();
        String clientId = endpoints.getMicrosoftApi().getClientId();
        URL oauthUrl = endpoints.getMicrosoftApi().getOauthAuthorizeUrl();
        URL redirectUrl = endpoints.getMicrosoftApi().getRedirectUrl();

        // Cookie Manager
        CookieHandler.setDefault(new CookieManager());

        // Web View
        view = new WebView();

        // Web Engine
        engine = view.getEngine();
        engine.locationProperty().addListener(event -> {
            String location = engine.getLocation();
            try {
                URL url = new URL(location);
                if (location.contains("nativeclient") && url.getHost().equals(redirectUrl.getHost())) {
                    if (url.getQuery().contains("error=")) {
                        if (url.getQuery().contains("access_denied")) {
                            // User probably hit 'back' or closed it.
                            dialog.hide();
                        } else {
                            throw new Exception("There was an error while authenticating! Please contact the developer.");
                        }
                    } else if (url.getQuery().contains("code=")) {
                        this.completeLogin(location.substring(location.indexOf("?code=") + "?code=".length()));
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("Login failed: " + e.getMessage());
                e.printStackTrace();
                dialog.hide();
            }
        });

        // Load the URL
        engine.load(
            oauthUrl +
            "?client_id=" + clientId +
            "&response_type=code" +
            "&redirect_uri=" + redirectUrl.toString() +
            "&scope=XboxLive.signin%20offline_access" +
            "&prompt=select_account"
        );
    }

    @Override
    protected void register() {
        Pane layout = this.getLayout();
        layout.getChildren().add(view);
    }

    /**
     * Handles completion of the login once an authCode has been achieved.
     * @param authCode The auth code.
     */
    private void completeLogin(String authCode) {
        // The task for logging in a Microsoft user.
        LauncherTask<User> loginTask = new LauncherTask<>() {
            @Override
            protected User call() throws Exception {
                return App.getInstance().getLauncher().addUserMicrosoft(authCode);
            }
        };

        // Handle success
        loginTask.setOnSucceeded(event -> {
            dialog.setUserData(loginTask.getValue());
            dialog.hide();
        });

        // Handle failure
        loginTask.setOnFailed(event -> {
            LOGGER.debug("Login failed: " + loginTask.getException().getMessage());
            loginTask.getException().printStackTrace();
            dialog.hide();
        });

        // Run the login thread
        new Thread(loginTask).start();
    }

}

