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


import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.P2Text;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class DialogBackupError extends P2DialogExtra {

    private final Button btnOk = new Button("Ok");
    private final BackupInfo backupInfo;


    public DialogBackupError(Stage stage, BackupInfo backupInfo) {
        super(stage, null, "Backup",
                true, false, false, DECO.NO_BORDER);

        this.backupInfo = backupInfo;
        init(true);
    }

    @Override
    public void make() {
        Text text = P2Text.getTextBold("Das Backup konnte nicht erstellt werden,\n" +
                "es wurde mit einem " +
                "Fehler abgebrochen.");
        Label lblName = P2Text.getLblTextBold("Backupname:");

        HBox hBoxName = new HBox(5);
        hBoxName.setAlignment(Pos.CENTER_LEFT);
        hBoxName.getChildren().addAll(lblName, new Label(backupInfo.getName()));

        VBox vBox = new VBox(5);
        vBox.setPadding(new Insets(0, 20, 0, 0));
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.getChildren().addAll(text, P2GuiTools.getVDistance(50), hBoxName);

        Node errorNode = PIconFactory.getAttentionIcon("mdoal-error");
        HBox hBox = new HBox(20);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(errorNode, vBox);

        getVBoxCont().getChildren().addAll(hBox);
        addOkButton(btnOk);
        btnOk.setOnAction(a -> close());
    }
}
