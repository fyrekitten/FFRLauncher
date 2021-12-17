package net.protolauncher.ui;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import net.protolauncher.ui.view.AbstractView;

import java.net.URL;

import static net.protolauncher.App.LOGGER;

public final class MainScene extends Scene {

    // Variables
    private AbstractView<?> view;

    // Constructor
    public MainScene() {
        super(new Pane());

        // Add focus removal
        this.getRoot().setOnMouseClicked(event -> this.removeFocus());
        this.removeFocus();

        // Add refresh keybind
        this.getAccelerators().put(Keybinds.REFRESH, () -> {
            LOGGER.debug("Refreshing...");
            this.getStylesheets().clear();
            view.refresh();
            this.setView(view);
        });
    }

    // Getters
    public AbstractView<?> getView() {
        return view;
    }

    /**
     * Removes the previous view from this scene and adds a new one.
     * <br/><br/>
     * TODO: Transitions
     *
     * @param view The new view to set this scene to.
     */
    public void setView(AbstractView<?> view) {
        Pane root = (Pane) this.getRoot();

        // Set the view variable
        this.view = view;

        // Remove previous view
        root.getChildren().clear();

        // Load stylesheets
        this.loadStylesheets(view);

        // Add new view
        root.getChildren().add(view.getLayout());

        // Bind width and height
        view.getLayout().prefWidthProperty().bind(root.widthProperty());
        view.getLayout().prefHeightProperty().bind(root.heightProperty());
    }

    /**
     * Loads the stylesheets for the given view.
     * <br/><br/>
     * Note: JavaFX has an internal cache for stylesheets, so we don't need our own cache.
     *
     * @param view The view to load stylesheets for.
     */
    private void loadStylesheets(AbstractView<?> view) {
        for (String stylesheet : view.getStylesheets()) {
            URL url = this.getClass().getResource("/styles/" + stylesheet);
            if (url == null) {
                LOGGER.warn("Unable to load stylesheet " + stylesheet + ": Stylesheet not found.");
                continue;
            }
            this.getStylesheets().add(url.toExternalForm());
        }
    }

    /**
     * Forcefully removes focus from whatever is currently focused.
     */
    public void removeFocus() {
        this.getRoot().setFocusTraversable(true);
        this.getRoot().requestFocus();
        this.getRoot().setFocusTraversable(false);
    }

}
