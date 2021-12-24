package net.protolauncher.ui.view.tab;

import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import net.protolauncher.App;
import net.protolauncher.api.Profile;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.mojang.version.VersionType;
import net.protolauncher.ui.ViewScene;
import net.protolauncher.ui.components.PLScrollPane;
import net.protolauncher.ui.dialog.ProfileDialog;
import net.protolauncher.ui.view.AbstractView;
import net.protolauncher.ui.view.LoadingView;
import net.protolauncher.ui.view.dialog.ProfileDialogView;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.protolauncher.App.LOGGER;

public class ProfilesTab extends AbstractView<Pane> {

    // References
    private final ProtoLauncher launcher;

    // Components
    private PLScrollPane spScrollContainer;
    private VBox vboxVerticalContainer;
    private List<HBox> profiles;

    // Constructor
    public ProfilesTab() {
        super(new Pane());
        this.launcher = App.getInstance().getLauncher();
        this.getLayout().setId("pt-layout");
        this.construct();
        this.register();
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
        // TODO: Order by last played.
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
        // Primary Container
        HBox hboxContainer = new HBox();
        hboxContainer.getStyleClass().add("pt-profile-container");
        HBox.setHgrow(hboxContainer, Priority.ALWAYS);
        hboxContainer.setOnMouseClicked(event -> this.profileContainerPressed(profile));
        hboxContainer.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE && ((Parent) event.getSource()).isFocused()) {
                this.profileContainerPressed(profile);
            }
        });

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
        regEditIcon.setOnMouseClicked(event -> {
            this.profileEditButtonPressed(profile);
            event.consume();
        });
        regEditIcon.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE && ((Parent) event.getSource()).isFocused()) {
                this.profileEditButtonPressed(profile);
            }
        });

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

    /**
     * Handles a profile's container being pressed.
     */
    private void profileContainerPressed(Profile profile) {
        ViewScene scene = App.getInstance().getSceneAsViewScene();

        // Switch profile task
        LoadingView lv = new LoadingView();
        Task<Void> switchProfileTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                launcher.switchProfile(profile);
                return null;
            }
        };
        switchProfileTask.setOnSucceeded(event1 -> {
            scene.refresh();
            System.gc();
            lv.hide(scene);
        });
        switchProfileTask.setOnFailed(event1 -> {
            LOGGER.error("Profile switch failed! " + switchProfileTask.getException().getMessage());
            switchProfileTask.getException().printStackTrace();
            lv.hide(scene);
        });
        lv.show(scene, () -> new Thread(switchProfileTask).start());
    }

    /**
     * Handles a profile's edit button being pressed.
     */
    private void profileEditButtonPressed(Profile profile) {
        ViewScene scene = App.getInstance().getSceneAsViewScene();
        ProfileDialog dialog = new ProfileDialog(App.getInstance().getStage());
        dialog.setUserData(profile);
        dialog.setOnHidden(hiddenEvent -> {
            // Refresh the scene
            LoadingView lv = new LoadingView();
            lv.show(scene, () -> {
                scene.refresh();
                System.gc();
                lv.hide(scene);
            });
        });
        ProfileDialogView view = new ProfileDialogView(dialog);
        ((ViewScene) dialog.getScene()).addView(view);
        dialog.show();
    }

}
