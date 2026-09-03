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

import de.p2tools.p2backup.controller.LoadFactory;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.gui.dialog.DialogAddBackup;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.guitools.P2BigButton;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BackupListController extends VBox {

    private final ProgData progData;
    private final ScrollPane scrollPane = new ScrollPane();
    private final PaneGenerateBackupList paneGenerateBackupList;
    private final VBox vBoxContent = new VBox();

    public BackupListController() {
        progData = ProgData.getInstance();
        this.paneGenerateBackupList = new PaneGenerateBackupList();

        setPadding(new Insets(P2LibConst.PADDING_VBOX));
        setSpacing(P2LibConst.SPACING_VBOX);
        getChildren().add(vBoxContent);

        init();
    }

    private void init() {
        vBoxContent.getChildren().addAll(BackupGuiFactory.getInfoPane("Liste der angelegten Backups"));

        scrollPane.setContent(paneGenerateBackupList);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("edge-to-edge");

        vBoxContent.getChildren().addAll(scrollPane, P2GuiTools.getVBoxGrower());
        addBackupPane();
    }

    private void addBackupPane() {
        Button btnLoad = new Button("Gespeichertes Backup laden");
        btnLoad.setGraphic(PIconFactory.PICON.BTN_LOAD_BACKUP.getFontIcon());
        btnLoad.setMaxWidth(Double.MAX_VALUE);
        btnLoad.visibleProperty().bind(progData.backupInfoProperty.isNull().not());
        btnLoad.managedProperty().bind(progData.backupInfoProperty.isNull().not());
        btnLoad.setOnAction(a -> LoadFactory.loadBackupInfo(null));

        P2BigButton btnBigLoad = new P2BigButton(PIconFactory.PICON.BTN_LOAD_BACKUP_BIG_50.getFontIcon(),
                "Gespeichertes Backup laden", "");
        btnBigLoad.setAlignment(Pos.CENTER);
        btnBigLoad.setMaxHeight(Double.MAX_VALUE);
        btnBigLoad.visibleProperty().bind(progData.backupInfoProperty.isNull());
        btnBigLoad.managedProperty().bind(progData.backupInfoProperty.isNull());
        btnBigLoad.setOnAction(a -> LoadFactory.loadBackupInfo(null));


        Button btnAdd = new Button("Neues Backup anlegen");
        btnAdd.setGraphic(P2IconFactory.P2ICON.BTN_PLUS_OUTLINE.getFontIcon());
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.visibleProperty().bind(progData.backupInfoProperty.isNull().not());
        btnAdd.managedProperty().bind(progData.backupInfoProperty.isNull().not());
        btnAdd.setOnAction(a -> add());

        P2BigButton btnBigAdd = new P2BigButton(P2IconFactory.P2ICON.BTN_PLUS_OUTLINE_50.getFontIcon(),
                "Neues Backup anlegen", "");
        btnBigAdd.setAlignment(Pos.CENTER);
        btnBigAdd.setMaxHeight(Double.MAX_VALUE);
        btnBigAdd.visibleProperty().bind(progData.backupInfoProperty.isNull());
        btnBigAdd.managedProperty().bind(progData.backupInfoProperty.isNull());
        btnBigAdd.setOnAction(e -> add());

        HBox hBoxAdd = new HBox(P2LibConst.SPACING_HBOX);
        hBoxAdd.getChildren().addAll(btnLoad, btnAdd);
        HBox hBoxBig = new HBox(P2LibConst.SPACING_HBOX);
        hBoxBig.getChildren().addAll(btnBigLoad, btnBigAdd);


        VBox vBoxAdd = new VBox();
        vBoxAdd.getChildren().addAll(hBoxAdd, hBoxBig);
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.getChildren().addAll(vBoxAdd);
        getChildren().addAll(P2GuiTools.getVBoxGrower(), hBox);
    }

    private void add() {
        ObjectProperty<BackupInfo> backupDataProb = new SimpleObjectProperty<>(new BackupInfo());
        if (new DialogAddBackup(progData, backupDataProb).isOk()) {
            progData.backupInfoList.add(backupDataProb.get());
            progData.backupInfoProperty.set(backupDataProb.get());
        }
    }
}
