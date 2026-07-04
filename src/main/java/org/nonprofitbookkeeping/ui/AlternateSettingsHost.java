package org.nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nonprofitbookkeeping.ui.panels.SettingsPanelFX;

/**
 * Alternate-shell chrome around the classic JavaFX settings panel.
 *
 * <p>The wrapped panel remains the source of settings controls so the alternate
 * shell does not keep a second placeholder settings implementation while
 * shared save-command extraction is still in progress.</p>
 */
public class AlternateSettingsHost implements AppPanel, AppPanel.SaveAware
{
    private final BorderPane root = new BorderPane();
    private final SettingsPanelFX settingsPanel;
    private final Label status = new Label(
        "Settings use the same controls as the classic workspace; use the embedded Save Settings button.");

    public AlternateSettingsHost()
    {
        this(null, null);
    }

    AlternateSettingsHost(Stage owner, nonprofitbookkeeping.service.SettingsService settingsService)
    {
        this.settingsPanel = new SettingsPanelFX(owner, settingsService,
            () -> this.status.setText("Settings saved through the shared settings panel."));
        build();
    }

    @Override
    public String title()
    {
        return "Settings";
    }

    @Override
    public Node root()
    {
        return this.root;
    }

    @Override
    public SaveResult save()
    {
        return SaveResult.unsupported(
            "Use the Save Settings button in the hosted settings panel.");
    }

    SettingsPanelFX hostedPanelForTest()
    {
        return this.settingsPanel;
    }

    Label statusForTest()
    {
        return this.status;
    }

    private void build()
    {
        Label title = new Label("Settings");
        title.getStyleClass().add("panel-title");
        this.status.setWrapText(true);

        VBox header = new VBox(6, title, this.status, new Separator());
        header.setPadding(new Insets(8));
        this.root.setTop(header);
        this.root.setCenter(this.settingsPanel);
        this.root.setPadding(new Insets(8));
        this.root.getStyleClass().add("alternate-content-card");
    }
}
