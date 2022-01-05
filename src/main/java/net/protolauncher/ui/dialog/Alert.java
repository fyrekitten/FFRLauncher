package net.protolauncher.ui.dialog;

import javafx.stage.Window;
import net.protolauncher.ui.ViewScene;
import net.protolauncher.ui.view.dialog.AlertView;
import net.protolauncher.ui.view.dialog.AlertView.AlertButton;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Represents a ProtoLauncher alert for sub-windows.
 */
public class Alert extends Dialog {

    /**
     * Constructs a new alert with an owner, title, and the given text and buttons.
     *
     * @param owner The owner of this alert, if any.
     * @param title The title of this alert.
     * @param text The text for this alert.
     * @param exception An exception, if applicable.
     * @param buttons The buttons from {@link AlertButton} to include for this alert.
     */
    public Alert(@Nullable Window owner, String title, String text, @Nullable Throwable exception, EnumSet<AlertButton> buttons) {
        super(owner, title, exception == null ? 450 : 650, exception == null ? 200 : 350, exception == null ? 450 : 650, exception == null ? 200 : 350);
        AlertView view = new AlertView(this, text, exception, buttons);
        ((ViewScene) this.getScene()).addView(view);
    }

}
