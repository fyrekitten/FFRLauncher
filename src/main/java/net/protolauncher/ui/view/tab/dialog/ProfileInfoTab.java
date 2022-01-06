package net.protolauncher.ui.view.tab.dialog;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import net.protolauncher.App;
import net.protolauncher.api.Profile;
import net.protolauncher.api.ProtoLauncher;
import net.protolauncher.mojang.version.VersionInfo;
import net.protolauncher.mojang.version.VersionType;
import net.protolauncher.ui.dialog.ProfileDialog;
import net.protolauncher.ui.view.AbstractView;

public class ProfileInfoTab extends AbstractView<VBox> {

    // References
    private final ProfileDialog dialog;
    private final ProtoLauncher launcher;

    // Components
    private VBox vboxNameContainer;
    private Label lblName;
    private TextField txtName;
    private VBox vboxVersionContainer;
    private Label lblVersion;
    private CheckBox chkLatest;
    private HBox hboxVersionHorizontalContainer;
    private ComboBox<VersionInfo> cbVersion;
    private ComboBox<VersionType> cbVersionType;

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

    // AbstractView Implementation
    @Override
    protected void construct() {
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

        // Version Option
        vboxVersionContainer = new VBox();
        vboxVersionContainer.setId("pit-version-container");
        vboxVersionContainer.getStyleClass().add("option");

        // Version label
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

        // Check for an existing profile and apply autofills
        Profile existingProfile = (Profile) this.dialog.getUserData();
        if (existingProfile != null) {
            txtName.setText(existingProfile.getName());
            VersionType type = existingProfile.getVersion().getType();
            cbVersionType.setValue(type);
            cbVersion.getItems().addAll(launcher.getVersionManifest().getVersionsOfType(type));
            cbVersion.setValue(launcher.getVersionManifest().getVersion(existingProfile.getVersion().getMinecraft()));
            if (existingProfile.getVersion().isLatest()) {
                chkLatest.setSelected(true);
                cbVersion.setDisable(true);
            }
        } else {
            txtName.setText("New Profile");
            cbVersionType.setValue(VersionType.RELEASE);
            cbVersion.getItems().addAll(launcher.getVersionManifest().getVersionsOfType(VersionType.RELEASE));
            cbVersion.setValue(launcher.getVersionManifest().getLatestRelease());
        }
    }

    @Override
    protected void register() {
        VBox layout = this.getLayout();
        vboxNameContainer.getChildren().addAll(lblName, txtName);
        hboxVersionHorizontalContainer.getChildren().addAll(cbVersion, cbVersionType);
        vboxVersionContainer.getChildren().addAll(lblVersion, hboxVersionHorizontalContainer, chkLatest);
        layout.getChildren().addAll(vboxNameContainer, vboxVersionContainer);
    }

}
