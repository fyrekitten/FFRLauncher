package net.protolauncher.ui.view;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import net.protolauncher.ui.view.tab.PlayTab;
import net.protolauncher.ui.view.tab.ProfilesTab;

import java.util.HashMap;
import java.util.Map;

public class MainView extends AbstractView<Pane> {

    // Variables
    private Map<String, AbstractView<?>> tabs;
    private String currentTabId;

    // Components
    private BorderPane bpTabContainer;
    private HBox hboxTabHeaderContainer;
    private Map<String, HBox> hboxTabHeaders;
    private Region regTabHeaderSeparator;
    private VBox vboxAttributionContainer;
    private Label lblAttribution;

    // Constructor
    public MainView() {
        super(new Pane(),
            "Colors.css",
            "Components.css",
            "view/MainView.css",
            "view/tab/PlayTab.css",
            "view/tab/ProfilesTab.css"
        );
        this.getLayout().setId("mv-layout");
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
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

        // Tab Headers
        hboxTabHeaders.put("play", this.constructHeader("pt", "play", "Play"));
        hboxTabHeaders.put("profiles", this.constructHeader("prt", "profiles", "Profiles"));

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
    private HBox constructHeader(String cssId, String id, String name) {
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
            regTabHeaderSeparator
        );
        bpTabContainer.setTop(hboxTabHeaderContainer);
        layout.getChildren().add(bpTabContainer);
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

}
