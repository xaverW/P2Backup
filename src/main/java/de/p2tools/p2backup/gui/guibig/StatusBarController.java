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

package de.p2tools.p2backup.gui.guibig;

import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.RunnerDto;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.p2event.P2Listener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class StatusBarController extends AnchorPane {

    private final HBox hBox;
    private final ProgData progData;

    public StatusBarController(ProgData progData) {
        this.progData = progData;

        hBox = makeHbox();
        getChildren().addAll(hBox);
        AnchorPane.setLeftAnchor(hBox, 0.0);
        AnchorPane.setBottomAnchor(hBox, 0.0);
        AnchorPane.setRightAnchor(hBox, 0.0);
        AnchorPane.setTopAnchor(hBox, 0.0);
        make();
    }

    private HBox makeHbox() {
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(2, 5, 2, 5));
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setStyle("-fx-background-color: -fx-background;");
        return hBox;
    }

    private void addRunner() {
        hBox.getChildren().clear();
        progData.backupInfoList.forEach(b -> {
            RunnerDto runnerDto = b.runnerDto;
            if (runnerDto.isRunning()) {
                Label lblRunning = new Label(b.getName());
                hBox.getChildren().add(lblRunning);
            }
        });
    }

    private void make() {
        progData.pEventHandler.addListener(new P2Listener(PEvents.EVENT_TIMER_HALF_SECOND) {
            @Override
            public void pingGui(P2Event event) {
                // können hunderte Dateien pro Sekunde sein!!
                addRunner();
            }
        });
    }
}
