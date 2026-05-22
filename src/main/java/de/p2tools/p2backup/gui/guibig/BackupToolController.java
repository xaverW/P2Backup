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

package de.p2tools.p2backup.gui.guibig;

import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.gui.tools.*;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class BackupToolController extends VBox {

    private final ProgData progData;
    private final VBox vBoxContent = new VBox();

    public BackupToolController() {
        progData = ProgData.getInstance();

        setPadding(new Insets(P2LibConst.PADDING_VBOX));
        setSpacing(P2LibConst.SPACING_VBOX);
        getChildren().addAll(new TitleBox());
        getChildren().add(vBoxContent);

        init();
    }

    private void init() {
        progData.backupInfoProperty.addListener((u, o, n) -> {
            vBoxContent.disableProperty().unbind();
            vBoxContent.setDisable(progData.backupInfoProperty.get() == null);
            if (progData.backupInfoProperty.get() != null) {
                vBoxContent.disableProperty().bind(progData.backupInfoProperty.get().runnerDto.runningProperty());
            }
        });

        vBoxContent.getChildren().addAll(BackupGuiFactory.getInfoPane("Werkzeuge"),
                P2GuiTools.getHDistance(15));

        // Infos
        Button btnInfo = new Button("Backup Infos");
        btnInfo.setMaxWidth(Double.MAX_VALUE);
        btnInfo.setOnAction(a -> {
            if (progData.backupInfoProperty.get() != null) {
                new BackupInfoDialogController(progData.backupInfoProperty.get());
            }
        });
        Label lblInfo = new Label("Damit werden Infos über die " +
                "Backups angezeigt.");
        lblInfo.setWrapText(true);
        lblInfo.getStyleClass().add("lblToolInfo");


        // Backup durchsuchen
        Button btnBackup = new Button("Backup Durchsuchen");
        btnBackup.setMaxWidth(Double.MAX_VALUE);
        btnBackup.setOnAction(a -> {
            if (progData.backupInfoProperty.get() != null) {
                new SearchInBackupDialogController(progData.backupInfoProperty.get()).showDialog();
            }
        });
        Label lblBackup = new Label("Damit kann man Dateien in einem Backup suchen.");
        lblBackup.setWrapText(true);
        lblBackup.getStyleClass().add("lblToolInfo");


        // Vergleich
        Button btnCompare = new Button("Daten und Backup vergleichen");
        btnCompare.setMaxWidth(Double.MAX_VALUE);
        btnCompare.setOnAction(a -> {
            if (progData.backupInfoProperty.get() != null) {
                new CompareBackupDialogController(progData.backupInfoProperty.get()).showDialog();
            }
        });
        Label lblCompare = new Label("Damit kann man den Ordner mit den Daten mit " +
                "einem Backup vergleichen. So kann geprüft werden, was sich geändert hat.");
        lblCompare.setWrapText(true);
        lblCompare.getStyleClass().add("lblToolInfo");


        // Prüfen
        Button btnCheck = new Button("Backups überprüfen");
        btnCheck.setMaxWidth(Double.MAX_VALUE);
        btnCheck.setOnAction(a -> {
            if (progData.backupInfoProperty.get() != null) {
                new CheckBackupDialogController(progData.backupInfoProperty.get()).showDialog();
            }
        });
        Label lblCheck = new Label("Hiermit kann überprüft werden, ob sich ein Backup geändert hat. Es wird " +
                "angezeigt, ob im Backup Dateien geändert oder entfernt wurden.");
        lblCheck.setWrapText(true);
        lblCheck.getStyleClass().add("lblToolInfo");


        // Geblockt
        Button btnBlocked = new Button("Geblockte Dateien Suchen");
        btnBlocked.setMaxWidth(Double.MAX_VALUE);
        btnBlocked.setOnAction(a -> {
            if (progData.backupInfoProperty.get() != null) {
                new BlockedFilesDialogController(progData.backupInfoProperty.get()).showDialog();
            }
        });
        Label lblBlocked = new Label("Dient zur Anzeige, welche Dateien ins Backup kopiert werden und " +
                "welche nicht im Backup landen.");
        lblBlocked.setWrapText(true);
        lblBlocked.getStyleClass().add("lblToolInfo");

        final GridPane gridPane = new GridPane();
        gridPane.setHgap(15);
        gridPane.setVgap(15);
        gridPane.getColumnConstraints().addAll(P2GridConstraints.getCcPrefSize(),
                P2GridConstraints.getCcComputedSizeAndHgrow());

        int row = 0;
        gridPane.add(btnInfo, 0, row);
        gridPane.add(lblInfo, 1, row);

        gridPane.add(btnBackup, 0, ++row);
        gridPane.add(lblBackup, 1, row);

        gridPane.add(btnCompare, 0, ++row);
        gridPane.add(lblCompare, 1, row);

        gridPane.add(btnCheck, 0, ++row);
        gridPane.add(lblCheck, 1, row);

        gridPane.add(btnBlocked, 0, ++row);
        gridPane.add(lblBlocked, 1, row);

        lblInfo.setMaxWidth(Double.MAX_VALUE);
        GridPane.setVgrow(lblInfo, Priority.ALWAYS);
        lblBackup.setMaxWidth(Double.MAX_VALUE);
        GridPane.setVgrow(lblInfo, Priority.ALWAYS);
        lblCompare.setMaxWidth(Double.MAX_VALUE);
        GridPane.setVgrow(lblCompare, Priority.ALWAYS);
        lblCheck.setMaxWidth(Double.MAX_VALUE);
        GridPane.setVgrow(lblCheck, Priority.ALWAYS);
        lblBlocked.setMaxWidth(Double.MAX_VALUE);
        GridPane.setVgrow(lblBlocked, Priority.ALWAYS);

        vBoxContent.getChildren().addAll(gridPane);
    }
}
