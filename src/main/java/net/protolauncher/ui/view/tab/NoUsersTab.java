package net.protolauncher.ui.view.tab;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import net.protolauncher.ui.view.AbstractView;

public class NoUsersTab extends AbstractView<VBox> {

    // Components
    private Label lblTitle;
    private Label lblSubtitle;

    // Constructor
    public NoUsersTab() {
        super(new VBox());
        this.getLayout().setId("nut-layout");
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Title
        lblTitle = new Label("YOU DON'T HAVE ANY USERS");
        lblTitle.setId("nut-title");

        // Subtitle
        lblSubtitle = new Label("Let's add some! Hit the + button to get started.");
        lblSubtitle.setId("nut-subtitle");
    }

    @Override
    protected void register() {
        VBox layout = this.getLayout();
        layout.getChildren().addAll(lblTitle, lblSubtitle);
    }

}
