package de.p2tools.p2backup.controller.runner.tools;

import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.filedata.FileData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2backup.controller.data.filedata.FileDataProps;
import de.p2tools.p2backup.controller.data.filedata.FileFactory;
import de.p2tools.p2backup.controller.runner.hashrunner.HashFactory;
import de.p2tools.p2lib.tools.log.P2Log;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CompareFactory {

    private CompareFactory(BackupInfo backupInfo) {
    }

    public static void compare(Stage stage,
                               FileDataList fileListData, FileDataList fileListBackup,
                               List<FileData> resultList, List<FileData> errorList) {

        // beim Backup prüfen ist fileListData die aus der DB, fileListBackup die aus dem Backup-Ordner
        final HashMap<String, FileData> dataMap = new HashMap<>();
        fileListData.forEach(fileData -> {
            fileData.resetError();
            fileData.setExistInData(true);
            dataMap.put(fileData.getFilePathStr(), fileData);
            resultList.add(fileData);
            if (fileData.getHash().equals(FileFactory.HASH_ERROR)) {
                fileData.setErrorHash(true);
                errorList.add(fileData);
            }
        });

        fileListBackup.forEach(fileBackup -> {
            fileBackup.resetError();
            fileBackup.setExistInBackup(true);
        });

        // vergleichen
        fileListBackup.forEach(fileBackup -> {
            String path = fileBackup.getFilePathStr();
            FileData fileDb = dataMap.remove(path);

            if (fileDb == null) {
                // dann nur im Backup
                resultList.add(fileBackup);
                if (fileBackup.getHash().equals(FileFactory.HASH_ERROR)) {
                    fileBackup.setErrorHash(true);
                    errorList.add(fileBackup);
                }

            } else {
                // dann in beiden
                fileDb.setExistInBackup(true);
                if (fileBackup.getHash().equals(FileFactory.HASH_ERROR)) {
                    fileDb.setErrorHash(true);
                    errorList.add(fileDb);

                } else if (!fileBackup.getHash().equals(fileDb.getHash())) {
                    fileDb.setErrorDiff(true);
                    errorList.add(fileDb);
                }
            }
        });

        errorList.addAll(resultList.stream().filter(f ->
                f.isErrorDiff() || f.isErrorHash() || f.isOnlyInData() || f.isOnlyInBackup()).toList());
        resultList.sort(Comparator.comparing(FileDataProps::getFilePathStr));
        errorList.sort(Comparator.comparing(FileDataProps::getFilePathStr));
    }

    public static void compareQuick(BackupInfo backupInfo,
                                    FileDataList fileListDataDb, FileDataList fileListBackup,
                                    List<FileData> resultList, List<FileData> errorList) {

        final HashMap<String, FileData> backupFileMap = new HashMap<>();
        for (FileData backupFile : fileListBackup) {
            backupFile.resetError();
            backupFile.setExistInBackup(true);
            backupFileMap.put(backupFile.getFilePathStr(), backupFile);
            resultList.add(backupFile);
        }

        for (FileData dataFile : fileListDataDb) {
            dataFile.resetError();
            dataFile.setExistInData(true);

            String path = dataFile.getFilePathStr();
            FileData backupFile = backupFileMap.remove(path);

            if (backupFile == null) {
                resultList.add(dataFile);
                continue;
            }

            backupFile.setExistInData(true);
            if (dataFile.getSize() != backupFile.getSize()) {
                // könnten doch gleich sein
                P2Log.debugLog("QuickError, size!!: " + backupFile.getBackupFilePathStr());
                isEqual(backupInfo, dataFile, backupFile);
                continue;
            }

            if (dataFile.getDate() != backupFile.getDate()) {
                // könnten doch gleich sein
                P2Log.debugLog("QuickError, date!!: " + backupFile.getBackupFilePathStr());
                isEqual(backupInfo, dataFile, backupFile);
                continue;
            }
        }
        errorList.addAll(resultList.stream().filter(f ->
                f.isErrorDiff() || f.isErrorHash() || f.isOnlyInData() || f.isOnlyInBackup()).toList());

        errorList.sort(Comparator.comparing(FileDataProps::getFilePathStr));
        resultList.sort(Comparator.comparing(FileDataProps::getFilePathStr));
    }

    private static void isEqual(BackupInfo backupInfo, FileData dataFile, FileData backupFile) {
        FileData dataHash = HashFactory.getFileData(backupInfo, "", false,
                dataFile.getFilePath().toFile(), true);
        FileData backupHash = HashFactory.getFileData(backupInfo, "", false,
                backupFile.getBackupFilePath().toFile(), true);

        if (backupHash == null || dataHash == null || !dataHash.getHash().equals(backupHash.getHash())) {
            backupFile.setErrorDiff(true);
            P2Log.debugLog("QuickError, diff: " + backupFile.getBackupFilePathStr());
            if (backupHash != null && backupHash.getHash().equals(FileFactory.HASH_ERROR)) {
                backupFile.setErrorHash(true);
                P2Log.debugLog("QuickError, hash: " + backupFile.getBackupFilePathStr());
            }
        }
    }
}
