package net.protolauncher.ui;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import net.protolauncher.ui.view.AbstractView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static net.protolauncher.App.LOGGER;

public final class ViewScene extends Scene {

    // Variables
    private final List<AbstractView<?>> views = new ArrayList<>();

    // Constructor
    public ViewScene() {
        super(new Pane());

        // Add focus removal
        this.getRoot().setOnMouseClicked(event -> this.removeFocus());
        this.removeFocus();

        // Add refresh keybind
        this.getAccelerators().put(Keybinds.REFRESH, this::refresh);
    }

    // Getters
    public List<AbstractView<?>> getViews() {
        return views;
    }

    /**
     * Adds a new abstract view onto the topmost layer for this scene.
     *
     * @param view The view to add onto this scene.
     */
    public void addView(AbstractView<?> view) {
        this.addView(view, false);
    }
    private void addView(AbstractView<?> view, boolean dontAdd) {
        Pane root = (Pane) this.getRoot();

        // Load stylesheets
        this.loadStylesheets(view);

        // Add view
        if (!dontAdd) {
            views.add(view);
        }
        root.getChildren().add(0, view.getLayout());

        // Bind width and height
        view.getLayout().prefWidthProperty().bind(root.widthProperty());
        view.getLayout().prefHeightProperty().bind(root.heightProperty());
    }

    /**
     * Removes the given view from this scene.
     *
     * @param view The view to remove from this scene.
     */
    public void removeView(AbstractView<?> view) {
        Pane root = (Pane) this.getRoot();

        // Remove view
        views.remove(view);
        root.getChildren().remove(view.getLayout());

        // Unbind width and height
        view.getLayout().prefWidthProperty().unbind();
        view.getLayout().prefHeightProperty().unbind();
    }

    /**
     * Loads the stylesheets for the given view.
     * <br/><br/>
     * Note: JavaFX has an internal cache for stylesheets, so we don't need our own cache.
     *
     * @param view The view to load stylesheets for.
     */
    public void loadStylesheets(AbstractView<?> view) {
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
     * Refreshes all views in this view and reloads the stylesheets.
     */
    public void refresh() {
        LOGGER.debug("Refreshing...");
        this.getStylesheets().clear();
        ((Pane) this.getRoot()).getChildren().clear();
        for (AbstractView<?> view : views) {
            view.refresh();
            this.addView(view, true);
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
