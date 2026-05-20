package de.p2tools.p2backup.gui.guibig;

import de.p2tools.p2backup.controller.config.ProgConfig;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2lib.guitools.P2GuiTools;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BackupGuiFactory {
    private BackupGuiFactory() {
    }

    public static void changeGui() {
        if (ProgData.getInstance().maskerPane.isVisible()) {
            return;
        }
        ProgConfig.SYSTEM_SMALL_BACKUP.set(!ProgConfig.SYSTEM_SMALL_BACKUP.get());
    }

    public static VBox getInfoPane(String text) {
        HBox hBox = new HBox();
        Label lblTitle = new Label(text);
        hBox.getStyleClass().add("infoPane");
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getChildren().add(lblTitle);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(hBox, P2GuiTools.getHDistance(10));
        return vBox;
    }
}
