package net.protolauncher.ui.dialog;

import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.protolauncher.App;
import net.protolauncher.ui.ViewScene;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a ProtoLauncher dialog users for sub-windows.
 */
public abstract class Dialog extends Stage {

    /**
     * Constructs a new dialog with the window as the owner and the requested view as the view.
     *
     * @param owner The owner of this dialog, if any.
     * @param title The title of this dialog.
     * @param minWidth The minimum width of this dialog.
     * @param minHeight The minimum height of this dialog.
     * @param width The initial width of this dialog.
     * @param height This initial height of this dialog.
     */
    public Dialog(@Nullable Window owner, String title, double minWidth, double minHeight, double width, double height) {
        this.initOwner(owner);
        this.initModality(Modality.WINDOW_MODAL);
        this.setTitle(title);
        this.getIcons().add(new Image(App.getInstance().getRequiredResourceAsStream("icon.png")));
        this.setMinWidth(minWidth);
        this.setMinHeight(minHeight);
        this.setWidth(width);
        this.setHeight(height);
        this.setScene(new ViewScene());
        this.initPosition(owner);
    }

    /**
     * Initializes the position of this dialog.
     * @param owner The owner of this dialog.
     */
    public void initPosition(@Nullable Window owner) {
        if (owner != null) {
            this.setX(owner.getX() + (owner.getWidth() / 2) - (this.getWidth() / 2));
            this.setY(owner.getY() + (owner.getHeight() / 2) - (this.getHeight() / 2));
        } else {
            Screen screen = Screen.getPrimary();
            this.setX((screen.getVisualBounds().getMaxX() / 2) - (this.getWidth() / 2));
            this.setY((screen.getVisualBounds().getMaxY() / 2) - (this.getHeight() / 2));
        }
    }

}
