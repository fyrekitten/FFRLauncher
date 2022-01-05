package net.protolauncher.ui.view;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.protolauncher.App;
import net.protolauncher.api.Profile;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.api.User;
import net.protolauncher.ui.ViewScene;
import net.protolauncher.ui.dialog.LoginDialog;
import net.protolauncher.ui.dialog.ProfileDialog;
import net.protolauncher.ui.view.dialog.LoginDialogView;
import net.protolauncher.ui.view.dialog.ProfileDialogView;
import net.protolauncher.ui.view.tab.*;

import java.io.IOException;

public class MainView extends AbstractTabView {

    // References
    private final ProtoLauncher launcher;

    // Components
    private VBox vboxAddButtonContainer;
    private Button btnAddButton;
    private VBox vboxAttributionContainer;
    private Label lblAttribution;

    // Constructor
    public MainView() {
        super("Colors.css",
            "Components.css",
            "view/MainView.css",
            "view/tab/PlayTab.css",
            "view/tab/ProfilesTab.css",
            "view/tab/UsersTab.css"
        );
        this.launcher = App.getInstance().getLauncher();
        this.getLayout().setId("mv-layout");
        this.construct();
        this.register();
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Call super
        super.construct();

        // Get current user
        User currentUser = launcher.getCurrentUser();
        Profile currentProfile = currentUser != null ? launcher.getCurrentProfile() : null;

        // Tabs
        this.constructTab("pt", "play", "Play", new PlayTab());
        if (currentProfile == null) {
            this.constructTab("nprt", "profiles", "Profiles", new NoProfilesTab());
        } else {
            this.constructTab("prt", "profiles", "Profiles", new ProfilesTab());
        }
        if (currentUser == null) {
            this.constructTab("nut", "users", "No Users", new NoUsersTab());
        } else {
            this.constructTab("ut", "users", currentUser.getUsername(), new UsersTab());
        }

        // Attribution Container
        vboxAttributionContainer = new VBox();
        vboxAttributionContainer.setId("mv-attribution-container");
        vboxAttributionContainer.prefWidthProperty().bind(this.getLayout().widthProperty());
        vboxAttributionContainer.prefHeightProperty().bind(this.getLayout().heightProperty());
        vboxAttributionContainer.setMouseTransparent(true);

        // Attribution
        lblAttribution = new Label("ProtoLauncher is NOT an official Minecraft project and is NOT approved by or associated with Mojang.");
        lblAttribution.setId("mv-attribution");
        lblAttribution.setMouseTransparent(true);

        // Add Button Container
        vboxAddButtonContainer = new VBox();
        vboxAddButtonContainer.setId("mv-add-button-container");
        vboxAddButtonContainer.prefWidthProperty().bind(this.getLayout().widthProperty());
        vboxAddButtonContainer.prefHeightProperty().bind(this.getLayout().heightProperty());
        vboxAddButtonContainer.setPickOnBounds(false);

        // Add Button
        btnAddButton = new Button("+");
        btnAddButton.getStyleClass().add("invisible");
        btnAddButton.setId("mv-add-button");
        btnAddButton.setPickOnBounds(false);
        btnAddButton.setOnAction(this::addButtonPressed);

        // Switch to the default tab
        if (launcher.getUserCount() == 0) {
            this.switchTab("users", true);
        } else if (launcher.getProfiles(launcher.getConfig().getCurrentUserUuid()) == null) {
            this.switchTab("profiles", true);
        } else {
            this.switchTab("play", true);
        }

        // Set tab right index
        this.setRightIndex(this.getTabCount() - 1);
    }

    @Override
    protected HBox constructTabHeader(String cssId, String id, String name) {
        HBox hboxHeader = super.constructTabHeader(cssId, id, name);
        if (cssId.equals("ut")) {
            ImageView ivAvatar;
            try {
                ivAvatar = new ImageView(launcher.fetchUserAvatar(launcher.getConfig().getCurrentUserUuid()).toAbsolutePath().toUri().toString());
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    ivAvatar = new ImageView(launcher.fetchUserAvatar(null).toAbsolutePath().toUri().toString());
                } catch (IOException e2) {
                    // Why are we still here? Just to suffer?
                    e2.printStackTrace();
                    ivAvatar = new ImageView();
                }
            }
            ivAvatar.setSmooth(false);
            ivAvatar.setFitWidth(24);
            ivAvatar.setFitHeight(24);
            hboxHeader.getChildren().add(ivAvatar);
        }
        return hboxHeader;
    }

    @Override
    protected void register() {
        super.register();
        Pane layout = this.getLayout();
        vboxAddButtonContainer.getChildren().add(btnAddButton);
        layout.getChildren().add(vboxAddButtonContainer);
        vboxAttributionContainer.getChildren().add(lblAttribution);
        layout.getChildren().add(vboxAttributionContainer);
    }

    @Override
    protected void switchTab(String tabId, boolean force) {
        super.switchTab(tabId, force);

        // Change add button visibility
        if (tabId.equals("profiles") || tabId.equals("users")) {
            btnAddButton.getStyleClass().remove("invisible");
        } else if (!btnAddButton.getStyleClass().contains("invisible")) {
            btnAddButton.getStyleClass().add("invisible");
        }
    }

    /**
     * Handles the add button being pressed.
     */
    private void addButtonPressed(ActionEvent event) {
        ViewScene scene = App.getInstance().getSceneAsViewScene();
        if (this.getCurrentTabId().equals("users")) {
            LoginDialog dialog = new LoginDialog(App.getInstance().getStage());
            dialog.setOnHidden(hiddenEvent -> {
                // Get the user and if the user is not null, refresh the scene
                User user = (User) dialog.getUserData();
                if (user != null) {
                    LoadingView lv = new LoadingView();
                    lv.show(scene, () -> {
                        scene.refresh();
                        System.gc();
                        lv.hide(scene);
                    });
                }
            });
            LoginDialogView view = new LoginDialogView(dialog);
            ((ViewScene) dialog.getScene()).addView(view);
            dialog.show();
        } else if (this.getCurrentTabId().equals("profiles")) {
            ProfileDialog dialog = new ProfileDialog(App.getInstance().getStage());
            dialog.setOnHidden(hiddenEvent -> {
                // Refresh the scene if requested
                if (dialog.getUserData() == Boolean.TRUE) {
                    LoadingView lv = new LoadingView();
                    lv.show(scene, () -> {
                        scene.refresh();
                        System.gc();
                        lv.hide(scene);
                    });
                }
            });
            ProfileDialogView view = new ProfileDialogView(dialog);
            ((ViewScene) dialog.getScene()).addView(view);
            dialog.show();
        }
    }

}
