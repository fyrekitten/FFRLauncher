package net.protolauncher.ui.view.tab;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import net.protolauncher.ui.view.AbstractView;

public class NoProfilesTab extends AbstractView<VBox> {

    // Components
    private Label lblTitle;
    private Label lblSubtitle;

    // Constructor
    public NoProfilesTab() {
        super(new VBox());
        this.getLayout().setId("npt-layout");
        this.construct();
        this.register();
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Title
        lblTitle = new Label("YOU DON'T HAVE ANY PROFILES");
        lblTitle.setId("npt-title");

        // Subtitle
        lblSubtitle = new Label("Let's create some! Hit the + button to get started.");
        lblSubtitle.setId("npt-subtitle");
    }

    @Override
    protected void register() {
        VBox layout = this.getLayout();
        layout.getChildren().addAll(lblTitle, lblSubtitle);
    }

}
