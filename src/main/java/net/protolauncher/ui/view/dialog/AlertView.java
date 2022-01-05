package net.protolauncher.ui.view.dialog;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.protolauncher.ui.dialog.Alert;
import net.protolauncher.ui.view.AbstractButtonView;
import net.protolauncher.ui.view.AbstractView;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumSet;

public class AlertView extends AbstractView<BorderPane> {

    // References
    private final Alert alert;
    private final String text;
    private final Throwable exception;
    private final EnumSet<AlertButton> buttons;

    // Components
    private VBox verticalContainer;
    private Label lblText;
    private TextArea txtException;
    private AlertButtonView abvButtons;

    // Constructor
    public AlertView(Alert alert, String text, @Nullable Throwable exception, EnumSet<AlertButton> buttons) {
        super(new BorderPane(), "Colors.css", "Components.css", "view/dialog/AlertView.css");
        this.alert = alert;
        this.text = text;
        this.exception = exception;
        this.buttons = buttons;
        this.getLayout().setId("alert-layout");
        this.construct();
        this.register();
        alert.setUserData(AlertButton.NONE);
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        verticalContainer = new VBox();
        lblText = new Label(text);
        VBox.setVgrow(lblText, Priority.ALWAYS);
        if (exception != null) {
            txtException = new TextArea();
            txtException.setEditable(false);
            txtException.setDisable(false);
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw, true));
            txtException.setText(sw.getBuffer().toString());
        }
        abvButtons = new AlertButtonView(alert, buttons);
    }

    @Override
    protected void register() {
        BorderPane layout = this.getLayout();
        verticalContainer.getChildren().add(lblText);
        if (exception != null) {
            verticalContainer.getChildren().add(txtException);
        }
        layout.setCenter(verticalContainer);
        layout.setBottom(abvButtons.getLayout());
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        this.alert.close();
    }

    /**
     * Represents the button view for the alert dialog view.
     */
    public class AlertButtonView extends AbstractButtonView {

        // References
        private final Alert alert;
        private final EnumSet<AlertButton> buttons;

        // Constructor
        public AlertButtonView(Alert alert, EnumSet<AlertButton> buttons) {
            super();
            this.alert = alert;
            this.buttons = buttons;
            this.getLayout().setId("abv-layout");
            this.construct();
            this.register();
        }

        // AbstractView Implementation
        @Override
        protected void construct() {
            // Call super
            super.construct();

            // Buttons
            if (buttons.contains(AlertButton.OKAY) || buttons.contains(AlertButton.OKAY_BAD)) {
                this.constructButton("alert-okay", "okay", "Okay", event -> {
                    alert.setUserData(AlertButton.OKAY);
                    AlertView.this.closeDialog();
                });
            }
            if (buttons.contains(AlertButton.RETRY)) {
                this.constructButton("alert-retry", "retry", "Retry", event -> {
                    alert.setUserData(AlertButton.RETRY);
                    AlertView.this.closeDialog();
                });
            }
            if (buttons.contains(AlertButton.YES) || buttons.contains(AlertButton.YES_BAD)) {
                this.constructButton("alert-yes", "yes", "Yes", event -> {
                    alert.setUserData(AlertButton.YES);
                    AlertView.this.closeDialog();
                });
            }
            if (buttons.contains(AlertButton.NO) || buttons.contains(AlertButton.NO_BAD)) {
                this.constructButton("alert-no", "no", "No", event -> {
                    alert.setUserData(AlertButton.NO);
                    AlertView.this.closeDialog();
                });
            }

            // "Bad"
            if (buttons.contains(AlertButton.OKAY_BAD)) {
                this.getButton("okay").getButton().getStyleClass().add("red");
            }
            if (buttons.contains(AlertButton.YES_BAD)) {
                this.getButton("yes").getButton().getStyleClass().add("red");
            }
            if (buttons.contains(AlertButton.NO_BAD)) {
                this.getButton("no").getButton().getStyleClass().add("red");
            }
        }

    }

    public enum AlertButton {

        NONE,
        OKAY,
        RETRY,
        YES,
        NO,
        OKAY_BAD,
        YES_BAD,
        NO_BAD

    }

}
