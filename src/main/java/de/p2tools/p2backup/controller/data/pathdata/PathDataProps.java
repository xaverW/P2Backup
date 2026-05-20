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


package de.p2tools.p2backup.controller.data.pathdata;

import de.p2tools.p2lib.configfile.config.Config;
import de.p2tools.p2lib.configfile.config.Config_intProp;
import de.p2tools.p2lib.configfile.config.Config_longProp;
import de.p2tools.p2lib.configfile.config.Config_stringProp;
import de.p2tools.p2lib.configfile.pdata.P2DataSample;
import de.p2tools.p2lib.tools.P2Index;
import javafx.beans.property.*;

import java.util.ArrayList;

public class PathDataProps extends P2DataSample<PathData> {

    public static final String TAG = "PathData";

    private LongProperty id = new SimpleLongProperty(P2Index.getIndex());
    private LongProperty backupInfoId = new SimpleLongProperty(0);
    private StringProperty path = new SimpleStringProperty("");
    private IntegerProperty count = new SimpleIntegerProperty(0); // Anzahl Dateien
    private LongProperty size = new SimpleLongProperty(0); // Größe aller Dateien

    public final Property[] properties = {id, path, count, size};

    @Override
    public Config[] getConfigsArr() {
        ArrayList<Config> configList = new ArrayList<>();
        configList.add(new Config_longProp("id", id));
        configList.add(new Config_longProp("backupInfoId", backupInfoId));
        configList.add(new Config_stringProp("path", path));
        configList.add(new Config_intProp("count", count));
        configList.add(new Config_longProp("size", size));
        return configList.toArray(new Config[]{});
    }

    @Override
    public String getTag() {
        return TAG;
    }

    public long getId() {
        return id.get();
    }

    public LongProperty idProperty() {
        return id;
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public long getBackupInfoId() {
        return backupInfoId.get();
    }

    public LongProperty backupInfoIdProperty() {
        return backupInfoId;
    }

    public void setBackupInfoId(long backupInfoId) {
        this.backupInfoId.set(backupInfoId);
    }

    public String getPath() {
        return path.get();
    }

    public StringProperty pathProperty() {
        return path;
    }

    public void setPath(String path) {
        this.path.set(path);
    }

    public int getCount() {
        return count.get();
    }

    public IntegerProperty countProperty() {
        return count;
    }

    public void setCount(int count) {
        this.count.set(count);
    }

    public long getSize() {
        return size.get();
    }

    public LongProperty sizeProperty() {
        return size;
    }

    public void setSize(long size) {
        this.size.set(size);
    }

    public void copyToMe(PathData abo) {
        for (int i = 0; i < this.properties.length; ++i) {
            this.properties[i].setValue(abo.properties[i].getValue());
        }
    }

    public PathData getCopy() {
        final PathData ret = new PathData();
        for (int i = 0; i < this.properties.length; ++i) {
            ret.properties[i].setValue(this.properties[i].getValue());
        }
        return ret;
    }
}
