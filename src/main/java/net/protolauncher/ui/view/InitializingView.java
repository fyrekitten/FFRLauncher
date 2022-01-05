package net.protolauncher.ui.view;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import net.protolauncher.App;
import net.protolauncher.log4j.FeedbackLoggerWrapper;
import net.protolauncher.log4j.ILogListener;

public class InitializingView extends AbstractView<StackPane> implements ILogListener {

    // Components
    private ImageView ivLogo;
    private VBox vboxVerticalContainer;
    private ProgressBar pgbProgressBar;
    private VBox vboxLogsContainer;

    // Constructor
    public InitializingView() {
        super(new StackPane(), "Colors.css", "Components.css", "view/InitializingView.css");
        this.getLayout().setId("iv-layout");
        this.construct();
        this.register();
        FeedbackLoggerWrapper.registerListener(this);
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Logo
        Image image = new Image(App.getInstance().getRequiredResourceAsStream("logo.png"), 1200, 1200, true, true);
        ivLogo = new ImageView(image);
        ivLogo.setPreserveRatio(true);
        ivLogo.setFitWidth(Math.min(1200, this.getLayout().prefWidthProperty().doubleValue() / 1.5));
        this.getLayout().prefWidthProperty().addListener((observable, oldValue, newValue) -> {
            ivLogo.setFitWidth(Math.min(1200, newValue.doubleValue() / 1.5));
        });
        ivLogo.setId("iv-logo");

        // Vertical Container
        vboxVerticalContainer = new VBox();
        vboxVerticalContainer.setId("iv-vertical-container");

        // Progress Bar
        pgbProgressBar = new ProgressBar(0);
        pgbProgressBar.setId("iv-pgb");

        // Logs
        vboxLogsContainer = new VBox();
        vboxLogsContainer.setId("iv-logs-container");
    }

    @Override
    protected void register() {
        StackPane layout = this.getLayout();
        vboxVerticalContainer.getChildren().add(vboxLogsContainer);
        vboxVerticalContainer.getChildren().add(pgbProgressBar);
        layout.getChildren().add(vboxVerticalContainer);
        layout.getChildren().add(ivLogo);
    }

    // ILogListener Implementation
    @Override
    public void onLog(String message) {
        Platform.runLater(() -> this.addLog(message));
    }

    /**
     * Updates the progress on the progress bar for this view.
     * @param progress The amount of progress.
     */
    public void setProgress(double progress) {
        this.pgbProgressBar.setProgress(progress);
    }

    /**
     * Adds a log to the logs container for this scene.
     * @param message The message to add.
     */
    public void addLog(String message) {
        // Get the children and create and add a label
        ObservableList<Node> children = vboxLogsContainer.getChildren();
        Label label = new Label(message);
        children.add(label);

        // Remove any logs above the limit
        if (children.size() > 10) {
            children.remove(0);
        }
    }

}
