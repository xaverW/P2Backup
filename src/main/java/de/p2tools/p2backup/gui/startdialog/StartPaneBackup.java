/*
 * MTPlayer Copyright (C) 2017 W. Xaver W.Xaver[at]googlemail.com
 * https://www.p2tools.de
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package de.p2tools.p2backup.gui.startdialog;


import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.dialog.P2Dialog;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.P2Text;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class StartPaneBackup extends VBox {
    private final P2Dialog pDialog;
    private final ObjectProperty<BackupInfo> backupDataProb;

    public StartPaneBackup(P2Dialog pDialog, ObjectProperty<BackupInfo> backupDataProb) {
        this.pDialog = pDialog;
        this.backupDataProb = backupDataProb;
    }

    public void close() {
    }

    public void makeStart() {
        HBox hBox = new HBox();
        hBox.getStyleClass().add("startInfo_2");
        hBox.setPadding(new Insets(P2LibConst.PADDING));
        hBox.setMaxWidth(Double.MAX_VALUE);
        hBox.setMinHeight(Region.USE_PREF_SIZE);
        Label lbl = new Label("Hier wird das erste Backup angelegt. Es muss ein Name angegeben " +
                "werden. Der Name dient zur Unterscheidung " +
                "der Backups im Programm. Die Beschreibung ist für eigene Infos zum Backup " +
                "und ist optional. " +
                "Weitere Einstellungen werden dann später im Programm vorgenommen.");

        lbl.setWrapText(true);
        lbl.setPrefWidth(500);
        hBox.getChildren().add(lbl);
        getChildren().addAll(StartFactory.getTitle("Backup"), hBox, P2GuiTools.getHDistance(20));

        Button btnHelp = PIconFactory.getHelpButton(pDialog.getStage(), "Name angeben",
                "Der Name wird zur Unterscheidung der Backups gebraucht." +
                        "\n\n" +
                        "Die Beschreibung " +
                        "ist optional und dient nur zum Anlegen eigener Infos zum Backup");

        final GridPane gridPane = new GridPane();
        gridPane.setHgap(P2LibConst.DIST_GRIDPANE_HGAP);
        gridPane.setVgap(P2LibConst.DIST_GRIDPANE_VGAP);

        TextField txtName = new TextField();
        txtName.setText("Mein erstes Backup");
        backupDataProb.get().setName(txtName.getText());
        txtName.textProperty().addListener((u, o, n) -> {
            backupDataProb.get().setName(txtName.getText());
        });

        TextArea txtDescription = new TextArea();
        backupDataProb.get().descriptionProperty().bind(txtDescription.textProperty());

        int row = 0;
        Text text = P2Text.getTextBold("Bitte einen Namen für das Backup angeben");
        gridPane.add(text, 0, row, 2, 1);

        gridPane.add(new Label(), 0, ++row);
        gridPane.add(new Label("Name:"), 0, ++row);
        gridPane.add(txtName, 1, row);
        gridPane.add(new Label("Beschreibung:"), 0, ++row);
        gridPane.add(txtDescription, 1, row);

        gridPane.add(btnHelp, 1, ++row);
        GridPane.setHalignment(btnHelp, HPos.RIGHT);

        gridPane.getColumnConstraints().addAll(P2GridConstraints.getCcPrefSize(),
                P2GridConstraints.getCcComputedSizeAndHgrow());
        getChildren().add(gridPane);
    }
}
