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

package de.p2tools.p2backup.gui.configdialog.configpanes;

import de.p2tools.p2backup.controller.config.ProgConfig;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.gui.help.HelpText;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
import de.p2tools.p2lib.guitools.ptoggleswitch.P2ToggleSwitch;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Collection;

public class PaneConfig {

    private final ProgData progData;
    private final P2ToggleSwitch tglOnlyOneInstance = new P2ToggleSwitch("Nur eine Instanz des Programms öffnen");

    private final Stage stage;

    public PaneConfig(Stage stage) {
        this.stage = stage;
        progData = ProgData.getInstance();
    }

    public void close() {
        tglOnlyOneInstance.selectedProperty().unbindBidirectional(ProgConfig.SYSTEM_ONLY_ONE_INSTANCE);
    }

    public TitledPane make(Collection<TitledPane> result) {
        tglOnlyOneInstance.selectedProperty().bindBidirectional(ProgConfig.SYSTEM_ONLY_ONE_INSTANCE);
        final Button btnHelpOnlyOneInstance = PIconFactory.getHelpButton(stage, "Nur eine Instanz des Programms öffnen",
                HelpText.ONLY_ONE_INSTANCE);
        GridPane.setHalignment(btnHelpOnlyOneInstance, HPos.RIGHT);

        final GridPane gridPane = new GridPane();
        gridPane.setHgap(P2LibConst.DIST_GRIDPANE_HGAP);
        gridPane.setVgap(P2LibConst.DIST_GRIDPANE_VGAP);
        gridPane.setPadding(new Insets(P2LibConst.PADDING));

        int row = 0;
        gridPane.add(tglOnlyOneInstance, 0, ++row, 2, 1);
        gridPane.add(btnHelpOnlyOneInstance, 2, row);
        gridPane.getColumnConstraints().addAll(P2GridConstraints.getCcPrefSize(),
                P2GridConstraints.getCcComputedSizeAndHgrow(), P2GridConstraints.getCcPrefSize());

        TitledPane tpConfig = new TitledPane("Allgemein", gridPane);
        result.add(tpConfig);
        return tpConfig;
    }
}
