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


import de.p2tools.p2backup.P2BackupMenu;
import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.guitools.P2Button;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.*;

public class BackupBigGui extends VBox {

    private final VBox vBoxTop = new VBox(P2LibConst.SPACING_VBOX);
    private final HBox hBoxButton = new HBox(P2LibConst.SPACING_HBOX);

    private final Button btnBackup = new Button("Backups");
    private final Button btnInfos = new Button("Infos");
    private final Button btnFrom = new Button("Was");
    private final Button btnTo = new Button("Wohin");
    private final Button btnHow = new Button("Wie");
    private final Button btnTool = new Button("Tools");
    private final Button btnSize = new Button("Backups");
    private final GridPane gridPane = new GridPane();


    private final BackupListController backupGenerateController;
    private final BackupInfoController backupInfoController;
    private final BackupFromController backupFromController;
    private final BackupToController backupToController;
    private final BackupHowController backupHowController;
    private final BackupToolController backupToolController;

    private final Button btnBack = new Button("");
    private final Button btnForward = new Button("");

    private final ProgData progData;

    public BackupBigGui() {
        progData = ProgData.getInstance();
        backupGenerateController = new BackupListController();
        backupInfoController = new BackupInfoController();
        backupFromController = new BackupFromController();
        backupToController = new BackupToController();
        backupHowController = new BackupHowController();
        backupToolController = new BackupToolController();

        pack();
    }

    public void setShowing() {
        double size = btnSize.getWidth();
        btnBackup.setMinWidth(size);
        btnInfos.setMinWidth(size);
        btnFrom.setMinWidth(size);
        btnTo.setMinWidth(size);
        btnHow.setMinWidth(size);
        btnTool.setMinWidth(size);

        gridPane.getColumnConstraints().setAll(
                P2GridConstraints.getCcMinSize((int) size),
                P2GridConstraints.getCcMinSize((int) size),
                P2GridConstraints.getCcMinSize((int) size),
                P2GridConstraints.getCcMinSize((int) size),
                P2GridConstraints.getCcMinSize((int) size),
                P2GridConstraints.getCcMinSize((int) size));
    }

    private void pack() {
        setPadding(new Insets(P2LibConst.PADDING_VBOX));
        setSpacing(P2LibConst.SPACING_VBOX);

        // Gui
        backupGenerateController.setStyle("-fx-border-color: gray; -fx-border-width: 2px;");
        backupInfoController.setStyle("-fx-border-color: gray; -fx-border-width: 2px;");
        backupFromController.setStyle("-fx-border-color: gray; -fx-border-width: 2px;");
        backupToController.setStyle("-fx-border-color: gray; -fx-border-width: 2px;");
        backupHowController.setStyle("-fx-border-color: gray; -fx-border-width: 2px;");
        backupToolController.setStyle("-fx-border-color: gray; -fx-border-width: 2px;");
        VBox.setVgrow(backupGenerateController, Priority.ALWAYS);
        VBox.setVgrow(backupInfoController, Priority.ALWAYS);
        VBox.setVgrow(backupFromController, Priority.ALWAYS);
        VBox.setVgrow(backupToController, Priority.ALWAYS);
        VBox.setVgrow(backupHowController, Priority.ALWAYS);
        VBox.setVgrow(backupToolController, Priority.ALWAYS);

        btnBack.getStyleClass().add("btnChange");
        btnBack.setGraphic(PIconFactory.PICON.TOOLBAR_BTN_BACKWARD.getFontIcon());

        btnForward.getStyleClass().add("btnChange");
        btnForward.setGraphic(PIconFactory.PICON.TOOLBAR_BTN_FORWARD.getFontIcon());

        btnBackup.getStyleClass().addAll("pFuncBtn", "pFuncBtnTitleBar");
        btnInfos.getStyleClass().addAll("pFuncBtn", "pFuncBtnTitleBar");
        btnFrom.getStyleClass().addAll("pFuncBtn", "pFuncBtnTitleBar");
        btnTo.getStyleClass().addAll("pFuncBtn", "pFuncBtnTitleBar");
        btnHow.getStyleClass().addAll("pFuncBtn", "pFuncBtnTitleBar");
        btnTool.getStyleClass().addAll("pFuncBtn", "pFuncBtnTitleBar");

        getGrid();
        setState();
        initListener();

        // Statusbar
        StatusBarController statusBarController;
        statusBarController = new StatusBarController(progData);
        statusBarController.setStyle("-fx-border-color: gray; -fx-border-width: 3px;");
        getChildren().add(statusBarController);
    }

