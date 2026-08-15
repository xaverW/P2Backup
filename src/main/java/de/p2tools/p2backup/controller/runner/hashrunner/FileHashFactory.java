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


package de.p2tools.p2backup.controller.runner.hashrunner;

import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2lib.hash.HashConst;
import de.p2tools.p2lib.tools.log.P2Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class FileHashFactory {
    private FileHashFactory() {
    }

    public static FileData getFileData(BackupInfo backupInfo, String toPath,
                                       boolean quick, File file, boolean followLink) {
        // liefert ein FileData mit oder ohne wenn (quick) dem Hash
        // toPath wird vom Pfad entfernt, wenn vorhanden -> wird dann der Pfad des ORG-DATEN-File

        try {
            // zuerst mal den Hash bauen
            String hashString = "";
            if (!quick) {
                hashString = getFileHash(backupInfo, file);
            }

            String strFile = file.getAbsolutePath();
            boolean link = Files.isSymbolicLink(file.toPath());
            if (link && followLink) {
                strFile = file.getCanonicalPath(); // ist der Pfad des verlinkten Files
            }
            return new FileData(strFile, toPath, file.lastModified(), file.length(), hashString, link);

        } catch (Exception ex) {
            P2Log.errorLog(784512589, ex.getLocalizedMessage());
            return null;
        }
    }

    public static void setFileData(BackupInfo backupInfo, boolean quick, FileData fileData) {
        try {
            File file = fileData.getFilePath().toFile();
            // zuerst mal den Hash bauen
            String hashString = "";
            if (!quick) {
                hashString = getFileHash(backupInfo, file);
            }

            fileData.setDate(file.lastModified());
            fileData.setSize(file.length());
            fileData.setHash(hashString);
            fileData.setLink(Files.isSymbolicLink(file.toPath()));
        } catch (Exception ex) {
            P2Log.errorLog(969695687, ex.getLocalizedMessage());
        }
    }

    public static void setFileDataHash(BackupInfo backupInfo, FileData fileData) {
        final byte[] buffer = new byte[1024]; // todo
        InputStream srcStream = null;
        String hashString = "";

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HashConst.HASH_MD5);
            srcStream = new DigestInputStream(new FileInputStream(fileData.getFilePath().toFile()), messageDigest);

            while (!backupInfo.runnerDto.isStop() && srcStream.read(buffer) > -1) {
            }
            hashString = getHashString(messageDigest.digest());
            fileData.setHash(hashString);
            return;

        } catch (Exception ex) {
            if (backupInfo.runnerDto.isAsk()) {
                if (!FileFactory.goOnError(backupInfo, fileData.getFilePathStr(), true)) {
                    backupInfo.runnerDto.setStop();
                }
            }
            P2Log.errorLog(963210472, ex, "Fehler! " + fileData.getFileNameStr());
        } finally {
            try {
                if (srcStream != null) {
                    srcStream.close();
                }
            } catch (IOException ignored) {
            }
        }

        fileData.setHash(FileFactory.HASH_ERROR);
    }

    private static String getFileHash(BackupInfo backupInfo, File file) {
        final byte[] buffer = new byte[1024]; // todo
        InputStream srcStream = null;
        String hashString = "";

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HashConst.HASH_MD5);
            srcStream = new DigestInputStream(new FileInputStream(file), messageDigest);

            while (!backupInfo.runnerDto.isStop() && srcStream.read(buffer) > -1) {
            }
            hashString = getHashString(messageDigest.digest());
            return hashString;

        } catch (Exception ex) {
            if (backupInfo.runnerDto.isAsk()) {
                if (!FileFactory.goOnError(backupInfo, file.getAbsolutePath(), true)) {
                    backupInfo.runnerDto.setStop();
                }
            }
            P2Log.errorLog(963210472, ex, "Fehler! " + file.getAbsolutePath());
        } finally {
            try {
                if (srcStream != null) {
                    srcStream.close();
                }
            } catch (IOException ignored) {
            }
        }

        return FileFactory.HASH_ERROR;
    }

    private static String getHashString(byte[] hash) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : hash) {
            stringBuilder.append(toHexString(b));
        }
        return stringBuilder.toString();
    }

    private static String toHexString(byte b) {
        int value = (b & 0x7F) + (b < 0 ? 128 : 0);
        String ret = (value < 16 ? "0" : "");
        ret += Integer.toHexString(value).toLowerCase();
        return ret;
    }
}
