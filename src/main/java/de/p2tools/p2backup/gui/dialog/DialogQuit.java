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


import de.p2tools.p2backup.controller.ProgQuit;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfoFactory;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2BigButton;
import de.p2tools.p2lib.guitools.pmask.P2MaskerPane;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import de.p2tools.p2lib.tools.P2Wait;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;

public class DialogQuit extends P2DialogExtra {

    private final StackPane stackPane = new StackPane();
    private final P2MaskerPane maskerPane = new P2MaskerPane();
    private final WaitTask waitTask = new WaitTask();
    private final ProgressBar progressBar = new ProgressBar();

    public DialogQuit() {
        super(ProgData.getInstance().primaryStage, null, "Programm beenden");
        ProgData.getInstance().dialogQuit = this;
        init(true);
    }

    @Override
    public void make() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(15);
        gridPane.setVgap(25);

        maskerPane.switchOffMasker();
        maskerPane.setButtonText("Abbrechen");
        maskerPane.getButton().setOnAction(a -> close());

        Label headerLabel = new Label("Es laufen noch Backups!");
        headerLabel.setStyle("-fx-font-size: 1.8em; -fx-font-weight: bold;");

        // nicht beenden
        P2BigButton cancelButton = new P2BigButton(P2IconFactory.P2ICON.BTN_DIALOG_RESET_CONFIG.getFontIcon(),
                "Nicht beenden", "");
        cancelButton.setOnAction(e -> {
            close();
        });

        // beenden
        P2BigButton quitButton = new P2BigButton(P2IconFactory.P2ICON.BTN_DIALOG_RESET_CONFIG.getFontIcon(),
                "Beenden", "Alle Backups abbrechen und das Programm beenden.");
        quitButton.setOnAction(e -> {
            DoubleProperty property = new SimpleDoubleProperty(0);
            progressBar.setVisible(true);
            progressBar.progressProperty().bind(property);

            BackupInfoFactory.stopAllAndQuitt(property);
        });

        // warten, dann beenden
        P2BigButton waitButton = new P2BigButton(P2IconFactory.P2ICON.BTN_DIALOG_RESET_CONFIG.getFontIcon(),
                "Warten", "Alle Backups abwarten und dann das Programm beenden.");
        waitButton.setOnAction(e -> startWaiting());
        waitTask.setOnSucceeded(event -> ProgQuit.quitNow());

        Node fx = P2IconFactory.P2ICON.ATTENTION_80.getFontIcon();
        gridPane.add(fx, 0, 0, 1, 4);
        gridPane.add(headerLabel, 0, 0, 2, 1);
        gridPane.add(cancelButton, 1, 1);
        gridPane.add(waitButton, 1, 2);
        gridPane.add(quitButton, 1, 3);

        gridPane.add(progressBar, 0, 4, 2, 1);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);

        GridPane.setValignment(fx, VPos.CENTER);
        GridPane.setHalignment(headerLabel, HPos.CENTER);

        ColumnConstraints ccTxt = new ColumnConstraints();
        ccTxt.setFillWidth(true);
        ccTxt.setMinWidth(Region.USE_COMPUTED_SIZE);
        ccTxt.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().addAll(new ColumnConstraints(), ccTxt);

        stackPane.getChildren().addAll(gridPane, maskerPane);
        getVBoxCont().getChildren().addAll(stackPane);
    }

    public void startWaiting() {
        maskerPane.setMaskerVisible(true, false, true);
        Thread th = new Thread(waitTask);
        th.setName("startWaiting");
        th.start();
    }

    @Override
    public void close() {
        ProgData.getInstance().dialogQuit = null;
        if (waitTask.isRunning()) {
            waitTask.cancel();
        }
        maskerPane.switchOffMasker();
        super.close();
    }

    private static class WaitTask extends Task<Void> {
        @Override
        protected Void call() {
            while (BackupInfoFactory.isRunning() && !isCancelled()) {
                P2Wait.pause(500);
            }
            return null;
        }
    }
}