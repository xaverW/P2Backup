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


package de.p2tools.p2backup.controller.config;

import de.p2tools.p2lib.tools.shortcut.P2ShortcutKey;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashSet;

public class PShortcut {


    public static final P2ShortcutKey SHORTCUT_CENTER_GUI =
            new P2ShortcutKey(ProgConfig.SHORTCUT_CENTER_GUI, ProgConfig.SHORTCUT_CENTER_INIT,
                    "Center Programm",
                    "Das Programmfenster wird auf dem Bildschirm zentriert positioniert.");

    public static final P2ShortcutKey SHORTCUT_MINIMIZE_GUI =
            new P2ShortcutKey(ProgConfig.SHORTCUT_MINIMIZE_GUI, ProgConfig.SHORTCUT_MINIMIZE_INIT,
                    "Programm-GUI minimieren",
                    "Das Programmfenster wird minimiert.");

    // Menü
    public static final P2ShortcutKey SHORTCUT_QUIT_PROGRAM =
            new P2ShortcutKey(ProgConfig.SHORTCUT_QUIT_PROGRAM, ProgConfig.SHORTCUT_QUIT_PROGRAM_INIT,
                    "Programm beenden",
                    "Das Programm wird beendet. Wenn noch ein Download läuft, wird in einem Dialog abgefragt, was getan werden soll.");

    public static final P2ShortcutKey SHORTCUT_QUIT_PROGRAM_WAIT =
            new P2ShortcutKey(ProgConfig.SHORTCUT_QUIT_PROGRAM_WAIT, ProgConfig.SHORTCUT_QUIT_PROGRAM_WAIT_INIT,
                    "Programm beenden, Downloads abwarten",
                    "Das Programm wird beendet. Wenn noch ein Download läuft, wird dieser noch abgeschlossen und " +
                            "das Programm wartet auf den Download. Der Dialog mit der Abfrage was getan werden soll, wird aber übersprungen.");

    private static final ObservableList<P2ShortcutKey> shortcutList = FXCollections.observableArrayList();

    public PShortcut() {
        shortcutList.add(SHORTCUT_CENTER_GUI);
        shortcutList.add(SHORTCUT_MINIMIZE_GUI);

        shortcutList.add(SHORTCUT_QUIT_PROGRAM);
        shortcutList.add(SHORTCUT_QUIT_PROGRAM_WAIT);
    }

    public static synchronized ObservableList<P2ShortcutKey> getShortcutList() {
        return shortcutList;
    }

    public static synchronized boolean checkDoubleShortcutList() {
        HashSet<String> hashSet = new HashSet<>();
        for (P2ShortcutKey ps : shortcutList) {
            if (!hashSet.add(ps.getActShortcut())) {
                hashSet.clear();
                return true;
            }
        }
        hashSet.clear();
        return false;
    }
}
