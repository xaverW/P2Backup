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

import de.p2tools.p2backup.controller.config.ProgConst;
import de.p2tools.p2backup.controller.data.backupdata.BackupDataList;
import de.p2tools.p2backup.controller.data.pathdata.PathDataList;
import de.p2tools.p2lib.tools.P2Index;
import de.p2tools.p2lib.tools.date.P2LDateProperty;
import de.p2tools.p2lib.tools.date.P2LDateTimeProperty;
import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;

public class BackupInfoProps implements Comparable<BackupInfo> {

    private LongProperty id = new SimpleLongProperty(P2Index.getIndex());
    private LongProperty version = new SimpleLongProperty(ProgConst.BACUP_VERSION); // ist immer die aktuelle Version
    private StringProperty name = new SimpleStringProperty("");
    private StringProperty color = new SimpleStringProperty("#000000");
    private StringProperty description = new SimpleStringProperty("");
    private StringProperty backupPath = new SimpleStringProperty("");
    private LongProperty lastBackupId = new SimpleLongProperty(0);
    private P2LDateTimeProperty lastStartDate = new P2LDateTimeProperty(LocalDateTime.MIN); // letztes Backup

    private IntegerProperty how = new SimpleIntegerProperty(ProgConst.BACKUP_DIFF);

    private BooleanProperty fileFilterNot = new SimpleBooleanProperty(true);

    private IntegerProperty sumDay = new SimpleIntegerProperty(5);
    private IntegerProperty sumWeek = new SimpleIntegerProperty(5);
    private IntegerProperty sumMonth = new SimpleIntegerProperty(5);

    private P2LDateProperty genDate = new P2LDateProperty(LocalDate.now()); // Erstelldatum

    private PathDataList pathListFrom = new PathDataList("pathListFrom");
    private PathDataList pathListExcludeDir = new PathDataList("pathListExcludeDir");
    private PathDataList pathListExcludeFile = new PathDataList("pathListExcludeFile");

    // Liste der einzelnen Backups:
    private BackupDataList backupDataList = new BackupDataList();

    // Werte für die Tools
    private IntegerProperty count = new SimpleIntegerProperty(0); // Anzahl aller Dateien
    private LongProperty size = new SimpleLongProperty(0); // Größe aller Dateien

    public final Property[] properties = {id, version, name, color, description, backupPath, lastBackupId, lastStartDate,
            how, fileFilterNot,
            sumDay, sumWeek, sumMonth, genDate};


    public long getId() {
        return id.get();
    }

    public LongProperty idProperty() {
        return id;
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public long getVersion() {
        return version.get();
    }

    public LongProperty versionProperty() {
        return version;
    }

    public void setVersion(long version) {
        this.version.set(version);
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

    public String getColor() {
        return color.get();
    }

    public StringProperty colorProperty() {
        return color;
    }

    public void setColor(String color) {
        this.color.set(color);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getBackupPath() {
        return backupPath.get();
    }

    public StringProperty backupPathProperty() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath.set(backupPath);
    }

    public long getLastBackupId() {
        return lastBackupId.get();
    }

    public LongProperty lastBackupIdProperty() {
        return lastBackupId;
    }

    public void setLastBackupId(long lastBackupId) {
        this.lastBackupId.set(lastBackupId);
    }

    public LocalDateTime getLastStartDate() {
        return lastStartDate.get();
    }

    public P2LDateTimeProperty lastStartDateProperty() {
        return lastStartDate;
    }

    public void setLastStartDate(LocalDateTime lastStartDate) {
        this.lastStartDate.set(lastStartDate);
    }

    public int getHow() {
        return how.get();
    }

    public IntegerProperty howProperty() {
        return how;
    }

    public void setHow(int how) {
        this.how.set(how);
    }

    public boolean isFileFilterNot() {
        return fileFilterNot.get();
    }

    public BooleanProperty fileFilterNotProperty() {
        return fileFilterNot;
    }

    public void setFileFilterNot(boolean fileFilterNot) {
        this.fileFilterNot.set(fileFilterNot);
    }

    public int getSumDay() {
        return sumDay.get();
    }

    public IntegerProperty sumDayProperty() {
        return sumDay;
    }

    public void setSumDay(int sumDay) {
        this.sumDay.set(sumDay);
    }

    public int getSumWeek() {
        return sumWeek.get();
    }

    public IntegerProperty sumWeekProperty() {
        return sumWeek;
    }

    public void setSumWeek(int sumWeek) {
        this.sumWeek.set(sumWeek);
    }

    public int getSumMonth() {
        return sumMonth.get();
    }

    public IntegerProperty sumMonthProperty() {
        return sumMonth;
    }

    public void setSumMonth(int sumMonth) {
        this.sumMonth.set(sumMonth);
    }

    public LocalDate getGenDate() {
        return genDate.get();
    }

    public P2LDateProperty genDateProperty() {
        return genDate;
    }

    public void setGenDate(LocalDate genDate) {
        this.genDate.set(genDate);
    }

    public PathDataList getPathListFrom() {
        return pathListFrom;
    }

    public PathDataList getPathListExcludeDir() {
        return pathListExcludeDir;
    }

    public PathDataList getPathListExcludeFile() {
        return pathListExcludeFile;
    }

    public BackupDataList getBackupDataList() {
        return backupDataList;
    }

    public BackupDataList getBackupDataListReverse() {
        final BackupDataList bl = new BackupDataList();
        bl.addAll(backupDataList);
        bl.sort(Comparator.reverseOrder());
        return bl;
    }

    // =========================
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

    // =================================
    @Override
    public String toString() {
        return getName();
    }

    public void copyToMe(BackupInfo data) {
        for (int i = 0; i < this.properties.length; ++i) {
            this.properties[i].setValue(data.properties[i].getValue());
        }
        backupDataList.forEach(b -> backupDataList.add(b.getCopy()));
    }

    public BackupInfo getCopy() {
        final BackupInfo ret = new BackupInfo();
        for (int i = 0; i < this.properties.length; ++i) {
            ret.properties[i].setValue(this.properties[i].getValue());
        }
        backupDataList.forEach(b -> ret.getBackupDataList().add(b.getCopy()));
        return ret;
    }

    @Override
    public int compareTo(BackupInfo arg0) {
        if (getId() == arg0.getId()) {
            return 0;
        } else {
            return -1;
        }
    }
}
