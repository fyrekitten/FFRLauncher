package net.protolauncher.ui.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import net.protolauncher.ui.components.PLButton;

import java.util.LinkedHashMap;

public class AbstractButtonView extends AbstractView<HBox> {

    // Components
    private LinkedHashMap<String, PLButton> btnButtons;

    /**
     * Constructs a new AbstractButtonView with a {@link HBox} as the layout.
     * @param stylesheets The stylesheets for this tab view.
     * @see AbstractView#AbstractView(Pane, String...)
     */
    public AbstractButtonView(String... stylesheets) {
        super(new HBox(), stylesheets);
    }

    /**
     * Constructs a new AbstractButtonView with a {@link HBox} as the layout.
     * @see AbstractView#AbstractView(Pane)
     */
    public AbstractButtonView() {
        super(new HBox());
    }

    // Getters
    public int getButtonCount() {
        return btnButtons.size();
    }
    public PLButton getButton(String id) {
        return btnButtons.get(id);
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Set lists
        this.btnButtons = new LinkedHashMap<>();
    }

    /**
     * Constructs a new button. Should only be called once per button.
     *
     * @param cssId The CSS id for this button.
     * @param id The id for this button.
     * @param text The name of this button to be used for the label.
     * @param actionHandler The action handler for when the button is activated.
     */
    protected void constructButton(String cssId, String id, String text, EventHandler<ActionEvent> actionHandler) {
        PLButton btn = new PLButton(text);
        btn.getButton().setId(cssId);
        btn.getButton().setOnAction(actionHandler);
        btnButtons.put(id, btn);
    }

    @Override
    protected void register() {
        HBox layout = this.getLayout();
        for (PLButton btn : btnButtons.values()) {
            layout.getChildren().add(btn);
        }
    }

}
