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

package de.p2tools.p2backup.controller.data.backupdata;

import de.p2tools.p2lib.tools.P2Index;
import de.p2tools.p2lib.tools.date.P2LDateTimeProperty;
import javafx.beans.property.*;

import java.time.LocalDateTime;

public class BackupDataProps implements Comparable<BackupData> {

    private LongProperty id = new SimpleLongProperty(P2Index.getIndex());
    private LongProperty backupInfoId = new SimpleLongProperty(0);
    private IntegerProperty count = new SimpleIntegerProperty(0); // Anzahl aller Dateien
    private LongProperty size = new SimpleLongProperty(0); // Größe aller Dateien
    private P2LDateTimeProperty startDate = new P2LDateTimeProperty(LocalDateTime.MIN); // Startzeit Backup
    private BooleanProperty ok = new SimpleBooleanProperty(true);
    private StringProperty subPath = new SimpleStringProperty(""); // 2025-10-25__10-12-00

    public final Property[] properties = {id, backupInfoId, count, startDate, ok, subPath};

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

    public LocalDateTime getStartDate() {
        return startDate.get();
    }

    public P2LDateTimeProperty startDateProperty() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate.set(startDate);
    }

    public boolean isOk() {
        return ok.get();
    }

    public BooleanProperty okProperty() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok.set(ok);
    }

    public String getSubPath() {
        return subPath.get();
    }

    public StringProperty subPathProperty() {
        return subPath;
    }

    public void setSubPath(String subPath) {
        this.subPath.set(subPath);
    }

    // ======================
    public void copyToMe(BackupData data) {
        for (int i = 0; i < this.properties.length; ++i) {
            this.properties[i].setValue(data.properties[i].getValue());
        }
    }

    public BackupData getCopy() {
        final BackupData ret = new BackupData();
        for (int i = 0; i < this.properties.length; ++i) {
            ret.properties[i].setValue(this.properties[i].getValue());
        }
        return ret;
    }

    @Override
    public int compareTo(BackupData arg0) {
        if (getId() == arg0.getId()) {
            return 0;
        } else if (getId() > arg0.getId()) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return getSubPath();
    }
}
