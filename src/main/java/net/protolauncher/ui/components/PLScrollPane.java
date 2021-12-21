package net.protolauncher.ui.components;

import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;

public class PLScrollPane extends ScrollPane {

    // Variables
    private final double scrollMultiplier;

    /**
     * Constructs a new scroll pane.
     * @param scrollMultiplier The multiplier for the scroll speed.
     */
    public PLScrollPane(double scrollMultiplier) {
        this.scrollMultiplier = scrollMultiplier;
    }

    /**
     * Applies the scroll multiplier.
     * Should be done after all components are registered and added.
     */
    public void applyScrollMultiplier() {
        this.setOnScroll(this::updateVerticalScroll);
        this.getContent().setOnScroll(this::updateVerticalScroll);
    }

    /**
     * The event for when this scroll pane gets updated.
     * @param event The event
     */
    private void updateVerticalScroll(ScrollEvent event) {
        double deltaY = event.getDeltaY() * scrollMultiplier;
        double width = this.getContent().getBoundsInLocal().getWidth();
        double vvalue = this.getVvalue();
        this.setVvalue(vvalue + -deltaY / width);
    }

}
