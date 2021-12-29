package net.protolauncher.ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PLPlayButton extends PLButton {

    // Components
    protected VBox vboxTextContainer;
    protected Label lblTopText;
    protected Label lblBottomText;

    // Constructor
    public PLPlayButton(String topText, String bottomText) {
        super("");

        // Text Container
        this.vboxTextContainer = new VBox();
        this.vboxTextContainer.getStyleClass().add("plpb-text-container");

        // Top Label
        this.lblTopText = new Label(topText);
        this.lblTopText.getStyleClass().add("plpb-top-label");

        // Bottom Label
        this.lblBottomText = new Label(bottomText);
        this.lblBottomText.getStyleClass().add("plpb-bottom-label");

        // Set graphic, change classes, and register
        this.vboxTextContainer.getChildren().addAll(this.lblTopText, this.lblBottomText);
        this.button.setGraphic(this.vboxTextContainer);
        this.getStyleClass().add("plpb-container");
        this.button.getStyleClass().add("plpb-button");

        // Add disabled listener
        this.button.disabledProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.lblTopText.getStyleClass().add("disabled");
                this.lblBottomText.getStyleClass().add("disabled");
            } else {
                this.lblTopText.getStyleClass().remove("disabled");
                this.lblBottomText.getStyleClass().remove("disabled");
            }
        });
    }

    // Getters
    public VBox getTextContainer() {
        return vboxTextContainer;
    }
    public Label getTopLabel() {
        return lblTopText;
    }
    public Label getBottomLabel() {
        return lblBottomText;
    }

}
