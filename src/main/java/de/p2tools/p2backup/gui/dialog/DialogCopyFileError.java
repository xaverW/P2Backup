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
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.P2Text;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class DialogCopyFileError extends P2DialogExtra {

    private final Button btnYes = new Button("Ja");
    private final Button btnNo = new Button("Nein");
    private final CheckBox chkAlways;
    private final BackupInfo backupInfo;
    private final String file;
    private final BooleanProperty yesProp;
    private final boolean isFile;

    public DialogCopyFileError(BackupInfo backupInfo, String file, BooleanProperty yesProp,
                               boolean isFile) {
        super(ProgData.getInstance().primaryStage, null, "Datei kopieren",
                true, false, false, DECO.NO_BORDER);

        this.backupInfo = backupInfo;
        this.file = file;
        this.yesProp = yesProp;
        this.isFile = isFile;
        if (isFile) {
            chkAlways = new CheckBox("Auch jede weitere Datei?");
        } else {
            chkAlways = new CheckBox("Auch jeder weitere Pfad?");
        }
        init(true);
    }

    @Override
    public void make() {
        Button btnHelp;
        if (isFile) {
            btnHelp = P2IconFactory.getHelpButton(getStage(), "Datei Lesen",
                    "Auf die Datei\n\n" +
                            file + "\n\n kann nicht zugegriffen werden. Soll alles abgebrochen " +
                            "werden oder soll die Datei übersprungen werden?" +
                            "\n\n" +
                            "Es kann auch ausgewählt " +
                            "ob nur diese eine Datei übersprungen wird oder auch alle " +
                            "noch folgenden Dateien.");
        } else {
            btnHelp = P2IconFactory.getHelpButton(getStage(), "Pfad lesen",
                    "Der Pfad\n\n" +
                            file + "\n\n kann nicht gelesen werden. Es kann das " +
                            "abgebrochen werden oder der Pfad wird übersprungen." +
                            "\n\n" +
                            "Es kann auch ausgewählt werden, " +
                            "ob nur dieser Pfad übersprungen wird oder auch alle " +
                            "noch folgenden Pfade.");
        }
        Text text;
        if (isFile) {
            text = P2Text.getTextBold("Datei kann nicht gelesen werden:");
        } else {
            text = P2Text.getTextBold("Pfad kann nicht gelesen werden:");
        }
        VBox vBoxFile = new VBox(5);
        vBoxFile.setAlignment(Pos.CENTER_LEFT);
        vBoxFile.getChildren().addAll(text, new Label(file));
        Node errorNode = PIconFactory.getAttentionIcon("gmi-error-outline");

        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(errorNode, vBoxFile);

        HBox hBoxBtn = new HBox(P2LibConst.SPACING_HBOX);
        hBoxBtn.setAlignment(Pos.CENTER_RIGHT);
        hBoxBtn.getChildren().addAll(chkAlways, P2GuiTools.getHBoxGrower(),
                btnHelp, btnNo, btnYes);
        Label lblName;
        if (isFile) {
            lblName = P2Text.getLblTextBold("Soll die Datei übersprungen werden?");
        } else {
            lblName = P2Text.getLblTextBold("Soll der Pfad übersprungen werden?");
        }

        getVBoxCont().getChildren().addAll(hBox, P2GuiTools.getVDistance(50), lblName, hBoxBtn);

        btnYes.setOnAction(a -> yes());
        btnNo.setOnAction(a -> close());
        yesProp.set(false);
    }

    private void yes() {
        yesProp.set(true);
        backupInfo.runnerDto.goAlwaysOverErrorProperty().set(chkAlways.isSelected());
        close();
    }
}
