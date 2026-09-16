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

import de.p2tools.p2lib.guitools.P2Text;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StartPane extends VBox {
    private final Stage stage;

    public StartPane(Stage stage) {
        this.stage = stage;
    }

    public void close() {
    }

    public void makeStart1() {
        HBox hBox = new HBox();
        hBox.setSpacing(25);
        hBox.setPadding(new Insets(20));

        ImageView iv = new ImageView();
        Image im = getHelpScreen1();
        iv.setSmooth(true);
        iv.setImage(im);

        hBox.getChildren().addAll(iv);
        VBox vBox = new VBox(10);
        vBox.getChildren().add(P2Text.getLblTextBold("""
                Das ist der Dialog beim
                ersten Programmstart.
                """));
        Label lblText = new Label("""
                1. Hier kann das
                Programmfenster in eine
                Minimal-Ansicht
                umgeschaltet werden.
                
                2. Hier befindet sich das
                Programm-Menü.
                
                3 Damit kann ein bereits
                angelegtes Backup hinzugefügt    
                werden.
                
                4. Ein neues Backup wird
                damit angelegt.
                
                
                
                
                """);
        lblText.setAlignment(Pos.TOP_LEFT);

        vBox.getChildren().add(lblText);
        hBox.getChildren().add(vBox);
        HBox.setHgrow(vBox, Priority.ALWAYS);

        getChildren().addAll(StartFactory.getTitle("Infos zur Programmoberfläche"), hBox);
    }

    public void makeStart2() {
        HBox hBox = new HBox();
        hBox.setSpacing(25);
        hBox.setPadding(new Insets(20));

        ImageView iv = new ImageView();
        Image im = getHelpScreen2();
        iv.setSmooth(true);
        iv.setImage(im);

        hBox.getChildren().addAll(iv);

        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.getChildren().add(P2Text.getLblTextBold("""
                Das ist die Liste der
                angelegten Backups
                """));
        Label text = new Label("""
                1. Damit können die
                verschiedenen Funktionen
                zum Einstellen der Backups
                umgeschaltet werden.
                
                2. Dieses Backup läuft gerade,
                der Fortschritt und die gerade
                kopierten Dateien werden
                angezeigt.
                
                3. Das ist ein weiteres Backup
                das gerade nicht läuft. Der
                grüne Rahmen zeigt an, dass
                es ausgewählt ist. Die
                Einstellungen beziehen sich
                immer auf das ausgewählte
                Backup.
                """);
        vBox.getChildren().add(text);
        hBox.getChildren().add(vBox);
        getChildren().addAll(StartFactory.getTitle("Infos zur Programmoberfläche"), hBox);
    }

    private javafx.scene.image.Image getHelpScreen1() {
        final String path = "/de/p2tools/p2backup/res/startdialog/mtviewer-startpage-1.png";
        return new javafx.scene.image.Image(path, 400,
                400,
                true, true);
    }

    private javafx.scene.image.Image getHelpScreen2() {
        final String path = "/de/p2tools/p2backup/res/startdialog/mtviewer-startpage-2.png";
        return new javafx.scene.image.Image(path, 400,
                400,
                true, true);
    }
}
