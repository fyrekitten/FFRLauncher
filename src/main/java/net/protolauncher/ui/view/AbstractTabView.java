package net.protolauncher.ui.view;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import net.protolauncher.App;
import net.protolauncher.ui.ViewScene;

import java.util.LinkedHashMap;
import java.util.List;

public abstract class AbstractTabView extends AbstractView<Pane> {

    // Variables
    private LinkedHashMap<String, AbstractView<?>> tabs;
    private String currentTabId;
    private int rightIndex;

    // Components
    private BorderPane bpTabContainer;
    private HBox hboxTabHeaderContainer;
    private LinkedHashMap<String, HBox> hboxTabHeaders;
    private Region regTabHeaderSeparator;

    /**
     * Constructs a new AbstractTabView with a {@link Pane} as the layout.
     * @param stylesheets The stylesheets for this tab view.
     * @see AbstractView#AbstractView(Pane, String...)
     */
    public AbstractTabView(String... stylesheets) {
        super(new Pane(), stylesheets);
    }

    /**
     * Constructs a new AbstractTabView with a {@link Pane} as the layout.
     * @see AbstractView#AbstractView(Pane)
     */
    public AbstractTabView() {
        super(new Pane());
    }

    // Getters
    public String getCurrentTabId() {
        return currentTabId;
    }
    public int getRightIndex() {
        return rightIndex;
    }
    public int getTabCount() {
        return tabs.size();
    }
    public AbstractView<?> getTab(String id) {
        return tabs.get(id);
    }

    // Setters
    /**
     * Sets the index for where the tabs should be separated between right and left.
     * @param rightIndex The index.
     */
    protected void setRightIndex(int rightIndex) {
        this.rightIndex = rightIndex;
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Set right index
        this.rightIndex = -1;

        // Set lists
        this.tabs = new LinkedHashMap<>();
        this.hboxTabHeaders = new LinkedHashMap<>();

        // Tab Container
        bpTabContainer = new BorderPane();
        bpTabContainer.getStyleClass().add("tab-container");
        bpTabContainer.prefWidthProperty().bind(this.getLayout().widthProperty());
        bpTabContainer.prefHeightProperty().bind(this.getLayout().heightProperty());

        // Tab Header Container
        hboxTabHeaderContainer = new HBox();
        hboxTabHeaderContainer.getStyleClass().add("tab-header-container");

        // Tab Separator
        regTabHeaderSeparator = new Region();
        regTabHeaderSeparator.getStyleClass().add("tab-separator");
        HBox.setHgrow(regTabHeaderSeparator, Priority.ALWAYS);
    }

    /**
     * Constructs a new tab header.
     *
     * @param cssId The CSS id for this tab.
     * @param id The id for this tab.
     * @param name The name of this tab to be used in the header label.
     * @return The {@link HBox} container for this tab.
     */
    protected HBox constructTabHeader(String cssId, String id, String name) {
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

    /**
     * Constructs a new tab. Should only be called once per tab during the {@link AbstractView#construct()} phase.
     *
     * @param cssId The CSS id for this tab.
     * @param id The id for this tab.
     * @param name The name of this tab to be used in the header label.
     * @param tab The {@link AbstractView} for this tab.
     */
    protected void constructTab(String cssId, String id, String name, AbstractView<?> tab) {
        tabs.put(id, tab);
        hboxTabHeaders.put(id, this.constructTabHeader(cssId, id, name));
    }

    @Override
    protected void register() {
        Pane layout = this.getLayout();
        List<String> keys = tabs.keySet().stream().toList();
        for (int i = 0; i < tabs.size(); i++) {
            if (i == rightIndex) {
                hboxTabHeaderContainer.getChildren().add(regTabHeaderSeparator);
            }
            hboxTabHeaderContainer.getChildren().add(hboxTabHeaders.get(keys.get(i)));
        }
        bpTabContainer.setTop(hboxTabHeaderContainer);
        layout.getChildren().add(bpTabContainer);
    }

    @Override
    public void refresh() {
        String previousTabId = currentTabId;
        this.tabs.clear();
        super.refresh();
        this.switchTab(previousTabId);
        ViewScene scene = App.getInstance().getSceneAsViewScene();
        if (scene != null) {
            scene.removeFocus();
        }
    }

    /**
     * Switches from the current tab to the requested tab.
     *
     * @param tabId The id of the tab to change to.
     * @param force Forces a switch even if the tab id is the same as the current tab.
     */
    protected void switchTab(String tabId, boolean force) {
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
     * Switches from the current tab to the requested tab.
     *
     * @param tabId The id of the tab to change to.
     */
    public void switchTab(String tabId) {
        this.switchTab(tabId, false);
    }

}
