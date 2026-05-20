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

package de.p2tools.p2backup.controller.data.backupinfo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class BackupInfo extends BackupInfosProps {
    // Sind die Daten eines angelegten Backups: Anzahl der Backups (BackupData), Daten zum Sichern, ...

    public RunnerDto runnerDto = new RunnerDto();
    public BooleanProperty notReady = new SimpleBooleanProperty(false);

    public BackupInfo() {
        notReady.bind(backupPathProperty().isEmpty().or(getPathListFrom().sizeProperty().isEqualTo(0)));
    }

    public boolean isNotReady() {
        return notReady.get();
    }

    public BooleanProperty notReadyProperty() {
        return notReady;
    }

    public void clear() {
        setCount(0);
        setSize(0);
    }
}
