package net.protolauncher.ui.components;

import javafx.scene.CacheHint;
import javafx.scene.control.Button;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class PLButton extends BorderPane {

    // Components
    protected Button button;

    // Constructor
    public PLButton(String text) {
        this.getStyleClass().add("plb-container");
        this.setCache(true);
        this.setCacheShape(true);
        this.setCacheHint(CacheHint.SPEED);

        // Button
        this.button = new Button(text);
        this.button.getStyleClass().add("plb-button");
        this.button.setCache(true);
        this.button.setCacheShape(true);
        this.button.setCacheHint(CacheHint.SPEED);

        // Set container and apply effects
        this.setCenter(this.button);
        this.applyEffects();
    }

    // Getters
    public Button getButton() {
        return button;
    }

    /**
     * I know this is incredibly nitpicky about how this works,
     * but I really need it to look exactly how I want it.
     * Caching should help with performance once this has applied.
     */
    private void applyEffects() {
        InnerShadow top     = new InnerShadow(BlurType.ONE_PASS_BOX, Color.rgb(255, 255, 255, (100 / 255.0)), 2, 0.040, 0, 1);
        InnerShadow left    = new InnerShadow(BlurType.ONE_PASS_BOX, Color.rgb(255, 255, 255, (40 / 255.0)), 2, 0.0625, 1, 0);
        InnerShadow bottom  = new InnerShadow(BlurType.ONE_PASS_BOX, Color.rgb(0, 0, 0, (100 / 255.0)), 2, 0.040, 0, -1);
        InnerShadow right   = new InnerShadow(BlurType.ONE_PASS_BOX, Color.rgb(0, 0, 0, (110 / 255.0)), 2, 0.0272, -1, 0);
        top.setInput(left);
        left.setInput(bottom);
        bottom.setInput(right);
        button.setEffect(top);
    }

}
