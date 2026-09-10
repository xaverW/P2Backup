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


import de.p2tools.p2backup.controller.config.ProgConfig;
import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DialogHowHelp extends P2DialogExtra {

    private final ProgData progData;
    private final TabPane tabPane = new TabPane();
    private final String TEXT_ALL_HEADER = "Alle Dateien kopieren\n";
    private final String TEXT_ALL =
            "Damit werden bei jedem Backup alle meine " +
                    "Dateien in den Backup-Ordner kopiert.\n\n" +
                    "Das ist sehr sicher, braucht aber länger und vor " +
                    "allem sehr viel Platz.";

    private final String TEXT_ONLY_HEADER = "Nur geänderte Dateien kopieren";
    private final String TEXT_ONLY =
            "Damit werden bei jedem Backup nur die Dateien die sich seit " +
                    "dem letzten Backup geändert haben, in den Backup-Ordner kopiert." +
                    "\n\n" +
                    "Das ist schneller und braucht weniger Platz. Die Backupdateien " +
                    "sind dann auf mehrere Ordner verteilt. Zum Wiederherstellen der Daten müssen sie " +
                    "daraus zusammengesucht werden. Das kann aber automatisch mit dem Programm unter " +
                    "\"Tools\" gemacht werden.";

    private final String TEXT_INTELLIGENT_HEADER = "Intelligentes kopieren";
    private final String TEXT_INTELLIGENT =
            "Hier werden auch nur die geänderten Dateien in den Backup-Ordner " +
                    "kopiert. Die Dateien die sich nicht geändert haben, werden aus dem Backup-Ordner " +
                    "von gestern, nur verlinkt. D.h. die unveränderten Dateien sind nur einmal in " +
                    "den Backup-Ordnern. Sie werden aber in jedem Backup angezeigt." +
                    "\n\n" +
                    "Der Vorteil ist, es braucht dadurch sehr wenig Platz. Beim Wiederherstellen der eigenen " +
                    "Daten muss man auch nicht alle Backup-Ordner absuchen." +
                    "\n\n" +
                    "Es funktioniert aber nur auf Dateisystemen die diese Funktion unterstützen. Das sind " +
                    "z.B. bei Linux ext3/ext4 und bei Windows NTFS. USB-Sticks und externe Festplatten " +
                    "sind oft nur mit fat32 formatiert, die " +
                    "müssten dann mit NTFS neu formatiert werden.";

    public DialogHowHelp() {
        super(ProgData.getInstance().primaryStage, ProgConfig.DIALOG_HOW_HELP_SIZE, "Wie jetzt?",
                true, true, false);

        this.progData = ProgData.getInstance();
        init(true);
    }

    @Override
    public void make() {
        Button btnOk = new Button("OK");
        btnOk.setOnAction(a -> close());
        addOkButton(btnOk);
        addCopy("Alles kopieren", TEXT_ALL_HEADER, TEXT_ALL, ProgConst.HOW_HELP_IMAGE_ALL);
        addCopy("Geändertes kopieren", TEXT_ONLY_HEADER, TEXT_ONLY, ProgConst.HOW_HELP_IMAGE_ONLY);
        if (ProgConfig.SYSTEM_ENHANCED.get()) {
            addCopy("Intelligentes kopieren", TEXT_INTELLIGENT_HEADER, TEXT_INTELLIGENT, ProgConst.HOW_HELP_IMAGE_INTELLIGENT);
        }
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        getVBoxCont().getChildren().add(tabPane);
    }

    private void addCopy(String tabTitle, String header, String text, String jpg) {
        ScrollPane sc = new ScrollPane();
        sc.setFitToHeight(true);
        sc.setFitToWidth(true);

        Label lblHeader = new Label();
        lblHeader.setMaxWidth(Double.MAX_VALUE);
        lblHeader.getStyleClass().add("lblHeader");
        lblHeader.setText(header);
        HBox.setHgrow(lblHeader, Priority.ALWAYS);

        Label lbl = new Label();
        lbl.setWrapText(true);
        lbl.setText(text);

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        Image im = new Image(jpg, 500, 0, true, true);

        ImageView iv = new ImageView();
        iv.setImage(im);

        VBox vBox = new VBox(5);
        vBox.setPadding(new Insets(20, 10, 10, 10));
        vBox.getChildren().addAll(lblHeader, lbl);
        hBox.getChildren().addAll(iv, vBox);
        sc.setContent(hBox);

        Tab tab = new Tab(tabTitle);
        tab.setClosable(false);
        tab.setContent(sc);
        tabPane.getTabs().add(tab);
    }
}