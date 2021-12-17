package net.protolauncher.ui.view;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;

/**
 * Represents a list of components that are used to construct
 * a {@link Scene}, abstracted as a 'view'. Similar to the concept of
 * scenes in JavaFX, with the primary differences being that
 * a view represents an abstract list of components, allowing
 * them to be nested.
 *
 * @param <L> The primary pane to act as this view's layout.
 */

public abstract class AbstractView<L extends Pane> {

    // Variables
    private final L layout;
    private final String[] stylesheets;

    /**
     * Constructs a new view using the given {@link Pane} as the layout.
     *
     * @param layout The layout for this view.
     * @param stylesheets A list of filenames relative to "/styles" in the resources folder required for this view to work.
     */
    public AbstractView(L layout, String... stylesheets) {
        this.layout = layout;
        this.layout.getStyleClass().add("layout");
        this.stylesheets = stylesheets;
        this.construct();
        this.register();
    }

    // Getters
    public L getLayout() {
        return layout;
    }
    public String[] getStylesheets() {
        return stylesheets;
    }

    /**
     * Constructs all the UI components for this view.
     */
    protected abstract void construct();

    /**
     * Registers all the UI components for this view.
     */
    protected abstract void register();

    /**
     * Re-constructs and re-registers the UI for this view.
     */
    public void refresh() {
        this.getLayout().getChildren().clear();
        this.construct();
        this.register();
    }

}
