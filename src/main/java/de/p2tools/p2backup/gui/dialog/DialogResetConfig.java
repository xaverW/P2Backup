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
import de.p2tools.p2backup.controller.picon.PIconFactory;
import de.p2tools.p2backup.gui.help.HelpText;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.alert.P2Alert;
import de.p2tools.p2lib.dialogs.dialog.P2DialogExtra;
import de.p2tools.p2lib.guitools.P2BigButton;
import de.p2tools.p2lib.guitools.grid.P2GridConstraints;
import de.p2tools.p2lib.ikonli.P2IconFactory;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class DialogResetConfig extends P2DialogExtra {

    final ProgData progData;
    final StackPane stackPane;

    public DialogResetConfig(ProgData progData) {
        super(progData.primaryStage, null, "Programm zurücksetzen");

        this.progData = progData;
        stackPane = new StackPane();

        init(true);
    }

    @Override
    public void make() {

        Label headerLabel = new Label("Einstellungen können komplett" + P2LibConst.LINE_SEPARATOR +
                "zurückgesetzt werden!");
        headerLabel.setStyle("-fx-font-size: 1.5em;");

        // Set zurücksetzen
        P2BigButton cancelButton = new P2BigButton(P2IconFactory.P2ICON.P2_DIALOG_RESET.getFontIcon(),
                "Nichts ändern", "");
        cancelButton.setOnAction(e -> close());

        final Button btnHelp = PIconFactory.getHelpButton(this.getStage(), "Programm zurücksetzen",
                HelpText.RESET_DIALOG);

        // alle Einstellungen
        P2BigButton allButton = new P2BigButton(P2IconFactory.P2ICON.P2_DIALOG_RESET.getFontIcon(), "" +
                "Alle Einstellungen zurücksetzen!",
                "Alle Einstellungen gehen verloren.");
        allButton.setOnAction(e -> {
            Text t = new Text("ALLE");
            t.setFont(Font.font(null, FontWeight.BOLD, -1));

            TextFlow tf = new TextFlow();
            tf.getChildren().addAll(new Text("Es werden "), t,
                    new Text(" von Ihnen erzeugten Änderungen gelöscht." + P2LibConst.LINE_SEPARATORx2 +
                            "Möchten Sie wirklich alle Einstellungen zurücksetzen?"));

            if (P2Alert.showAlert_yes_no_cancel("Einstellungen zurücksetzen",
                    "alle Einstellungen zurücksetzen!", tf, false) == P2Alert.BUTTON.YES) {
                // damit wird vor dem Beenden das Konfig-Verzeichnis umbenannt und so startet das
                // Programm wie beim ersten Start
                ProgData.reset = true;
                ProgQuit.quit();
            }
        });

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(0, 0, 20, 0));
        gridPane.setHgap(15);
        gridPane.setVgap(25);
        gridPane.add(P2IconFactory.P2ICON.P2_ATTENTION_OCT_80.getFontIcon(), 0, 0, 1, 1);
        gridPane.add(headerLabel, 1, 0);
        gridPane.add(cancelButton, 1, 1);
        gridPane.add(btnHelp, 2, 1);
        gridPane.add(allButton, 1, 2);
        gridPane.getColumnConstraints().addAll(P2GridConstraints.getCcComputedSizeAndHgrowLeft(),
                P2GridConstraints.getCcPrefSize());

        getVBoxCont().getChildren().addAll(gridPane);
    }
}