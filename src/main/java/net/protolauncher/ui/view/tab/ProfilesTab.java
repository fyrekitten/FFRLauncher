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
import net.protolauncher.api.User;
import net.protolauncher.mods.version.ModdedVersionType;
import net.protolauncher.mojang.version.VersionType;
import net.protolauncher.ui.ViewScene;
import net.protolauncher.ui.components.PLScrollPane;
import net.protolauncher.ui.dialog.ProfileDialog;
import net.protolauncher.ui.view.AbstractView;
import net.protolauncher.ui.view.LoadingView;
import net.protolauncher.ui.view.dialog.ProfileDialogView;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
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
        List<Profile> userProfilesWithGlobals = Objects.requireNonNull(launcher.getProfilesWithGlobals(launcher.getConfig().getCurrentUserUuid()));
        for (Profile profile : userProfilesWithGlobals.stream().sorted(Comparator.comparing(Profile::getLastLaunched).reversed()).toList()) {
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

        // Name & Icon Container
        HBox hboxNameIconContainer = new HBox();
        hboxNameIconContainer.getStyleClass().add("pt-profile-name-icon-container");

        // Name
        Label lblName = new Label(profile.getName());
        lblName.getStyleClass().add("pt-profile-name");

        // Global Icon
        Region regGlobalIcon = null;
        if (profile.getProfileSettings().isGlobal()) {
            SVGPath svgGlobalIcon = new SVGPath();
            svgGlobalIcon.setContent("M62 32C62 15.432 48.568 2 32 2 15.861 2 2.703 14.746 2.031 30.72c-.008.196-.01.395-.014.592-.005.23-.017.458-.017.688v.101C2 48.614 15.432 62 32 62s30-13.386 30-29.899l-.002-.049L62 32M37.99 59.351c-.525-.285-1.029-.752-1.234-1.388-.371-1.152-.084-2.046.342-3.086.34-.833-.117-1.795.109-2.667.441-1.697.973-3.536.809-5.359-.102-1.119-.35-1.17-1.178-1.816-.873-.685-.873-1.654-1.457-2.52-.529-.787.895-3.777.498-3.959-.445-.205-1.457.063-1.777-.362-.344-.458-.584-.999-1.057-1.354-.305-.229-1.654-.995-2.014-.941-1.813.271-3.777-1.497-4.934-2.65-.797-.791-1.129-1.678-1.713-2.593-.494-.775-1.242-.842-1.609-1.803-.385-1.004-.156-2.29-.273-3.346-.127-1.135-.691-1.497-1.396-2.365-1.508-1.863-2.063-4.643-4.924-4.643-1.537 0-1.428 3.348-2.666 2.899-1.4-.507-3.566 1.891-3.535 1.568.164-1.674 1.883-2.488 2.051-2.987.549-1.638-2.453-1.246-2.068-2.612.188-.672 2.098-1.161 1.703-1.562-.119-.122-1.58-1.147-1.508-1.198.271-.19 1.449.412 1.193-.37-.086-.26-.225-.499-.357-.74a27.955 27.955 0 0 1 1.92-1.975c1.014-.083 2.066-.02 2.447.054 2.416.476 3.256 1.699 5.672.794 1.162-.434 5.445.319 6.059 1.537.334.666 1.578-.403 2.063-.475.52-.078 1.695.723 2.053.232.943-1.291-.604-1.827 1.223-.833 1.225.667 3.619-2.266 2.861 1.181-.547 2.485-2.557 2.54-4.031 4.159-1.451 1.594 2.871 2.028 2.982 3.468.32 4.146 2.531-.338 1.939-1.812-1.145-2.855 1.303-2.071 2.289-.257.547 1.007.963.159 1.633-.192.543-.283.688 1.25.805 1.517.385.887 1.65 1.152 1.436 2.294-.238 1.259-1.133.881-2.008 1.094-.977.237.158 1.059.016 1.359-.154.328-1.332.464-1.646.65-.924.544-.359 1.605-1.082 2.175-.496.392-.996.137-1.092.871-.113.865-1.707 1.143-1.5 1.97.057.227.516 1.923.227 2.013-.133.043-1.184-1.475-1.471-1.627-.568-.301-3.15-.055-3.482 1.654-.215 1.105 1.563 2.85 2.016 1.328.561-1.873.828 1.091.693 1.207.268.234 1.836-.385 1.371.7-.197.459.193 1.656.889 1.287.291-.154 1.041.31 1.172.061a2.14 2.14 0 0 1 .742-.692c.701-.41 1.75-.025 2.518.02.469.027 4.313 2.124 4.334 2.545.084 1.575 2.99 1.37 3.436 1.933 1.199 1.526.83.751-.045 2.706-.441.984-.057 2.191-1.125 2.904-.514.342-1.141.171-1.598.655-.412.437-.25.959-.5 1.464-.301.601-4.346 4.236-4.613 5.115-.133.441-1.34.825-.322 1.248.592.174-1.311 1.973-.396 2.718.223.181.369.334.479.471-.457.122-.91.233-1.369.333M35.594 4.237c-.039.145.02.316.271.483.566.375-.162 1.208-.943.671-.779-.537-2.531.241-2.41.644.119.403.66.563 1.496.242.834-.322 1.178.048 1.318.43.096.259 0 .403-.027.752-.025.349-.996.107-1.803.162-.809.054-1.67-.162-1.645-.619.027-.456-.861-1.289-1.391-1.637-.529-.348.232-1.1.934-.537.699.564.727-.107 1.535-.321.459-.122.275-.305.119-.479.86.03 1.708.102 2.546.209m3.517 8.869c.605.164 1.656.929 1.656 1.291 0 .363-.477.817-.688.765-1.523-.371-2.807-1.874-3.514-2.697-1.234-1.435-1.156-.205-3.111-.826-.5-.16-1.293-1.711-.768-2.476s1.131-.886 1.615-.683c.484.2 1.898-.645 2.223.362.322 1.007 1.211 2.292 2.02 2.636.81.342-.04 1.464.567 1.628m.485 4.673c.242.483-1.455-.564-1.859-1.047-.402-.482-1.01-1.571-.523-2.054.484-.482 1.57 1.005 2.141 1.33 1.129.645-.001 1.289.241 1.771m-8.594-7.315c.117-.161.365.242.586.645s-.084.971-.586.885c-.502-.084-.281-1.136 0-1.53m0-4.052s.473 1.154 0 .966c-.473-.188-.496-.671 0-.966m.096 3.65c-.135-.321-.166-1.64.162-2.04.484-.59 1.266.564.74 1.02-.525.457-.768 1.343-.902 1.02m-6.077 1.415c-.879-.063-.898-.823-1.02-1.226s-.85.765-1.586 0c-.736-.765.172-1.771.01-2.376-.162-.604 1.736 0 2.02 0s1.051 1.248 1.252 1.227c.203-.02 1.293.987 1.293.584 0-.402.166-1.088.93-1.168 1.172-.121.121 1.289.08 1.838-.039.549.891 1.504 1.232 1.907.344.403-.867.686-1.07.443-.201-.242-.727 0-1.172.322-.443.322-1.656-.443-2.221-.685-.566-.241 1.131-.804.252-.866m3.141-6.354c.781.269 1.225.51 1.609 0 .371-.492.654 1.073.385 1.502-.27.431-.781.324-.863 0-.08-.32-1.912-1.771-1.131-1.502m1.131 4.859c-.268-.35-.295-.752 0-1.047.297-.295.201-.644.729-.751.26-.054.295.348.295.724s.324.859 0 1.448c-.323.589-.754-.026-1.024-.374m2.205-5.969c-.012.074-.061.118-.184.106a.597.597 0 0 1-.236-.095c.141-.005.279-.009.42-.011M25.389 5.15c.619 0 .539.418 1.051.719.512.3.242-1.552.592-.854.35.697 1.389 1.664.889 1.851-.43.163-2.234.859-2.396.739s-.377-.63-.809-.739c-.432-.107-.889-1.127-1.186-1.1-.113.01-.123-.184-.049-.442a27.533 27.533 0 0 1 1.572-.455c.058.158.146.281.336.281m13.519 30.025c-.645.666-1.756-.464-2.523-.424s-1.152-.765-1.818-.684c-.668.079.182-.847 1.111-.362.927.483 3.756.925 3.23 1.47m12.93-22.934c-.188.24-.402.408-.607.585-.605.524-1.736.484-1.898.846-.162.362-.566 1.489-1.98 1.494-1.414.004-1.01 2.131-1.131 2.738-.121.607-.443 1.325-.848.801-.404-.523-.566-.323-1.816-1.853s-.77-2.375-.365-2.818c.404-.442.566-1.49 0-1.329-.566.161-.889-.202-.768-.703.121-.501.727-.867 0-1.402-.727-.534-.324-2.445-.889-4.189-.566-1.745-1.334-.51-2.586-.443-1.252.067-1.455-.873-.889-1.303a27.948 27.948 0 0 1 13.777 7.576");
            regGlobalIcon = new Region();
            regGlobalIcon.setShape(svgGlobalIcon);
            regGlobalIcon.getStyleClass().add("pt-profile-globe-icon");
        }

        // Fabric Icon
        Region regFabricIcon = null;
        if (profile.getVersion().getModdedType() == ModdedVersionType.FABRIC) {
            SVGPath svgFabricIcon = new SVGPath();
            svgFabricIcon.setContent("M962.5926,1750.866c-31.7789,31.783-39.092,78.1677-15.9652,101.2945,23.1227,23.1269,69.5033,15.81,101.29-15.9692,18.1231-18.1272,28.81-40.9306,29.5883-62.4563l-68.8891-68.8892-46.02,46.02ZM1736.031,768.48c-15.0848-15.0849-36.1193-23.07-60.1838-23.07a115.8843,115.8843,0,0,0-13.4716.8026c-31.8117,3.7344-63.4145,19.8715-88.9817,45.4347-25.5632,25.5673-41.7,57.166-45.4347,88.9818-3.4764,29.6578,4.4263,55.8147,22.2669,73.6553s44.0179,25.7392,73.6512,22.2628c31.8157-3.7343,63.4145-19.8715,88.9817-45.4346,51.2247-51.2287,61.6211-124.1839,23.176-162.633h-.004ZM1538.65,756.904c33.3472-33.3431,75.2522-54.4758,118.0008-59.4918,2.5838-.3031,5.1266-.4545,7.6776-.6388l-479.98-479.98c-15.0848-15.0849-36.1194-23.0654-60.1838-23.0654a114.9366,114.9366,0,0,0-13.4716.8025c-31.8117,3.7344-63.4145,19.8716-88.9818,45.4347-51.2245,51.2287-61.6169,124.1839-23.1718,162.633l479.98,479.98c.1843-2.551.3358-5.0938.6388-7.6734,5.02-42.7445,26.1447-84.6537,59.4918-118.0008ZM981.17,454.7156l-657.3619,657.362c-31.783,31.783-39.0961,78.1676-15.9693,101.2945l578.6046,578.6047c5.2781-26.8,19.2123-53.661,41.4055-75.8542l80.7679-80.7678h0l609.0773-609.0733c-2.5182.1147-5.0324.2211-7.5137.2211-28.1757,0-54.0418-7.5055-75.588-21.841Z");
            regFabricIcon = new Region();
            regFabricIcon.setShape(svgFabricIcon);
            regFabricIcon.getStyleClass().add("pt-profile-fabric-icon");
        }

        // Game Version
        String gameVersionText = profile.getVersion().getMinecraft();
        if (profile.getProfileSettings().isGlobal()) {
            User user = launcher.getUser(profile.getOwner());
            if (user != null) {
                gameVersionText += " - " + user.getUsername();
            } else {
                gameVersionText += " - " + profile.getOwner();
            }
        }
        Label lblGameVersion = new Label(gameVersionText);
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
        if (!profile.getOwner().equals(launcher.getConfig().getCurrentUserUuid())) {
            regEditIcon.getStyleClass().add("disabled");
        } else {
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
        }

        // Registration
        hboxNameIconContainer.getChildren().add(lblName);
        if (regGlobalIcon != null) {
            hboxNameIconContainer.getChildren().add(regGlobalIcon);
        }
        if (regFabricIcon != null) {
            hboxNameIconContainer.getChildren().add(regFabricIcon);
        }
        vboxLeftTextContainer.getChildren().addAll(hboxNameIconContainer, lblGameVersion);
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

        // If we're currently on this profile, don't do anything
        if (profile.getUuid().equals(launcher.getConfig().getCurrentProfileUuid())) {
            return;
        }

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
        lv.show(scene, () -> {
            Thread switchProfileThread = new Thread(switchProfileTask);
            switchProfileThread.setName("Switch Profile Task");
            switchProfileThread.start();
        });
    }

    /**
     * Handles a profile's edit button being pressed.
     */
    private void profileEditButtonPressed(Profile profile) {
        ViewScene scene = App.getInstance().getSceneAsViewScene();
        ProfileDialog dialog = new ProfileDialog(App.getInstance().getStage());
        dialog.setUserData(profile);
        dialog.setOnHidden(hiddenEvent -> {
            // Refresh the scene if requested
            if (dialog.getUserData() == Boolean.TRUE) {
                LoadingView lv = new LoadingView();
                lv.show(scene, () -> {
                    scene.refresh();
                    System.gc();
                    lv.hide(scene);
                });
            }
        });
        ProfileDialogView view = new ProfileDialogView(dialog);
        ((ViewScene) dialog.getScene()).addView(view);
        dialog.show();
    }

}
