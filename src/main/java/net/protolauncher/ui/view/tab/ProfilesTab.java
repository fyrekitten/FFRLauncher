package net.protolauncher.ui.view.tab;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import net.protolauncher.App;
import net.protolauncher.api.Profile;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.mojang.version.VersionType;
import net.protolauncher.ui.components.PLScrollPane;
import net.protolauncher.ui.view.AbstractView;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfilesTab extends AbstractView<Pane> {

    // Components
    private PLScrollPane spScrollContainer;
    private VBox vboxVerticalContainer;
    private List<HBox> profiles;

    // Constructor
    public ProfilesTab() {
        super(new Pane());
        this.getLayout().setId("pt-layout");
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Set profiles
        this.profiles = new ArrayList<>();

        // Scroll Container
        spScrollContainer = new PLScrollPane(2);
        spScrollContainer.setId("pt-scroll-container");
        spScrollContainer.prefWidthProperty().bind(this.getLayout().widthProperty());
        spScrollContainer.prefHeightProperty().bind(this.getLayout().heightProperty());

        // Vertical Container
        vboxVerticalContainer = new VBox();
        vboxVerticalContainer.setId("pt-vertical-container");

        // Construct Profiles
        ProtoLauncher launcher = App.getInstance().getLauncher();
        for (Profile profile : Objects.requireNonNull(launcher.getProfiles(launcher.getConfig().getCurrentUserUuid()))) {
            profiles.add(this.constructProfile(profile));
        }
    }

    /**
     * Constructs a new profile component for the list.
     *
     * @param profile The profile we're constructing this component for.
     * @return A new profile component.
     */
    private HBox constructProfile(Profile profile) {
        // Get the launcher
        ProtoLauncher launcher = App.getInstance().getLauncher();

        // Primary Container
        HBox hboxContainer = new HBox();
        hboxContainer.getStyleClass().add("pt-profile-container");
        HBox.setHgrow(hboxContainer, Priority.ALWAYS);
        // TODO: Mouse clicked, key pressed (spacebar)

        // Add selected class if current profile
        if (launcher.getConfig().getCurrentProfileUuid().equals(profile.getUuid())) {
            hboxContainer.getStyleClass().add("pt-profile-selected");
        }

        // Left Container
        HBox hboxLeftContainer = new HBox();
        hboxLeftContainer.getStyleClass().add("pt-profile-left-container");

        // Image
        Image imgImage;
        if (profile.getProfileSettings().getPreviewIconPath() != null) {
            imgImage = new Image(Path.of(profile.getProfileSettings().getPreviewIconPath()).toAbsolutePath().toUri().toString(), -1, 59, true, true);
        } else {
            String resourcePath;
            if (profile.getVersion().getType() == VersionType.SNAPSHOT) {
                resourcePath = "images/profile_snapshot.png";
            } else {
                resourcePath = "images/profile_vanilla.png";
            }
            imgImage = new Image(App.getInstance().getRequiredResourceAsStream(resourcePath), -1, 59, true, true);
        }
        int imgWidth = (int) imgImage.getWidth();
        int imgHeight = (int) imgImage.getHeight();
        ImageView ivImage = new ImageView();
        ivImage.setImage(new WritableImage(imgImage.getPixelReader(), ((imgWidth / 2) - (59 / 2)), 0, 59, imgHeight));
        ivImage.getStyleClass().add("pt-profile-image");
        ivImage.setSmooth(true);

        // Left Text Container
        VBox vboxLeftTextContainer = new VBox();
        vboxLeftTextContainer.getStyleClass().add("pt-profile-left-text-container");

        // Name
        Label lblName = new Label(profile.getName());
        lblName.getStyleClass().add("pt-profile-name");

        // Game Version
        Label lblGameVersion = new Label(profile.getVersion().getMinecraft());
        lblGameVersion.getStyleClass().add("pt-profile-game-version");

        // Right Container
        HBox hboxRightContainer = new HBox();
        hboxRightContainer.getStyleClass().add("pt-profile-right-container");
        HBox.setHgrow(hboxRightContainer, Priority.ALWAYS);

        // Edit Icon
        SVGPath svgEditIcon = new SVGPath();
        svgEditIcon.setContent("M 19.171875 2 C 18.448125 2 17.724375 2.275625 17.171875 2.828125 L 16 4 L 20 8 L 21.171875 6.828125 C 22.275875 5.724125 22.275875 3.933125 21.171875 2.828125 C 20.619375 2.275625 19.895625 2 19.171875 2 z M 14.5 5.5 L 3 17 L 3 21 L 7 21 L 18.5 9.5 L 14.5 5.5 z");
        Region regEditIcon = new Region();
        regEditIcon.setShape(svgEditIcon);
        regEditIcon.getStyleClass().add("pt-profile-edit-icon");
        regEditIcon.setPickOnBounds(true);
        // TODO: Edit icon mouseclick & spacebar

        // Registration
        vboxLeftTextContainer.getChildren().addAll(lblName, lblGameVersion);
        hboxLeftContainer.getChildren().addAll(ivImage, vboxLeftTextContainer);
        hboxRightContainer.getChildren().addAll(regEditIcon);
        hboxContainer.getChildren().addAll(hboxLeftContainer, hboxRightContainer);

        // Return
        return hboxContainer;
    }

    @Override
    protected void register() {
        Pane layout = this.getLayout();

        // Add profiles
        for (HBox profile : profiles) {
            vboxVerticalContainer.getChildren().add(profile);
        }

        // The rest
        spScrollContainer.setContent(vboxVerticalContainer);
        spScrollContainer.applyScrollMultiplier();
        layout.getChildren().add(spScrollContainer);
    }

}
