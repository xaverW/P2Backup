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
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.alert.P2Alert;
import de.p2tools.p2lib.dialogs.P2DirFileChooser;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2ComboBoxString;
import de.p2tools.p2lib.guitools.P2GuiTools;
import de.p2tools.p2lib.guitools.P2Text;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import de.p2tools.p2lib.tools.file.P2FileName;
import de.p2tools.p2lib.tools.file.P2FileUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DialogCopyFileController extends P2DialogExtra {

    private final Button btnOk = new Button("Ok");
    private final Button btnCancel = new Button("Abbrechen");
    private final Button btnSearchDestPath = new Button();
    private final Button btnProposeFileName = new Button();
    private final String srcFile;
    private final P2ComboBoxString cboDest = new P2ComboBoxString();
    private final TextField txtName = new TextField();
    private final Node errorNode = PIconFactory.getAttentionIconSmall("mdoal-error");
    private final HBox hBoxErrorMsg = new HBox(P2LibConst.PADDING_HBOX);
    private final ProgData progData;
    private final boolean file;


    public DialogCopyFileController(Stage stage, String srcFile, boolean file) {
        super(stage, null, "Datei kopieren",
                true, false, false, DECO.NO_BORDER);
        this.progData = ProgData.getInstance();
        this.srcFile = srcFile;
        this.file = file;
        init(true);
    }

    @Override
    public void make() {
        cboDest.init(ProgConfig.CBO_COPY_DIALOG_DEST_DIR, ProgConfig.COPY_DIALOG_DEST_DIR);
        cboDest.setMaxWidth(Double.MAX_VALUE);
        btnSearchDestPath.setTooltip(new Tooltip("Verzeichnis auswählen"));
        btnSearchDestPath.setGraphic(P2IconFactory.P2ICON.BTN_OPEN_DIR.getFontIcon());
        btnSearchDestPath.setOnAction(a -> {
            P2DirFileChooser.DirChooser(getStage(), cboDest);
//            ProgConfig.COPY_DIALOG_DEST_DIR.set(
//                    P2DialogFileChooser.showFileChooser(getStage(), "Kopieren", "Ziel auswählen",
//                            "Einen Ordner zum Speichern auswählen", true, false, ""));
        });

        String name = Path.of(srcFile).toFile().getName();
        String suffix = name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : "";
        txtName.setText(name);

        btnProposeFileName.setGraphic(PIconFactory.PICON.BTN_RANDOM.getFontIcon());
        btnProposeFileName.setTooltip(new Tooltip("Einen Dateinamen vorschlagen"));
        btnProposeFileName.setOnAction(event -> {
            String fileName = txtName.textProperty().getValueSafe();

            if (fileName.isEmpty()) {
                fileName = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH:mm:ss").format(LocalDateTime.now()) +
                        name;
            }
            if (!fileName.endsWith("." + suffix)) {
                fileName = fileName + "." + suffix;
            }

            txtName.textProperty().setValue(P2FileName.getNextFileNameWithDateWithOutPath(fileName, suffix));
        });

        checkFileName();
        ProgConfig.COPY_DIALOG_DEST_DIR.addListener((u, o, n) -> checkFileName());
        txtName.textProperty().addListener((u, o, n) -> checkFileName());

        final Label lblTitle;
        if (file) {
            lblTitle = P2Text.getLblTextBoldBig("Datei kopieren");
        } else {
            lblTitle = P2Text.getLblTextBoldBig("Ordner kopieren");
        }

        TextArea taSrc = new TextArea(srcFile);
        taSrc.setWrapText(true);
        taSrc.setEditable(false);
        taSrc.setPrefRowCount(2);

        GridPane gridPane = new GridPane(P2LibConst.DIST_GRIDPANE_HGAP, P2LibConst.DIST_GRIDPANE_VGAP);
        gridPane.getColumnConstraints().addAll(P2GridConstraints.getCcPrefSize(),
                P2GridConstraints.getCcComputedSizeAndHgrow(),
                P2GridConstraints.getCcPrefSizeRight());

        int row = 0;
        gridPane.add(new Label("Name:"), 0, row);
        gridPane.add(taSrc, 1, row);

        gridPane.add(new Label("Ziel:"), 0, ++row);
        gridPane.add(cboDest, 1, row);
        gridPane.add(btnSearchDestPath, 2, row);

        if (file) {
            gridPane.add(new Label("Dateiname:"), 0, ++row);
            gridPane.add(txtName, 1, row);
            gridPane.add(btnProposeFileName, 2, row);
        }

        VBox vBox = new VBox(5);
        vBox.setPadding(new Insets(0, 20, 0, 0));
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.getChildren().addAll(lblTitle, P2GuiTools.getHDistance(20), gridPane);

        hBoxErrorMsg.setAlignment(Pos.CENTER_LEFT);
        hBoxErrorMsg.getChildren().addAll(errorNode, new Label("Datei existiert bereits!"));
        getVBoxCont().getChildren().addAll(vBox);
        if (file) {
            getHboxLeft().getChildren().add(hBoxErrorMsg);
            getHboxLeft().setAlignment(Pos.CENTER_LEFT);
        }

        addOkCancelButtons(btnOk, btnCancel);
        if (file) {
            btnOk.disableProperty().bind(ProgConfig.COPY_DIALOG_DEST_DIR.isEmpty().or(txtName.textProperty().isEmpty()));
            btnOk.setOnAction(a -> {
                if (copyFile()) {
                    close();
                }
            });
        } else {
            btnOk.disableProperty().bind(ProgConfig.COPY_DIALOG_DEST_DIR.isEmpty());
            btnOk.setOnAction(a -> {
                if (copyDir()) {
                    close();
                } else {
                    P2Alert.showErrorAlert(getStage(), "Kopieren",
                            "Das Kopieren hat nicht korrekt geklappt");
                }
            });
        }
        btnCancel.setOnAction(a -> close());
    }

    private void checkFileName() {
        Path destFile = Path.of(ProgConfig.COPY_DIALOG_DEST_DIR.getValueSafe(), txtName.getText());
        hBoxErrorMsg.setVisible(destFile.toFile().exists());
    }

    private boolean copyFile() {
        return P2FileUtils.copyFileToDir(getStage(), Path.of(srcFile), Path.of(ProgConfig.COPY_DIALOG_DEST_DIR.getValueSafe()),
                txtName.getText(), true);
    }

    private boolean copyDir() {
        new Thread(() -> {
            P2FileUtils.copyPath(getStage(), srcFile, ProgConfig.COPY_DIALOG_DEST_DIR.getValueSafe());
        }).start();
        return true;
    }
}