    private void getGrid() {
        // Top
        gridPane.setHgap(4);
        gridPane.add(btnBackup, 0, 0);
        gridPane.add(btnInfos, 1, 0);
        gridPane.add(btnFrom, 2, 0);
        gridPane.add(btnTo, 3, 0);
        gridPane.add(btnHow, 4, 0);
        gridPane.add(btnTool, 5, 0);

        btnSize.getStyleClass().add("btnSize");
        btnSize.setVisible(false);

        GridPane.setHalignment(btnBackup, HPos.CENTER);
        GridPane.setHalignment(btnInfos, HPos.CENTER);
        GridPane.setHalignment(btnFrom, HPos.CENTER);
        GridPane.setHalignment(btnTo, HPos.CENTER);
        GridPane.setHalignment(btnHow, HPos.CENTER);
        GridPane.setHalignment(btnTool, HPos.CENTER);

        HBox hBoxGrid = new HBox();
        hBoxGrid.getChildren().addAll(btnBack, P2GuiTools.getVDistance(10),
                gridPane, P2GuiTools.getVDistance(10), btnForward);
        hBoxGrid.setAlignment(Pos.CENTER);
        HBox.setHgrow(hBoxGrid, Priority.ALWAYS);

        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        stackPane.getChildren().addAll(btnSize, hBoxGrid);
        HBox.setHgrow(stackPane, Priority.ALWAYS);

        MenuButton mb = new P2BackupMenu();
        vBoxTop.setStyle("-fx-border-color: gray; -fx-border-width: 2px;");
        hBoxButton.setAlignment(Pos.CENTER);
        hBoxButton.setPadding(new Insets(P2LibConst.PADDING_HBOX));
        hBoxButton.getChildren().addAll(getBtnSmall(), stackPane, mb);

        vBoxTop.getChildren().addAll(hBoxButton);
        getChildren().addAll(vBoxTop);
    }

    private Button getBtnSmall() {
        final Button btnSmall = P2Button.getButton(PIconFactory.PICON.SMALL_ICON_BIG.getFontIcon(), "Kleine Ansicht");
        btnSmall.getStyleClass().add("changeGuiBtn");
        btnSmall.setOnAction(a -> BackupGuiFactory.changeGui());
        return btnSmall;
    }

    private void setState() {
        switch (progData.programState.get()) {
            case ProgConst.PROGRAM_STATE_BACKUP -> {
                this.getChildren().clear();
                this.getChildren().addAll(vBoxTop, backupGenerateController);
                setButton(btnBackup);
            }
            case ProgConst.PROGRAM_STATE_INFO -> {
                this.getChildren().clear();
                this.getChildren().addAll(vBoxTop, backupInfoController);
                setButton(btnInfos);
            }
            case ProgConst.PROGRAM_STATE_FROM -> {
                this.getChildren().clear();
                this.getChildren().addAll(vBoxTop, backupFromController);
                setButton(btnFrom);
            }
            case ProgConst.PROGRAM_STATE_TO -> {
                this.getChildren().clear();
                this.getChildren().addAll(vBoxTop, backupToController);
                setButton(btnTo);
            }
            case ProgConst.PROGRAM_STATE_HOW -> {
                this.getChildren().clear();
                this.getChildren().addAll(vBoxTop, backupHowController);
                setButton(btnHow);
            }
            case ProgConst.PROGRAM_STATE_TOOL -> {
                this.getChildren().clear();
                this.getChildren().addAll(vBoxTop, backupToolController);
                setButton(btnTool);
            }
        }
    }

    private void setButton(Button btn) {
        btnBackup.getStyleClass().remove("pFuncBtnTitleBarSel");
        btnInfos.getStyleClass().remove("pFuncBtnTitleBarSel");
        btnFrom.getStyleClass().remove("pFuncBtnTitleBarSel");
        btnTo.getStyleClass().remove("pFuncBtnTitleBarSel");
        btnHow.getStyleClass().remove("pFuncBtnTitleBarSel");
        btnTool.getStyleClass().remove("pFuncBtnTitleBarSel");
        btn.getStyleClass().add("pFuncBtnTitleBarSel");
    }

    private void initListener() {
        btnBackup.setOnAction(a -> progData.programState.set(ProgConst.PROGRAM_STATE_BACKUP));
        btnInfos.setOnAction(a -> progData.programState.set(ProgConst.PROGRAM_STATE_INFO));
        btnFrom.setOnAction(a -> progData.programState.set(ProgConst.PROGRAM_STATE_FROM));
        btnTo.setOnAction(a -> progData.programState.set(ProgConst.PROGRAM_STATE_TO));
        btnHow.setOnAction(a -> progData.programState.set(ProgConst.PROGRAM_STATE_HOW));
        btnTool.setOnAction(a -> progData.programState.set(ProgConst.PROGRAM_STATE_TOOL));

        btnBack.setOnAction(a -> {
            if (progData.programState.get() > ProgConst.PROGRAM_STATE_MIN) {
                progData.programState.set(progData.programState.get() - 1);
            }
        });
        btnBack.visibleProperty().bind(progData.programState.isEqualTo(ProgConst.PROGRAM_STATE_MIN).not()
                .and(progData.backupInfoProperty.isNull().not()));

        btnForward.setOnAction(a -> {
            if (progData.programState.get() < ProgConst.PROGRAM_STATE_MAX) {
                progData.programState.set(progData.programState.get() + 1);
            }
        });
        btnForward.visibleProperty().bind(progData.programState.isEqualTo(ProgConst.PROGRAM_STATE_MAX).not()
                .and(progData.backupInfoProperty.isNull().not()));

//        hBoxButton.visibleProperty().bind(progData.backupInfoProperty.isNull().not());
        btnInfos.disableProperty().bind(progData.backupInfoProperty.isNull());
        btnFrom.disableProperty().bind(progData.backupInfoProperty.isNull());
        btnTo.disableProperty().bind(progData.backupInfoProperty.isNull());
        btnHow.disableProperty().bind(progData.backupInfoProperty.isNull());
        btnTool.disableProperty().bind(progData.backupInfoProperty.isNull());

        progData.backupInfoProperty.addListener((u, o, n) -> {
            setState();
        });
        progData.programState.addListener((u, o, n) -> {
            setState();
        });
    }
}
