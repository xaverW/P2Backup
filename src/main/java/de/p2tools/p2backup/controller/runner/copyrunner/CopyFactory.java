package de.p2tools.p2backup.controller.runner.copyrunner;

import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.runner.hashrunner.HashFactory;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import de.p2tools.p2lib.p2event.P2Event;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class CopyFactory {
    private CopyFactory() {
    }

    public static boolean checkToPath(Path toDataPath) {
        if (toDataPath == null ||
                !toDataPath.toFile().isDirectory() ||
                !toDataPath.toFile().exists()) {
            P2AlertAppThread.showErrorAlert("Dateien kopieren", "Der Zielordner\n" +
                    toDataPath + "\n" +
                    "kann nicht angelegt werden.");
            return false;
        }
        if (toDataPath.toFile().listFiles().length != 0) {
            P2AlertAppThread.showErrorAlert("Dateien kopieren", "Der Zielordner\n" +
                    toDataPath + "\n" +
                    "ist nicht leer und kann so nicht verwendet werden.");
            return false;
        }

        return true;
    }

    public static boolean copyFiles(BackupInfo backupInfo,
                                    List<FileData> fileDataList /* eingelesenen DATEN */) {

        ProgData.getInstance().pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        for (FileData fileData : fileDataList) {
            // toFilePath:  /tmp/usb/backup/2025-10-21__16-29-29/Daten/home/emil/Desktop/daten/file2/1972/bild.jpg
            // toPath:      /tmp/usb/backup/2025-10-23__10-12-00
            // filePath:    /home/emil/Desktop/daten/file1/1970/1960_05.jpg

            if (backupInfo.runnerDto.isStop()) {
                return false;
            }

            HashFactory.setFileDataHash(backupInfo, fileData);
            fileData.setErrorHash(fileData.getHash().equals(FileFactory.HASH_ERROR));
            if (fileData.isErrorHash()) {
                System.out.println("=== get hash error ===");
                System.out.println("   from " + fileData.getFilePathStr());
                continue;
            }

            if (fileData.getFilePathStr().isEmpty() ||
                    fileData.getToPathStr().isEmpty()) {
                // dann nix
                continue;
            }

            Path fromPath = fileData.getFilePath(); // ist der aktuelle Pfad zum File
            Path toFilePath = fileData.getBackupFilePath(); // neue Speicherpfad

            try {
                FileUtils.copyFile(fromPath.toFile(), toFilePath.toFile(), StandardCopyOption.COPY_ATTRIBUTES);
                backupInfo.runnerDto.setRunnerFileName(fileData.getFileNameStr());
                backupInfo.runnerDto.addRunnerAlreadyDone();
            } catch (Exception ex) {
                // auch die sind nicht zugreifbar, wahrscheinlich während des Backups gelöscht worden?? Vorsichtshalber
                fileData.setHash(FileFactory.HASH_ERROR);
                fileData.setErrorHash(true);
                System.out.println("=== copy file error ===");
                System.out.println("   from " + fromPath);
                System.out.println("     to " + toFilePath);

                if (!FileFactory.goOnError(backupInfo, fromPath.toString(), true)) {
                    backupInfo.runnerDto.setRunnerMax(0);
                    backupInfo.runnerDto.setRunnerFileName("");
                    return false;
                }
            }
        }

        backupInfo.runnerDto.setRunnerFileName("");
        ProgData.getInstance().pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        return true;
    }

    public static boolean moveFiles(BackupInfo backupInfo, String oldToPath,
                                    FileDataList fileDataList, FileDataList resetList) {
        // move
        for (FileData fileData : fileDataList) {
            if (backupInfo.runnerDto.isStop()) {
                break;
            }

            HashFactory.setFileData(backupInfo, false, fileData);
            fileData.setErrorHash(fileData.getHash().equals(FileFactory.HASH_ERROR));
            if (fileData.isErrorHash()) {
                continue;
            }


            File fromFile = fileData.getBackupFilePath(oldToPath).toFile();
            File toFile = fileData.getBackupFilePath().toFile();
            try {
                FileUtils.moveFileToDirectory(fromFile, toFile.getParentFile(), true);
                resetList.add(fileData);
                backupInfo.runnerDto.setRunnerFileName(fileData.getFileNameStr());
                backupInfo.runnerDto.addRunnerAlreadyDone();
            } catch (IOException e) {
                if (!FileFactory.goOnError(backupInfo, fromFile.toString(), true)) {
                    backupInfo.runnerDto.setRunnerFileName("");
                    return false;
                }
            }
        }


        backupInfo.runnerDto.setRunnerFileName("");
        return true;
    }

    public static boolean linkFiles(BackupInfo backupInfo, String oldToPath,
                                    List<FileData> fileDataList) {
        // in der FileDataList sind die Dateien aus dem Backup die verlinkt werden
        for (FileData fileData : fileDataList) {
            if (backupInfo.runnerDto.isStop()) {
                break;
            }

            HashFactory.setFileDataHash(backupInfo, fileData);
            fileData.setErrorHash(fileData.getHash().equals(FileFactory.HASH_ERROR));
            if (fileData.isErrorHash()) {
                continue;
            }

            Path fromFile = fileData.getBackupFilePath(oldToPath);
            Path toFile = fileData.getBackupFilePath();

            try {
                // create a hard link
                backupInfo.runnerDto.setRunnerFileName(fileData.getFileNameStr());
                backupInfo.runnerDto.addRunnerAlreadyDone();
                FileUtils.createParentDirectories(toFile.toFile());
                Files.createLink(toFile, fromFile);
            } catch (Exception ex) {
                if (!FileFactory.goOnError(backupInfo, fromFile.toString(), true)) {
                    backupInfo.runnerDto.setRunnerFileName("");
                    return false;
                }
            }
        }

        backupInfo.runnerDto.setRunnerFileName("");
        return true;
    }
}
