/*
 * MTViewer Copyright (C) 2017 W. Xaver W.Xaver[at]googlemail.com
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

package de.p2tools.p2backup.gui.dialog;


import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.P2DirFileChooser;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2Text;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class DialogAskBackupPath extends P2DialogExtra {

    private final Button btnOk = new Button("OK");
    private final Button btnCancel = new Button("Abbrechen");
    private final TextField txtPath = new TextField();
    private final ObjectProperty<BackupInfo> backupDataProb;
    private final ProgData progData;
    private boolean ok = false;

    public DialogAskBackupPath(ProgData progData, ObjectProperty<BackupInfo> backupDataProb) {
        super(ProgData.getInstance().primaryStage, null, "Ordner für das Backup",
                true, false, false, DECO.NO_BORDER);

        this.progData = progData;
        this.backupDataProb = backupDataProb;
        init(true);
    }

    public boolean isOk() {
        return ok;
    }

    @Override
    public void close() {
        txtPath.textProperty().unbindBidirectional(backupDataProb.get().backupPathProperty());
        super.close();
    }

    @Override
    public void make() {
        txtPath.textProperty().bindBidirectional(backupDataProb.get().backupPathProperty());

        GridPane gridPane = new GridPane();
        gridPane.setHgap(P2LibConst.DIST_GRIDPANE_HGAP);
        gridPane.setVgap(P2LibConst.DIST_GRIDPANE_VGAP);

        int row = 0;
        Button btnHelp = P2IconFactory.getHelpButton(getStage(), "Ordner für das Backup",
                "Es muss ein Ordner angegeben werden, in dem das Backup " +
                        "gespeichert werden kann.");

        Button btnPath = new Button();
        btnPath.setGraphic(P2IconFactory.P2ICON.P2_BTN_OPEN_DIR.getFontIcon());
        btnPath.setTooltip(new Tooltip("Den Ordner zum Sichern auswählen"));
        btnPath.setOnAction(event -> {
            String path = P2DirFileChooser.DirChooser(ProgData.getInstance().primaryStage, "");
            if (!path.isEmpty()) {
                txtPath.setText(path);
            }
        });


        Text text = P2Text.getTextBold("Bitte einen Ordner für das Backup angeben");
        gridPane.add(text, 0, row, 2, 1);

        gridPane.add(new Label(), 0, ++row);
        gridPane.add(new Label("Backupordner:"), 0, ++row);
        gridPane.add(txtPath, 1, row);
        gridPane.add(btnPath, 2, row);
        GridPane.setHgrow(txtPath, Priority.ALWAYS);
        getVBoxCont().getChildren().addAll(gridPane);
        VBox.setVgrow(gridPane, Priority.ALWAYS);

        HBox hBoxBtn = new HBox(P2LibConst.SPACING_HBOX);
        hBoxBtn.setAlignment(Pos.CENTER_RIGHT);
        hBoxBtn.getChildren().addAll(btnHelp, btnCancel, btnOk);
        getVBoxCont().getChildren().addAll(hBoxBtn);

        btnOk.setOnAction(a -> add());
        btnOk.disableProperty().bind(txtPath.textProperty().isEmpty());
        btnCancel.setOnAction(a -> close());
    }

    private void add() {
        ok = true;
        close();
    }
}
