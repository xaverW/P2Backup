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

import java.nio.file.Path;

public class FileData extends FileDataProps {

    /*
     * sind die Daten zu einer Datei
     * wird erstellt, bei FileRun und liegen in den BackupHashData
     */

    String parentFilePathStr = "";

    public FileData() {
    }

    public FileData(String dataFile, String toPath,
                    long date, long fileSize, String hash, boolean link) {
        setFilePathStr(dataFile);
        setToPathStr(toPath);
        setDate(date);
        setSize(fileSize);
        setHash(hash);
        setLink(link);
    }

    public Path getFilePath() {
        if (getFilePathStr().isEmpty()) {
            return null;
        }
        return Path.of(getFilePathStr());
    }

    public String getCorrFilePathStr() {
        if (getFilePathStr().isEmpty()) {
            return "";
        }
        return FileFactory.setCorrPath(getFilePathStr());
    }

    public void setParentFilePathStr(String parentFilePathStr) {
        this.parentFilePathStr = parentFilePathStr;
    }

    public Path getParentFilePath() {
        if (getFilePathStr().isEmpty()) {
            return null;
        }
        return getFilePath().getParent();
    }

    public String getParentFilePathStr() {
        if (getFilePathStr().isEmpty()) {
            return "";
        }
        return getFilePath().getParent().toString();
    }

    public String getCorrParentFilePathStr() {
        if (getFilePathStr().isEmpty()) {
            return "";
        }
        return FileFactory.setCorrPath(getFilePath().getParent().toString());
    }

    public Path getBackupFilePath() {
        if (getFilePathStr().isEmpty()) {
            return null;
        }
        return Path.of(getToPathStr(), FileFactory.setCorrPath(getFilePathStr()));
    }

    public String getBackupFilePathStr() {
        if (getFilePathStr().isEmpty()) {
            return "";
        }
        return Path.of(getToPathStr(), FileFactory.setCorrPath(getFilePathStr())).toString();
    }

    public Path getBackupFilePath(String toPath) {
        if (getFilePathStr().isEmpty()) {
            return null;
        }
        return Path.of(toPath, FileFactory.setCorrPath(getFilePathStr()));
    }

    public String getBackupFilePathStr(String toPath) {
        if (getFilePathStr().isEmpty()) {
            return "";
        }
        return Path.of(toPath, FileFactory.setCorrPath(getFilePathStr())).toString();
    }

    public Path getParentBackupFilePath() {
        if (getFilePathStr().isEmpty()) {
            return null;
        }
        return Path.of(getToPathStr(), FileFactory.setCorrPath(getFilePathStr())).getParent();
    }

    public String getParentBackupFilePathStr() {
        if (getFilePathStr().isEmpty()) {
            return "";
        }
        return Path.of(getToPathStr(), FileFactory.setCorrPath(getFilePathStr())).getParent().toString();
    }

    public void setCorrBackupPath(String toDataPath) {
        // BackupPath setzen
        setFilePathStr(FileFactory.setCorrPath(getFilePathStr()));
        setToPathStr(toDataPath);
    }

    public FileData getCopy() {
        FileData fileData = new FileData();
        fileData.setBackupId(getBackupId());
        fileData.setFilePathStr(getFilePathStr());
        fileData.setToPathStr(getToPathStr());
        fileData.setDate(getDate());
        fileData.setSize(getSize());
        fileData.setLink(isLink());
        fileData.setHash(getHash());
        fileData.setDiff(isDiff());
        fileData.setError(isError());
        fileData.setExistData(isExistData());
        fileData.setExistBackup(isExistBackup());

        return fileData;
    }
}
