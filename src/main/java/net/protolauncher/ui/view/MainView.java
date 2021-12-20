package net.protolauncher.ui.view;

import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import net.protolauncher.App;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.api.User;
import net.protolauncher.ui.ViewScene;
import net.protolauncher.ui.dialog.LoginDialog;
import net.protolauncher.ui.view.tab.NoUsersTab;
import net.protolauncher.ui.view.tab.PlayTab;
import net.protolauncher.ui.view.tab.ProfilesTab;
import net.protolauncher.ui.view.tab.UsersTab;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static net.protolauncher.App.LOGGER;

public class MainView extends AbstractView<Pane> {

    // Variables
    private Map<String, AbstractView<?>> tabs;
    private String currentTabId;

    // Components
    private BorderPane bpTabContainer;
    private HBox hboxTabHeaderContainer;
    private Map<String, HBox> hboxTabHeaders;
    private Region regTabHeaderSeparator;
    private VBox vboxAddButtonContainer;
    private Button btnAddButton;
    private VBox vboxAttributionContainer;
    private Label lblAttribution;

    // Constructor
    public MainView() {
        super(new Pane(),
            "Colors.css",
            "Components.css",
            "view/MainView.css",
            "view/tab/PlayTab.css",
            "view/tab/ProfilesTab.css",
            "view/tab/UsersTab.css"
        );
        this.getLayout().setId("mv-layout");
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Get current user
        ProtoLauncher launcher = App.getInstance().getLauncher();
        User currentUser = launcher.getCurrentUser();

        // Set lists
        this.tabs = new HashMap<>();
        this.hboxTabHeaders = new HashMap<>();

        // Tab Container
        bpTabContainer = new BorderPane();
        bpTabContainer.setId("mv-tab-container");
        bpTabContainer.prefWidthProperty().bind(this.getLayout().widthProperty());
        bpTabContainer.prefHeightProperty().bind(this.getLayout().heightProperty());

        // Tab Header Container
        hboxTabHeaderContainer = new HBox();
        hboxTabHeaderContainer.setId("mv-tab-header-container");

        // Tabs
        tabs.put("play", new PlayTab());
        tabs.put("profiles", new ProfilesTab());
        if (currentUser == null) {
            tabs.put("users", new NoUsersTab());
        } else {
            tabs.put("users", new UsersTab());
        }

        // Tab Headers
        hboxTabHeaders.put("play", this.constructTabHeader("pt", "play", "Play"));
        hboxTabHeaders.put("profiles", this.constructTabHeader("prt", "profiles", "Profiles"));
        if (currentUser == null) {
            hboxTabHeaders.put("users", this.constructTabHeader("nut", "users", "No Users"));
        } else {
            // If the user exists, then we have a different header for that one
            HBox hboxHeader = this.constructTabHeader("ut", "users", currentUser.getUsername());
            ImageView ivAvatar;
            try {
                ivAvatar = new ImageView(launcher.fetchUserAvatar(currentUser.getUuid()).toAbsolutePath().toUri().toString());
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
            hboxTabHeaders.put("users", hboxHeader);
        }

        // Tab Separator
        regTabHeaderSeparator = new Region();
        regTabHeaderSeparator.setId("mv-tab-header-separator");
        HBox.setHgrow(regTabHeaderSeparator, Priority.ALWAYS);

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
        this.currentTabId = "play";
        this.switchTab("play", true);
    }

    /**
     * Constructs a new header for a tab.
     *
     * @param cssId The CSS id for this tab.
     * @param id The id for this tab.
     * @param name The name of this tab to be used in the header label.
     */
    private HBox constructTabHeader(String cssId, String id, String name) {
        // Container
        HBox header = new HBox();
        header.setId(cssId + "-header");

        // Label
        Label label = new Label(name);
        label.setMouseTransparent(true);
        header.getChildren().add(label);

        // Set events
        header.setOnMouseClicked(event -> this.switchTab(id));
        header.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE && ((Parent) event.getSource()).isFocused()) {
                this.switchTab(id);
            }
        });

        // Return header
        return header;
    }

    @Override
    protected void register() {
        Pane layout = this.getLayout();
        hboxTabHeaderContainer.getChildren().addAll(
            hboxTabHeaders.get("play"),
            hboxTabHeaders.get("profiles"),
            regTabHeaderSeparator,
            hboxTabHeaders.get("users")
        );
        bpTabContainer.setTop(hboxTabHeaderContainer);
        layout.getChildren().add(bpTabContainer);
        vboxAddButtonContainer.getChildren().add(btnAddButton);
        layout.getChildren().add(vboxAddButtonContainer);
        vboxAttributionContainer.getChildren().add(lblAttribution);
        layout.getChildren().add(vboxAttributionContainer);
    }

    @Override
    public void refresh() {
        String previousTabId = currentTabId;
        this.tabs.clear();
        super.refresh();
        this.switchTab(previousTabId);
    }

    /**
     * Switches from the current tab to the requested tab.
     *
     * @param tabId The id of the tab to change to.
     * @param force Force a switch even if the tab id is the same as the current tab.
     */
    public void switchTab(String tabId, boolean force) {
        if (force || !currentTabId.equals(tabId)) {
            // Remove styles on current tab header
            HBox oldHeader = hboxTabHeaders.getOrDefault(currentTabId, null);
            if (oldHeader != null) {
                oldHeader.getStyleClass().remove("current");
            }

            // Add styles to new tab header
            HBox newHeader = hboxTabHeaders.get(tabId);
            newHeader.getStyleClass().add("current");
            newHeader.requestFocus();

            // Change add button visibility
            if (tabId.equals("profiles") || tabId.equals("users")) {
                btnAddButton.getStyleClass().remove("invisible");
            } else if (!btnAddButton.getStyleClass().contains("invisible")) {
                btnAddButton.getStyleClass().add("invisible");
            }

            // Set the current tab
            AbstractView<?> tab = tabs.get(tabId);
            currentTabId = tabId;
            bpTabContainer.setCenter(tab.getLayout());
        }
    }

    /**
     * Swicthes from the current tab to the requested tab.
     *
     * @param tabId The id of the tab to change to.
     */
    public void switchTab(String tabId) {
        this.switchTab(tabId, false);
    }

    /**
     * Handles the add button being pressed.
     */
    private void addButtonPressed(ActionEvent event) {
        Scene scene = App.getInstance().getStage().getScene();
        ViewScene viewScene;
        if (scene instanceof ViewScene) {
            viewScene = (ViewScene) scene;
            viewScene.removeFocus();
        } else {
            LOGGER.error("Scene was not a ViewScene");
            return;
        }

        if (currentTabId.equals("users")) {
            LoginDialog dialog = new LoginDialog(App.getInstance().getStage());
            dialog.setOnHidden(hiddenEvent -> {
                // Get the user and if the user is not null, refresh the scene
                User user = (User) dialog.getUserData();
                if (user != null) {
                    // TODO: Show loading screen
                    viewScene.refresh();
                    System.gc();
                }
            });
            dialog.show();
        } else if (currentTabId.equals("profiles")) {
            System.out.println("Add profiles prompt!");
        }
    }

}
