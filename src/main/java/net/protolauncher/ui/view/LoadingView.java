package net.protolauncher.ui.view;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.scene.CacheHint;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import net.protolauncher.ui.ViewScene;
import net.protolauncher.ui.anim.AnimFadeBackground;
import org.jetbrains.annotations.Nullable;

public class LoadingView extends AbstractView<Pane> {

    // Constants
    private static final Color BG_COLOR = new Color(39.0 / 255.0, 39.0 / 255.0, 39.0 / 255.0, 0.0);
    private static final long MIN_DURATION = 500;
    private static final long FADE_DURATION = 200;
    private static final long SPIN_DURATION = 1200;
    private static final long WAIT_DURATION = SPIN_DURATION / 3;

    // Components
    private VBox vboxImageContainer;
    private Region regImage;

    // Transitions
    private boolean stopRotation = false;
    private RotateTransition rtImageRotation;

    // Constructor
    public LoadingView() {
        super(new Pane(), "Colors.css", "Components.css", "view/LoadingView.css");
        this.getLayout().setId("lv-layout");
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Image Container
        vboxImageContainer = new VBox();
        vboxImageContainer.setId("lv-image-container");
        vboxImageContainer.prefWidthProperty().bind(this.getLayout().widthProperty());
        vboxImageContainer.prefHeightProperty().bind(this.getLayout().heightProperty());
        vboxImageContainer.setMouseTransparent(true);

        // Image
        SVGPath svgImage = new SVGPath();
        svgImage.setContent("M150,125a25,25,0,1,1-25-25A25,25,0,0,1,150,125Zm94-18v37a4.0118,4.0118,0,0,1-4,4H209.4388a86.9789,86.9789,0,0,1-8.9926,21.3335l21.4275,21.4274a4.0119,4.0119,0,0,1,0,5.6569l-26.163,26.1629a4.0117,4.0117,0,0,1-5.6569,0l-21.6113-21.6113A86.9521,86.9521,0,0,1,147,209.7094V240a4.0118,4.0118,0,0,1-4,4H106a4.0118,4.0118,0,0,1-4-4V209.4388a86.98,86.98,0,0,1-21.3336-8.9926L59.2391,221.8736a4.0119,4.0119,0,0,1-5.6569,0L27.4193,195.7107a4.0118,4.0118,0,0,1,0-5.6569l21.6114-21.6113A86.9521,86.9521,0,0,1,40.2906,147H10a4.0118,4.0118,0,0,1-4-4V106a4.0118,4.0118,0,0,1,4-4H40.5612a86.9751,86.9751,0,0,1,8.9926-21.3335L28.1263,59.2391a4.0119,4.0119,0,0,1,0-5.6569l26.163-26.1629a4.0117,4.0117,0,0,1,5.6569,0L81.5575,49.0306A86.9562,86.9562,0,0,1,103,40.2905V10a4.0118,4.0118,0,0,1,4-4h37a4.0118,4.0118,0,0,1,4,4V40.5612a86.9767,86.9767,0,0,1,21.3335,8.9926l21.4274-21.4275a4.0119,4.0119,0,0,1,5.6569,0l26.1629,26.163a4.0117,4.0117,0,0,1,0,5.6569L200.9694,81.5575A86.9521,86.9521,0,0,1,209.7094,103H240A4.0118,4.0118,0,0,1,244,107Zm-79.0528,20.0814a40.0014,40.0014,0,1,0-37.8654,37.8658A40.0042,40.0042,0,0,0,164.9472,127.0814ZM236.5,109.75l-32.9514-.0015a79.5265,79.5265,0,0,0-11.66-28.6576l20.6157-20.6145a4.0119,4.0119,0,0,0,0-5.657L195.8876,38.2028a4.0121,4.0121,0,0,0-5.657,0L169.7507,58.68A79.5645,79.5645,0,0,0,141.25,46.6518V13.5L125,13.4991V75a50,50,0,0,1,50,50h61.5Z");
        regImage = new Region();
        regImage.setShape(svgImage);
        regImage.setId("lv-image");
        regImage.setCache(true);
        regImage.setCacheShape(true);
        regImage.setCacheHint(CacheHint.SPEED);
        regImage.setMouseTransparent(true);
        regImage.setVisible(false);

        // Rotate Transition
        rtImageRotation = new RotateTransition(Duration.millis(SPIN_DURATION), regImage);
        rtImageRotation.setInterpolator(Interpolator.SPLINE(0.49, 0.0, 0.51, 1.0));
        rtImageRotation.setFromAngle(0);
        rtImageRotation.setToAngle(360);
        rtImageRotation.setOnFinished(event -> {
            (new Thread(() -> {
                try {
                    Thread.sleep(WAIT_DURATION);
                } catch (InterruptedException e) {
                    // ...
                }
                if (stopRotation) {
                    return;
                }
                Platform.runLater(rtImageRotation::play);
            })).start();
        });
    }

    @Override
    protected void register() {
        Pane layout = this.getLayout();
        vboxImageContainer.getChildren().add(regImage);
        layout.getChildren().add(vboxImageContainer);
    }

    @Override
    public void refresh() {
        // This view is not allowed to refresh.
    }

    /**
     * Makes this loading view visible.
     * @param scene The scene this should be added to and eventually shown in.
     * @param finished Called when this loading view has completed its fade in.
     */
    public void show(ViewScene scene, Runnable finished) {
        vboxImageContainer.setMouseTransparent(false);
        regImage.setMouseTransparent(false);
        regImage.setVisible(true);
        rtImageRotation.play();
        scene.addView(this);

        // Fade in
        Animation animation = new AnimFadeBackground(Duration.millis(FADE_DURATION), Interpolator.EASE_BOTH, this.getLayout(), BG_COLOR, 0.0, 1.0);
        animation.setOnFinished(event -> (new Thread(() -> {
            // Wait minimum time
            try {
                Thread.sleep(MIN_DURATION - FADE_DURATION);
            } catch (InterruptedException e) {
                // ...
            }

            // Switch back to JavaFX thread and run finished
            Platform.runLater(finished);
        })).start());
        animation.play();
    }

    /**
     * Hides this loading view.
     * @param scene The scene this should be hidden and eventually removed from.
     * @param finished Called when this loading view has completed its fade out.
     */
    public void hide(ViewScene scene, @Nullable Runnable finished) {
        vboxImageContainer.setMouseTransparent(true);
        regImage.setMouseTransparent(true);
        stopRotation = true;
        regImage.setVisible(false);

        // Fade out
        Animation animation = new AnimFadeBackground(Duration.millis(200), Interpolator.EASE_BOTH, this.getLayout(), BG_COLOR, 1.0, 0.5);
        animation.setOnFinished(event -> {
            scene.removeView(this);
            if (finished != null) {
                finished.run();
            }
        });
        animation.play();
    }

    /**
     * Hides this loading view.
     * @param scene The scene this should be hidden and eventually removed from.
     */
    public void hide(ViewScene scene) {
        this.hide(scene, null);
    }

}
