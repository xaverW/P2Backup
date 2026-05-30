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

import de.p2tools.p2backup.controller.config.ProgConfig;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.dialogs.P2DirFileChooser;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
import de.p2tools.p2lib.guitools.pcbo.P2CboButtonListString;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class BackupToController extends VBox {

    private final ProgData progData;

    private BackupInfo backupInfo = null;
    private final StringProperty filter = new SimpleStringProperty();
    private final P2CboButtonListString cboPath;
    private final VBox vBoxContent = new VBox();

    public BackupToController() {
        progData = ProgData.getInstance();

        setPadding(new Insets(P2LibConst.PADDING_VBOX));
        setSpacing(P2LibConst.SPACING_VBOX);
        getChildren().addAll(new TitleBox());
        getChildren().add(vBoxContent);

        cboPath = new P2CboButtonListString(progData.usedToPathList.getPathList(), filter);
        init();
        initList();
    }

    private void init() {
        progData.backupInfoProperty.addListener((u, o, n) -> {
            vBoxContent.disableProperty().unbind();
            vBoxContent.setDisable(progData.backupInfoProperty.get() == null);
            setBackup();
            if (progData.backupInfoProperty.get() != null) {
                vBoxContent.disableProperty().bind(progData.backupInfoProperty.get().runnerDto.guiRunningProperty());
            }
        });
        ProgConfig.SYSTEM_ENHANCED.addListener((u, o, n) -> {
            setBackup();
        });

        vBoxContent.getChildren().addAll(BackupGuiFactory.getInfoPane("Wohin soll gesichert werden"));
        setBackup();
    }

    private void initList() {
        Button btnTo = new Button();
        btnTo.setGraphic(PIconFactory.PICON.BTN_DIR_OPEN.getFontIcon());
        btnTo.setTooltip(new Tooltip("Den Ordner für das Backup auswählen"));
        btnTo.setOnAction(event -> {
            String start;
            if (filter.get().isEmpty()) {
                start = ProgConfig.SYSTEM_TO_PATH.get();
            } else {
                start = filter.get();
            }
            String path = P2DirFileChooser.DirChooser(ProgData.getInstance().primaryStage, start);
            if (!path.isEmpty()) {
                ProgConfig.SYSTEM_TO_PATH.set(path);
                filter.set(path);
            }
        });

        Button btnHlpTo = PIconFactory.getHelpButton("Ordner zum Speichern des Backups",
                "Hier muss der Ordner angegeben werden, in dem das " +
                        "Backup gespeichert werden soll.");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(P2LibConst.DIST_GRIDPANE_HGAP);
        gridPane.setVgap(P2LibConst.DIST_GRIDPANE_VGAP);

        int row = 0;
        gridPane.add(new Label("Ordner zum Sichern auswählen:"), 0, row);
        gridPane.add(cboPath, 0, ++row);
        gridPane.add(btnTo, 1, row);
        gridPane.add(btnHlpTo, 2, row);
        GridPane.setHgrow(cboPath, Priority.ALWAYS);
        cboPath.setMaxWidth(Double.MAX_VALUE);

        gridPane.getColumnConstraints().addAll(P2GridConstraints.getCcComputedSizeAndHgrow(),
                P2GridConstraints.getCcPrefSize(),
                P2GridConstraints.getCcPrefSize());

        vBoxContent.getChildren().add(gridPane);
    }

    private void setBackup() {
        if (backupInfo != null) {
            filter.unbindBidirectional(backupInfo.backupPathProperty());
            backupInfo = null;
        }
        if (progData.backupInfoProperty.get() != null) {
            backupInfo = progData.backupInfoProperty.get();

            filter.bindBidirectional(backupInfo.backupPathProperty());
        }
    }
}
