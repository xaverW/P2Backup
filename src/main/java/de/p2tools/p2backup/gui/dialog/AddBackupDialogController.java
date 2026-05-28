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
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2Text;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class AddBackupDialogController extends P2DialogExtra {

    private final Button btnOk = new Button("OK");
    private final Button btnCancel = new Button("Abbrechen");
    private final ObjectProperty<BackupInfo> backupInfoProb;
    private final ProgData progData;
    private boolean ok = false;

    public AddBackupDialogController(ProgData progData, ObjectProperty<BackupInfo> backupInfoProb) {
        super(ProgData.getInstance().primaryStage, null, "Backup anlegen",
                true, false, false, DECO.NO_BORDER);

        this.progData = progData;
        this.backupInfoProb = backupInfoProb;
        init(true);
    }

    public boolean isOk() {
        return ok;
    }

    @Override
    public void make() {
        TextField txtName = new TextField();
        backupInfoProb.get().nameProperty().bind(txtName.textProperty());
        txtName.setOnAction(a -> {
            if (!txtName.getText().isEmpty()) {
                add();
            }
        });

        TextArea txtDescription = new TextArea();
        backupInfoProb.get().descriptionProperty().bind(txtDescription.textProperty());

        GridPane gridPane = new GridPane();
        gridPane.setHgap(P2LibConst.DIST_GRIDPANE_HGAP);
        gridPane.setVgap(P2LibConst.DIST_GRIDPANE_VGAP);

        int row = 0;
        Button btnHelp = PIconFactory.getHelpButton(getStage(), "Name angeben",
                "Der Name wird zur Unterscheidung der Backups gebraucht." +
                        "\n\n" +
                        "Die Beschreibung " +
                        "ist optional und dient nur zum Anlegen eigener Infos zum Backup");
        Text text = P2Text.getTextBold("Bitte einen Namen für das Backup angeben");
        gridPane.add(text, 0, row, 2, 1);

        gridPane.add(new Label(), 0, ++row);
        gridPane.add(new Label("Name:"), 0, ++row);
        gridPane.add(txtName, 1, row);
        gridPane.add(new Label("Beschreibung:"), 0, ++row);
        gridPane.add(txtDescription, 1, row);

        getVBoxCont().getChildren().addAll(gridPane);

        HBox hBoxBtn = new HBox(P2LibConst.SPACING_HBOX);
        hBoxBtn.setAlignment(Pos.CENTER_RIGHT);
        hBoxBtn.getChildren().addAll(btnHelp, btnCancel, btnOk);
        getVBoxCont().getChildren().addAll(hBoxBtn);

        btnOk.setOnAction(a -> add());
        btnOk.disableProperty().bind(txtName.textProperty().isEmpty());
        btnCancel.setOnAction(a -> close());
    }

    private void add() {
        ok = true;
        backupInfoProb.get().nameProperty().unbind();
        backupInfoProb.get().descriptionProperty().unbind();
        close();
    }
}
