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


package de.p2tools.p2backup.controller.data.filedata;

import de.p2tools.p2lib.tools.P2Index;

import java.nio.file.Path;

public class FileDataProps implements Comparable<FileData> {

    private long id = P2Index.getIndex();
    private long backupId = 0;

    private String filePathStr = ""; // Daten /home/emil/Desktop/daten/file1/1970/1960_05.jpg
    private String toPathStr = ""; // Backup /tmp/usb/backup/2025-10-23__10-12-00
    private String fileNameStr = "";

    private long date;
    private long size = 0;
    private boolean link = false; // Datei ist ein Link

    // Hash
    private String hash = "";
    private boolean error = false; // Hash kann nicht erstellt werden, nicht zugreifbar

    // ist nur für den Vergleich Backup/Data
    private boolean diff = false; // Dateien sind unterschiedlich
    private boolean existInData = false; // ist in den Daten
    private boolean existInBackup = false; // ist im Backup

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBackupId() {
        return backupId;
    }

    public void setBackupId(long backupId) {
        this.backupId = backupId;
    }


    public String getFilePathStr() {
        return filePathStr;
    }

    public void setFilePathStr(String filePathStr) {
        this.filePathStr = filePathStr;
        if (this.filePathStr.isEmpty()) {
            fileNameStr = "";
        } else {
            fileNameStr = Path.of(this.filePathStr).getFileName().toString();
        }
    }

    public String getToPathStr() {
        return toPathStr;
    }

    public void setToPathStr(String toPathStr) {
        this.toPathStr = toPathStr;
    }

    public String getFileNameStr() {
        return fileNameStr;
    }

    public void setFileNameStr(String fileNameStr) {
        this.fileNameStr = fileNameStr;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isLink() {
        return link;
    }

    public void setLink(boolean link) {
        this.link = link;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isDiff() {
        return diff;
    }

    public void setDiff(boolean diff) {
        this.diff = diff;
    }

    public boolean isExistInBackup() {
        return existInBackup;
    }

    public void setExistInBackup(boolean existInBackup) {
        this.existInBackup = existInBackup;
    }

    public boolean isExistInData() {
        return existInData;
    }

    public void setExistInData(boolean existInData) {
        this.existInData = existInData;
    }

    @Override
    public String toString() {
        return getFilePathStr();
    }

    @Override
    public int compareTo(FileData e) {
        return hash.compareTo(e.getHash());
    }
}
