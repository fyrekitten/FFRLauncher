package net.protolauncher.ui.anim;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import net.protolauncher.util.AnimUtil;

public class AnimFadeBackground extends Transition {

    // Variables
    private final Region component;
    private final Color color;
    private final double startingOpacity;
    private final double endingOpacity;

    // Constructor
    public AnimFadeBackground(Duration duration, Interpolator interpolator, Region component, Color color, double startingOpacity, double endingOpacity) {
        this.setCycleDuration(duration);
        this.setInterpolator(interpolator);
        this.component = component;
        this.color = color;
        this.startingOpacity = startingOpacity;
        this.endingOpacity = endingOpacity;
    }

    // Transition Implementation
    @Override
    protected void interpolate(double frac) {
        // Get new opacity
        double newOpacity = AnimUtil.lerp(startingOpacity, endingOpacity, frac);

        // Set component background
        component.setBackground(new Background(new BackgroundFill(new Color(color.getRed(), color.getBlue(), color.getGreen(), newOpacity), CornerRadii.EMPTY, Insets.EMPTY)));
    }

}
