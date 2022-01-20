package net.protolauncher.ui.view.tab.dialog;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import net.protolauncher.App;
import net.protolauncher.api.Profile;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.mods.version.ModdedVersionInfo;
import net.protolauncher.mods.version.ModdedVersionType;
import net.protolauncher.mojang.version.VersionInfo;
import net.protolauncher.mojang.version.VersionType;
import net.protolauncher.ui.components.PLScrollPane;
import net.protolauncher.ui.dialog.ProfileDialog;
import net.protolauncher.ui.view.AbstractView;

import java.io.IOException;
import java.util.Objects;

public class ProfileInfoTab extends AbstractView<Pane> {

    // References
    private final ProfileDialog dialog;
    private final ProtoLauncher launcher;

    // Components
    private PLScrollPane spScrollContainer;
    private VBox vboxContainer;
    private VBox vboxNameContainer;
    private Label lblName;
    private TextField txtName;
    private VBox vboxVersionContainer;
    private Label lblVersion;
    private HBox hboxVersionHorizontalContainer;
    private ComboBox<VersionInfo> cbVersion;
    private ComboBox<VersionType> cbVersionType;
    private CheckBox chkLatest;
    private CheckBox chkInjectFabric;
    private ComboBox<ModdedVersionInfo> cbFabricVersion;
    private VBox vboxPathsContainer;
    private Label lblPaths;
    private TextField txtPath;
    private boolean pathModified = false;

    // Constructor
    public ProfileInfoTab(ProfileDialog dialog) {
        super(new VBox());
        this.dialog = dialog;
        this.launcher = App.getInstance().getLauncher();
        this.getLayout().setId("pit-layout");
        this.construct();
        this.register();
    }

    // Getters
    public String getName() {
        return txtName.getText();
    }
    public VersionInfo getVersion() {
        return cbVersion.getValue();
    }
    public CheckBox getLatest() {
        return chkLatest;
    }
    public CheckBox getInjectFabric() {
        return chkInjectFabric;
    }
    public ModdedVersionInfo getFabricVersion() {
        return cbFabricVersion.getValue();
    }
    public boolean isPathModified() {
        return pathModified;
    }
    public String getPath() {
        return txtPath.getText();
    }

