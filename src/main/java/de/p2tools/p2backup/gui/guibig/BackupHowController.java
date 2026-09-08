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
import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.gui.dialog.DialogHowHelp;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.guitools.P2Button;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class BackupHowController extends VBox {

    private final ProgData progData;
    private final Spinner<Integer> spinnerCount = new Spinner<>(1, 25, 1);
    private final RadioButton rbAll = new RadioButton("Alle Dateien kopieren");
    private final RadioButton rbDiff = new RadioButton("Nur geänderte Dateien kopieren");
    private final RadioButton rbIntelligent = new RadioButton("Intelligent");
    private final VBox vBoxContent = new VBox();

    public BackupHowController() {
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
                vBoxContent.disableProperty().bind(progData.backupInfoProperty.get().runnerDto.guiRunningProperty());
            }
        });
        spinnerCount.valueProperty().addListener((u, o, n) -> {
            if (progData.backupInfoProperty.get() != null) {
                progData.backupInfoProperty.get().setSumDay(spinnerCount.getValue());
            }
        });

        ToggleGroup tg = new ToggleGroup();

        rbAll.setToggleGroup(tg);
        rbDiff.setToggleGroup(tg);
        rbIntelligent.setToggleGroup(tg);

        rbAll.disableProperty().bind(progData.backupInfoProperty.isNull());
        rbDiff.disableProperty().bind(progData.backupInfoProperty.isNull());
        rbIntelligent.disableProperty().bind(progData.backupInfoProperty.isNull());
        rbIntelligent.visibleProperty().bind(ProgConfig.SYSTEM_ENHANCED);
        rbIntelligent.managedProperty().bind(ProgConfig.SYSTEM_ENHANCED);

        tg.selectedToggleProperty().addListener((u, o, n) -> {
            if (rbAll.isSelected()) {
                progData.backupInfoProperty.get().setHow(ProgConst.BACKUP_ALL);
            } else if (rbDiff.isSelected()) {
                progData.backupInfoProperty.get().setHow(ProgConst.BACKUP_DIFF);

            } else {
                progData.backupInfoProperty.get().setHow(ProgConst.BACKUP_INTELLIGENT);
            }
        });

        setInfosProp();
        progData.backupInfoProperty.addListener((u, o, n) -> {
            spinnerCount.setEditable(progData.backupInfoProperty.get() != null);
            setInfosProp();
        });

        GridPane gridPaneHow = new GridPane();
        gridPaneHow.setVgap(10);
        gridPaneHow.setHgap(10);

        int row = 0;
        gridPaneHow.add(rbAll, 0, row);
        gridPaneHow.add(rbDiff, 0, ++row);
        gridPaneHow.add(rbIntelligent, 0, ++row);

        final Button btnHowHelp = new Button();
        btnHowHelp.setGraphic(P2IconFactory.P2ICON.P2_BTN_HELP.getFontIcon());
        btnHowHelp.setOnAction(a -> new DialogHowHelp());

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getChildren().addAll(gridPaneHow, btnHowHelp);
        hBox.setAlignment(Pos.BOTTOM_RIGHT);
        HBox.setHgrow(gridPaneHow, Priority.ALWAYS);

        vBoxContent.getChildren().addAll(BackupGuiFactory.getInfoPane("Wie wird gesichert?"),
                hBox);

        Button btnHelp = P2Button.helpButton("Anzahl der Backups",
                "Hier kann man vorgeben, wie viele Backups vorgehalten werden sollen." +
                        "\n\n" +
                        "Sind mehr als die Vorgabe vorhanden, wird das " +
                        "älteste gelöscht.");
        row = 0;
        GridPane gridPaneCount = new GridPane();
        gridPaneCount.setVgap(P2LibConst.DIST_GRIDPANE_VGAP);
        gridPaneCount.setHgap(P2LibConst.DIST_GRIDPANE_HGAP);
        gridPaneCount.getColumnConstraints().addAll(P2GridConstraints.getCcPrefSize(),
                P2GridConstraints.getCcPrefSize(),
                P2GridConstraints.getCcComputedSizeAndHgrow());

        gridPaneCount.add(new Label("Anzahl:"), 0, row);
        gridPaneCount.add(spinnerCount, 1, row);
        gridPaneCount.add(btnHelp, 2, row);
        GridPane.setHalignment(btnHelp, HPos.RIGHT);

        vBoxContent.getChildren().addAll(P2GuiTools.getDistance(25),
                BackupGuiFactory.getInfoPane("Wie viele Backups sollen gespeichert werden?"),
                gridPaneCount);
    }

    private void setInfosProp() {
        setSpinner();
        BackupInfo backupInfo = progData.backupInfoProperty.get();
        if (backupInfo != null) {
            final int how = backupInfo.getHow();
            switch (how) {
                case ProgConst.BACKUP_ALL -> rbAll.setSelected(true);
                case ProgConst.BACKUP_DIFF -> rbDiff.setSelected(true);
                case ProgConst.BACKUP_INTELLIGENT -> rbIntelligent.setSelected(true);
                default -> {
                    rbAll.setSelected(true);
                    backupInfo.setHow(ProgConst.BACKUP_ALL);
                }
            }
        }
    }

    private void setSpinner() {
        spinnerCount.setEditable(progData.backupInfoProperty.get() != null);
        if (progData.backupInfoProperty.get() != null) {
            spinnerCount.getValueFactory().setValue(progData.backupInfoProperty.get().getSumDay());
        }
    }
}
