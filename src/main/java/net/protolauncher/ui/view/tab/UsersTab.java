package net.protolauncher.ui.view.tab;

import javafx.event.ActionEvent;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import net.protolauncher.App;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.api.User;
import net.protolauncher.ui.ViewScene;
import net.protolauncher.ui.components.PLButton;
import net.protolauncher.ui.components.PLScrollPane;
import net.protolauncher.ui.view.AbstractView;

import java.util.ArrayList;
import java.util.List;

import static net.protolauncher.App.LOGGER;

public class UsersTab extends AbstractView<Pane> {

    // Components
    private PLScrollPane spScrollContainer;
    private GridPane gpGridContainer;
    private List<HBox> users;

    // Constructor
    public UsersTab() {
        super(new Pane());
        this.getLayout().setId("ut-layout");
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Set users
        this.users = new ArrayList<>();

        // Scroll Container
        spScrollContainer = new PLScrollPane(2);
        spScrollContainer.setId("ut-scroll-container");
        spScrollContainer.prefWidthProperty().bind(this.getLayout().widthProperty());
        spScrollContainer.prefHeightProperty().bind(this.getLayout().heightProperty());

        // Grid Container
        gpGridContainer = new GridPane();
        gpGridContainer.setId("ut-grid-container");
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(50);
        gpGridContainer.getColumnConstraints().addAll(constraints, constraints);

        // Construct Users
        for (User user : App.getInstance().getLauncher().getAllUsers()) {
            users.add(this.constructUser(user));
        }
    }

    /**
     * Constructs a new user component for the list.
     *
     * @param user The user we're constructing this component for.
     * @return A new user component.
     */
    private HBox constructUser(User user) {
        // Get the launcher
        ProtoLauncher launcher = App.getInstance().getLauncher();

        // Primary Container
        HBox hboxContainer = new HBox();
        hboxContainer.getStyleClass().add("ut-user-container");
        HBox.setHgrow(hboxContainer, Priority.ALWAYS);

        // Left Container
        VBox vboxLeftContainer = new VBox();
        vboxLeftContainer.getStyleClass().add("ut-user-left-container");

        // Avatar
        Circle circAvatar = new Circle();
        circAvatar.getStyleClass().add("ut-user-avatar");
        circAvatar.setRadius(32);
        try {
            circAvatar.setFill(new ImagePattern(new Image(launcher.fetchUserAvatar(user.getUuid()).toAbsolutePath().toUri().toString())));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                circAvatar.setFill(new ImagePattern(new Image(launcher.fetchUserAvatar(null).toAbsolutePath().toUri().toString())));
            } catch (Exception e2) {
                // Why are we still here? Just to suffer?
                e2.printStackTrace();
                circAvatar.setFill(Color.valueOf("#c81688"));
            }
        }
        circAvatar.setCache(true);
        circAvatar.setCacheHint(CacheHint.SPEED);

        // Right Container
        VBox vboxRightContainer = new VBox();
        vboxRightContainer.getStyleClass().add("ut-user-right-container");

        // Username
        Label lblUsername = new Label(user.getUsername());
        lblUsername.getStyleClass().add("ut-user-username");

        // Button Container
        HBox hboxButtons = new HBox();
        hboxButtons.getStyleClass().add("ut-user-button-container");

        // Select Button
        PLButton btnSelect = new PLButton("Select");
        btnSelect.getStyleClass().add("ut-user-select-button");
        btnSelect.getButton().setOnAction(event -> this.userSelectButtonPressed(event, user));

        // Check against current user
        if (user.getUuid().equals(launcher.getConfig().getCurrentUserUuid())) {
            btnSelect.getButton().setText("Selected");
            btnSelect.getButton().setDisable(true);
        }

        // Logout Button
        PLButton btnLogout = new PLButton("Logout");
        btnLogout.getStyleClass().add("ut-user-logout-button");
        btnLogout.getButton().getStyleClass().add("red");
        btnLogout.getButton().setOnAction(event -> this.userLogoutButtonPressed(event, user));

        // Registration
        vboxLeftContainer.getChildren().add(circAvatar);
        hboxButtons.getChildren().addAll(btnSelect, btnLogout);
        vboxRightContainer.getChildren().addAll(lblUsername, hboxButtons);
        hboxContainer.getChildren().addAll(vboxLeftContainer, vboxRightContainer);

        // Return
        return hboxContainer;
    }

    @Override
    protected void register() {
        Pane layout = this.getLayout();

        // Add users
        int row = 0;
        for (int i = 0; i < users.size(); i++) {
            if (i % 2 != 0) {
                gpGridContainer.add(users.get(i), 1, row++);
            } else {
                gpGridContainer.add(users.get(i), 0, row);
            }
        }

        // The rest
        spScrollContainer.setContent(gpGridContainer);
        spScrollContainer.applyScrollMultiplier();
        layout.getChildren().add(spScrollContainer);
    }

    /**
     * Handles a user's select button being pressed.
     */
    private void userSelectButtonPressed(ActionEvent event, User user) {
        Scene scene = App.getInstance().getStage().getScene();
        ViewScene viewScene;
        if (scene instanceof ViewScene) {
            viewScene = (ViewScene) scene;
            viewScene.removeFocus();
        } else {
            LOGGER.error("Scene was not a ViewScene");
            return;
        }

        // TODO: Show loading screen
        try {
            App.getInstance().getLauncher().switchUser(user);
        } catch (Exception e) {
            // TODO: Show error popup window
            e.printStackTrace();
        }
        viewScene.refresh();
        System.gc();
    }

    /**
     * Handles a user's logout button being pressed.
     */
    private void userLogoutButtonPressed(ActionEvent event, User user) {
        Scene scene = App.getInstance().getStage().getScene();
        ViewScene viewScene;
        if (scene instanceof ViewScene) {
            viewScene = (ViewScene) scene;
            viewScene.removeFocus();
        } else {
            LOGGER.error("Scene was not a ViewScene");
            return;
        }

        // TODO: Show loading screen
        try {
            App.getInstance().getLauncher().removeUser(user);
        } catch (Exception e) {
            // TODO: Show error popup window
            e.printStackTrace();
        }
        viewScene.refresh();
        System.gc();
    }

}