    // AbstractView Implementation
    @Override
    protected void construct() {
        // Scroll Container
        spScrollContainer = new PLScrollPane(2);
        spScrollContainer.setId("pit-scroll-container");
        spScrollContainer.prefWidthProperty().bind(this.getLayout().widthProperty());
        spScrollContainer.prefHeightProperty().bind(this.getLayout().heightProperty());

        // Container
        vboxContainer = new VBox();
        vboxContainer.setId("pit-container");

        // Name Option
        vboxNameContainer = new VBox();
        vboxNameContainer.setId("pit-name-container");
        vboxNameContainer.getStyleClass().add("option");

        // Name Label
        lblName = new Label("Name");
        lblName.setId("pit-name-label");

        // Name Text Field
        txtName = new TextField();
        txtName.setId("pit-name");
        txtName.setTooltip(new Tooltip("The name of this profile."));
        txtName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!pathModified) {
                try {
                    txtPath.setText(Profile.getDefaultLocation(!txtName.getText().isEmpty() ? txtName.getText().trim() : "(profile name)", Objects.requireNonNull(launcher.getCurrentUser()).getUuid()).toAbsolutePath().toString());
                } catch (IOException e) {
                    txtPath.setText("???");
                    txtPath.setDisable(true);
                }
            }
        });

        // Version Option
        vboxVersionContainer = new VBox();
        vboxVersionContainer.setId("pit-version-container");
        vboxVersionContainer.getStyleClass().add("option");

        // Version Label
        lblVersion = new Label("Version");
        lblVersion.setId("pit-version-label");

        // Version Horizontal Container
        hboxVersionHorizontalContainer = new HBox();
        hboxVersionHorizontalContainer.setId("pit-version-horizontal-container");

        // Version ChoiceBox
        cbVersion = new ComboBox<>();
        cbVersion.setId("pit-version");
        cbVersion.setTooltip(new Tooltip("The version of Minecraft this profile should launch."));
        hboxVersionHorizontalContainer.widthProperty().addListener((observable, oldValue, newValue) -> cbVersion.setPrefWidth(hboxVersionHorizontalContainer.getWidth() / 3.0 * 2));
        cbVersion.setConverter(new StringConverter<>() {
            @Override
            public String toString(VersionInfo object) {
                if (object == null) {
                    return null;
                }

                String str = "Minecraft ";
                if (object.getType() == VersionType.OLD_ALPHA) {
                    str += "Alpha ";
                } else if (object.getType() == VersionType.OLD_BETA) {
                    str += "Beta ";
                } else if (object.getType() == VersionType.SNAPSHOT) {
                    str += "Snapshot ";
                }
                str += object.getId();
                return str;
            }

            @Override
            public VersionInfo fromString(String string) {
                return null;
            }
        });
        cbVersion.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE && cbVersion.isFocused()) {
                cbVersion.show();
            }
        });
        cbVersion.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue && newValue != null && newValue.getType() == VersionType.RELEASE) {
                cbFabricVersion.getItems().clear();
                cbFabricVersion.getItems().addAll(launcher.getModdedVersionManifest().getVersionsOfTypeWithMcv(ModdedVersionType.FABRIC, newValue.getId()));
                if (cbFabricVersion.getItems().size() > 0) {
                    cbFabricVersion.setValue(cbFabricVersion.getItems().get(0));
                }
            }
        });

        // Version Type ChoiceBox
        cbVersionType = new ComboBox<>();
        cbVersionType.setId("pit-version-type");
        cbVersionType.setTooltip(new Tooltip("Changes the list of available Minecraft versions."));
        hboxVersionHorizontalContainer.widthProperty().addListener((observable, oldValue, newValue) -> cbVersionType.setPrefWidth(hboxVersionHorizontalContainer.getWidth() / 3.0));
        cbVersionType.getItems().addAll(VersionType.values());
        cbVersionType.setConverter(new StringConverter<>() {
            @Override
            public String toString(VersionType object) {
                return object == null ? null : object.getName();
            }

            @Override
            public VersionType fromString(String string) {
                return null;
            }
        });
        cbVersionType.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE && cbVersionType.isFocused()) {
                cbVersionType.show();
            }
        });
        cbVersionType.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue) {
                cbVersion.getItems().clear();
                cbVersion.getItems().addAll(launcher.getVersionManifest().getVersionsOfType(newValue));
                cbVersion.setValue(cbVersion.getItems().get(0));
                if (newValue != VersionType.RELEASE) {
                    chkInjectFabric.setSelected(false);
                    chkInjectFabric.setDisable(true);
                } else {
                    chkInjectFabric.setDisable(false);
                }
            }
        });

        // Latest Checkbox
        chkLatest = new CheckBox("Keep Profile up to Date");
        chkLatest.setId("pit-latest");
        chkLatest.setTooltip(new Tooltip("Whether to keep this profile up to date with the latest version of Minecraft or not."));
        chkLatest.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                cbVersion.setValue(launcher.getVersionManifest().getVersionsOfType(cbVersionType.getValue()).get(0));
                cbVersion.setDisable(true);
            } else {
                cbVersion.setDisable(false);
            }
        });

        // Inject Fabric Checkbox
        chkInjectFabric = new CheckBox("Inject Fabric");
        chkInjectFabric.setId("pit-inject-fabric");
        chkInjectFabric.setTooltip(new Tooltip("Whether to inject the Fabric Mod Loader to this profile or not."));
        chkInjectFabric.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chkLatest.setSelected(false);
                chkLatest.setDisable(true);
                cbFabricVersion.setDisable(false);
            } else {
                chkLatest.setDisable(false);
                cbFabricVersion.setDisable(true);
            }
        });

        // Fabric Version Type ComboBox
        cbFabricVersion = new ComboBox<>();
        cbFabricVersion.setId("pt-fabric-version-type");
        cbFabricVersion.setTooltip(new Tooltip("The Fabric version that should be injected."));
        cbFabricVersion.prefWidthProperty().bind(hboxVersionHorizontalContainer.widthProperty());
        cbFabricVersion.setConverter(new StringConverter<>() {
            @Override
            public String toString(ModdedVersionInfo object) {
                if (object == null) {
                    return null;
                }
                return "Fabric " + object.getLv();
            }

            @Override
            public ModdedVersionInfo fromString(String string) {
                return null;
            }
        });
        cbFabricVersion.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE && cbFabricVersion.isFocused()) {
                cbFabricVersion.show();
            }
        });

        // Paths Option
        vboxPathsContainer = new VBox();
        vboxPathsContainer.setId("pit-paths-container");
        vboxPathsContainer.getStyleClass().add("option");

        // Paths Label
        lblPaths = new Label("Profile Path");
        lblPaths.setId("pit-paths-label");

        // Paths Text Field
        txtPath = new TextField();
        txtPath.setId("pit-path");
        txtPath.setTooltip(new Tooltip("The folder on your system where this profile will launch."));
        txtPath.setOnKeyPressed(event -> {
            if (!pathModified) {
                pathModified = true;
            }
        });

        // Check for an existing profile and apply autofills
        Profile existingProfile = (Profile) this.dialog.getUserData();
        if (existingProfile != null) {
            txtName.setText(existingProfile.getName());
            VersionType type = existingProfile.getVersion().getType();
            cbVersionType.setValue(type);
            if (existingProfile.getVersion().isLatest()) {
                chkLatest.setSelected(true);
                cbVersion.setDisable(true);
            }
            ModdedVersionType moddedType = existingProfile.getVersion().getModdedType();
            if (moddedType == null) {
                cbFabricVersion.setDisable(true);
            } else if (moddedType == ModdedVersionType.FABRIC) {
                chkInjectFabric.setSelected(true);
            }
            pathModified = true;
            txtPath.setText(existingProfile.getPath());
        } else {
            txtName.setText("New Profile");
            cbVersionType.setValue(VersionType.RELEASE);
            cbVersion.getItems().addAll(launcher.getVersionManifest().getVersionsOfType(VersionType.RELEASE));
            cbVersion.setValue(launcher.getVersionManifest().getLatestRelease());
            cbFabricVersion.setDisable(true);
        }
    }

    @Override
    protected void register() {
        Pane layout = this.getLayout();
        vboxNameContainer.getChildren().addAll(lblName, txtName);
        hboxVersionHorizontalContainer.getChildren().addAll(cbVersion, cbVersionType);
        vboxVersionContainer.getChildren().addAll(lblVersion, hboxVersionHorizontalContainer, chkLatest, chkInjectFabric, cbFabricVersion);
        vboxPathsContainer.getChildren().addAll(lblPaths, txtPath);
        vboxContainer.getChildren().addAll(vboxNameContainer, vboxVersionContainer, vboxPathsContainer);
        spScrollContainer.setContent(vboxContainer);
        spScrollContainer.applyScrollMultiplier();
        layout.getChildren().add(spScrollContainer);
    }

}
