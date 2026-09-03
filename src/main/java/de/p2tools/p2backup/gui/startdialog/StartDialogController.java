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

package de.p2tools.p2backup.gui.startdialog;

import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2Button;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;


public class StartDialogController extends P2DialogExtra {

    public static final int DIALOG_TEXT_WIDTH = 700;
    private static final String STR_START_1 = "Infos";
    private static final String STR_START_2 = "Infos";
    private static final String STR_COLOR_MODE = "Farbe";
    private static final String STR_BACKUP = "Backup";
    private static final String STR_UPDATE = "Update";

    private final ProgData progData;
    private boolean ok = false;

    private final TilePane tilePane = new TilePane();
    private Button btnOk, btnCancel;
    private Button btnPrev, btnNext;

    private final Button btnStart1 = new Button(STR_START_1);
    private final Button btnStart2 = new Button(STR_START_2);
    private final Button btnColorMode = new Button(STR_COLOR_MODE);
    private final Button btnUpdate = new Button(STR_UPDATE);
    private final Button btnBackup = new Button(STR_BACKUP);

    private State aktState = State.START_1;
    private TitledPane tStart1;
    private TitledPane tStart2;
    private TitledPane tColorMode;
    private TitledPane tUpdate;
    private TitledPane tBackup;

    private StartPane startPane1;
    private StartPane startPane2;
    private StartPaneColorMode startPaneColorMode;
    private StartPaneUpdate startPaneUpdate;
    private StartPaneBackup startPaneBackup;
    private final VBox vBoxCont = new VBox();
    private final ObjectProperty<BackupInfo> backupDataObjectProperty = new SimpleObjectProperty<>(new BackupInfo());

    private enum State {START_1, START_2, COLOR_MODE, UPDATE, BACKUP}


    public StartDialogController() {
        super(null, null, "Starteinstellungen");

        this.progData = ProgData.getInstance();
        init(true);
    }

    @Override
    public void make() {
        initTopButton();
        initStack();
        initButton();
        initTooltip();
        selectActPane();
    }

    private void closeDialog(boolean ok) {
        this.ok = ok;
        startPane1.close();
        startPane2.close();
        startPaneColorMode.close();
        startPaneUpdate.close();
        startPaneBackup.close();
        super.close();
    }

    public boolean isOk() {
        return ok;
    }

    private void initTopButton() {
        final TilePane tilePane1 = new TilePane();
        tilePane1.setAlignment(Pos.CENTER);
        tilePane1.setHgap(10);
        tilePane1.setVgap(10);
        tilePane1.getChildren().addAll(btnStart1, btnStart2, btnColorMode, btnUpdate, btnBackup);

        initTopButton(btnStart1, State.START_1);
        initTopButton(btnStart2, State.START_2);
        initTopButton(btnColorMode, State.COLOR_MODE);
        initTopButton(btnUpdate, State.UPDATE);
        initTopButton(btnBackup, State.BACKUP);
        VBox.setVgrow(vBoxCont, Priority.ALWAYS);
        getVBoxCont().setPadding(new Insets(5));
        getVBoxCont().getChildren().addAll(tilePane1, P2GuiTools.getHDistance(5), vBoxCont);
    }

    private void initTopButton(Button btn, State state) {
        btn.getStyleClass().addAll("btnStartDialog");
        btn.setAlignment(Pos.CENTER);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(a -> {
            aktState = state;
            selectActPane();
        });
    }

    private void initStack() {
        //startPane 1
        startPane1 = new StartPane(getStage());
        startPane1.makeStart1();

        //startPane 2
        startPane2 = new StartPane(getStage());
        startPane2.makeStart2();

        //colorModePane
        startPaneColorMode = new StartPaneColorMode(this.getStage());
        startPaneColorMode.make();

        //updatePane
        startPaneUpdate = new StartPaneUpdate(this);
        startPaneUpdate.makeStart();

        //updatePane
        startPaneBackup = new StartPaneBackup(this, backupDataObjectProperty);
        startPaneBackup.makeStart();
    }

