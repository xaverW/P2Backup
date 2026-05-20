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

import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.gui.dialog.HowHelpDialog;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.guitools.P2GuiTools;
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
            vBoxContent.setDisable(progData.backupInfoProperty.get() == null);
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

        final Button btnHowHelp = new Button("Wie jetzt?");
        btnHowHelp.setOnAction(a -> new HowHelpDialog());

        HBox hBox = new HBox(P2LibConst.SPACING_HBOX);
        hBox.getChildren().addAll(gridPaneHow, btnHowHelp);
        hBox.setAlignment(Pos.BOTTOM_RIGHT);
        HBox.setHgrow(gridPaneHow, Priority.ALWAYS);

//        VBox vBoxHow = new VBox(10);
        vBoxContent.getChildren().addAll(BackupGuiFactory.getInfoPane("Wie wird gesichert?"),
                hBox);

        row = 0;
        GridPane gridPaneCount = new GridPane();
        gridPaneCount.setVgap(P2LibConst.DIST_GRIDPANE_VGAP);
        gridPaneCount.setHgap(P2LibConst.DIST_GRIDPANE_HGAP);

        gridPaneCount.add(new Label("Anzahl:"), 0, row);
        gridPaneCount.add(spinnerCount, 1, row);

        vBoxContent.getChildren().addAll(P2GuiTools.getHDistance(10),
                BackupGuiFactory.getInfoPane("Wie viele Backups sollen gespeichert werden?"),
                gridPaneCount);

//        vBoxContent.getChildren().addAll( P2GuiTools.getVDistance(10), vBoxCount);
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
