/*
 * P2tools Copyright (C) 2018 W. Xaver W.Xaver[at]googlemail.com
 * https://www.p2tools.de/
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


package de.p2tools.p2backup.controller.data;

import de.p2tools.p2lib.configfile.config.Config;
import de.p2tools.p2lib.configfile.configlist.ConfigStringList;
import de.p2tools.p2lib.configfile.pdata.P2DataSample;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class BackupPathList extends P2DataSample<String> {

    public static final String TAG = "BackupPathList";

    private ObservableList<String> path = FXCollections.observableArrayList();

    public BackupPathList() {
    }

    @Override
    public Config[] getConfigsArr() {
        ArrayList<Config> configList = new ArrayList<>();
        configList.add(new ConfigStringList("path", path));
        return configList.toArray(new Config[]{});
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public ObservableList<String> getPath() {
        return path;
    }

    public void setPath(ObservableList<String> path) {
        this.path = path;
    }
}