    private void initButton() {
        btnOk = new Button("_Ok");
        btnOk.setDisable(true);
        btnOk.setOnAction(a -> {
            progData.backupInfoList.add(backupDataObjectProperty.get());
            closeDialog(true);
        });
        btnOk.disableProperty().bind(backupDataObjectProperty.getValue().nameProperty().isEmpty());

        btnCancel = new Button("_Abbrechen");
        btnCancel.setOnAction(a -> closeDialog(false));

        btnNext = P2Button.getButton(P2IconFactory.P2ICON.BTN_NEXT.getFontIcon(), "nächste Seite");
        btnNext.setOnAction(event -> {
            switch (aktState) {
                case START_1:
                    aktState = State.START_2;
                    break;
                case START_2:
                    aktState = State.COLOR_MODE;
                    break;
                case COLOR_MODE:
                    aktState = State.UPDATE;
                    break;
                case UPDATE:
                    aktState = State.BACKUP;
                    break;
                case BACKUP:
                    break;
            }
            selectActPane();
        });
        btnPrev = P2Button.getButton(P2IconFactory.P2ICON.BTN_PREV.getFontIcon(), "vorherige Seite");
        btnPrev.setOnAction(event -> {
            switch (aktState) {
                case START_1:
                    break;
                case START_2:
                    aktState = State.START_1;
                    break;
                case COLOR_MODE:
                    aktState = State.START_2;
                    break;
                case UPDATE:
                    aktState = State.COLOR_MODE;
                    break;
                case BACKUP:
                    aktState = State.UPDATE;
                    break;
            }
            selectActPane();
        });

        addOkCancelButtons(btnOk, btnCancel);
        ButtonBar.setButtonData(btnPrev, ButtonBar.ButtonData.BACK_PREVIOUS);
        ButtonBar.setButtonData(btnNext, ButtonBar.ButtonData.NEXT_FORWARD);
        addAnyButton(btnNext);
        addAnyButton(btnPrev);
        getButtonBar().setButtonOrder("BX+CO");
    }

    private void selectActPane() {
        switch (aktState) {
            case START_1:
                btnPrev.setDisable(true);
                btnNext.setDisable(false);
                vBoxCont.getChildren().clear();
                vBoxCont.getChildren().add(startPane1);
                setButtonStyle(btnStart1);
                break;
            case START_2:
                btnPrev.setDisable(false);
                btnNext.setDisable(false);
                vBoxCont.getChildren().clear();
                vBoxCont.getChildren().add(startPane2);
                setButtonStyle(btnStart2);
                break;
            case COLOR_MODE:
                btnPrev.setDisable(false);
                btnNext.setDisable(false);
                vBoxCont.getChildren().clear();
                vBoxCont.getChildren().add(startPaneColorMode);
                setButtonStyle(btnColorMode);
                break;
            case UPDATE:
                btnPrev.setDisable(false);
                btnNext.setDisable(false);
                vBoxCont.getChildren().clear();
                vBoxCont.getChildren().add(startPaneUpdate);
                setButtonStyle(btnUpdate);
                break;
            case BACKUP:
                btnPrev.setDisable(false);
                btnNext.setDisable(true);
                vBoxCont.getChildren().clear();
                vBoxCont.getChildren().add(startPaneBackup);
                setButtonStyle(btnBackup);
                break;

            default:
                btnOk.setDisable(false);
        }
    }

    private void setButtonStyle(Button btnSel) {
        btnStart1.getStyleClass().setAll("btnStartDialog");
        btnStart2.getStyleClass().setAll("btnStartDialog");
        btnColorMode.getStyleClass().setAll("btnStartDialog");
        btnUpdate.getStyleClass().setAll("btnStartDialog");
        btnBackup.getStyleClass().setAll("btnStartDialog");
        btnSel.getStyleClass().setAll("btnStartDialog", "btnStartDialogSel");
    }

    private void initTooltip() {
        btnStart1.setTooltip(new Tooltip("Infos über das Programm"));
        btnStart2.setTooltip(new Tooltip("Infos über das Programm"));
        btnColorMode.setTooltip(new Tooltip("Wie soll die Programmoberfläche aussehen?"));
        btnUpdate.setTooltip(new Tooltip("Soll das Programm nach Updates suchen?"));
        btnBackup.setTooltip(new Tooltip("Hier kann das erste Backup angelegt werden."));

        btnOk.setTooltip(new Tooltip("Programm mit den gewählten Einstellungen starten"));
        btnCancel.setTooltip(new Tooltip("Das Programm nicht einrichten\n" +
                "und starten sondern Dialog wieder beenden"));
        btnNext.setTooltip(new Tooltip("Nächste Einstellmöglichkeit"));
        btnPrev.setTooltip(new Tooltip("Vorherige Einstellmöglichkeit"));
    }
}
