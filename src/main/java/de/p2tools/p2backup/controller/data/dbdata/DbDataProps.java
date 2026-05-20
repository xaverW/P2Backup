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


package de.p2tools.p2backup.controller.data.dbdata;

import de.p2tools.p2lib.configfile.config.Config;
import de.p2tools.p2lib.configfile.config.Config_longProp;
import de.p2tools.p2lib.configfile.config.Config_stringProp;
import de.p2tools.p2lib.configfile.pdata.P2DataSample;
import de.p2tools.p2lib.tools.P2Index;
import javafx.beans.property.*;

import java.util.ArrayList;

public class DbDataProps extends P2DataSample<DbData> {

    public static final String TAG = "DbData";

    private LongProperty id = new SimpleLongProperty(P2Index.getIndex());
    private LongProperty backupInfoId = new SimpleLongProperty(0);
    private StringProperty name = new SimpleStringProperty("");
    private StringProperty path = new SimpleStringProperty("");

    public final Property[] properties = {id, backupInfoId, name, path};

    @Override
    public Config[] getConfigsArr() {
        ArrayList<Config> configList = new ArrayList<>();
        configList.add(new Config_longProp("id", id));
        configList.add(new Config_longProp("backupInfoId", backupInfoId));
        configList.add(new Config_stringProp("name", name));
        configList.add(new Config_stringProp("path", path));
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

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
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

    public void copyToMe(DbData dbData) {
        for (int i = 0; i < this.properties.length; ++i) {
            this.properties[i].setValue(dbData.properties[i].getValue());
        }
    }

    public DbData getCopy() {
        final DbData ret = new DbData();
        for (int i = 0; i < this.properties.length; ++i) {
            ret.properties[i].setValue(this.properties[i].getValue());
        }
        return ret;
    }
}
