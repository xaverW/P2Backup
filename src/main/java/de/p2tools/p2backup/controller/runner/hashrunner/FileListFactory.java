package de.p2tools.p2backup.controller.runner.hashrunner;

import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.pathdata.PathData;
import de.p2tools.p2backup.controller.runner.FileRunner;
import de.p2tools.p2lib.tools.log.P2Log;

import java.io.File;
import java.util.List;
import java.util.Set;

public class FileListFactory {
    private FileListFactory() {
    }

//    public static int getFileList(File file, Set<File> foundFileList) {
//        // im Verzeichnis nach Dateien suchen
//        try {
//            new FileRunner() {
//                @Override
//                public void workFile(File file) {
//                    foundFileList.add(file);
//                }
//            }.recDir(file, true);
//        } catch (Exception ex) {
//            P2Log.errorLog(975102364, ex, "runFindFiles - " + file.getPath());
//        }
//        return foundFileList.size();
//    }

    public static void getFileList(BackupInfo backupInfo,
                                   List<File> fromPathList,
                                   Set<File> foundDirList,
                                   Set<File> foundFileList,
                                   Set<File> blockList) {
        // in den Verzeichnissen (fromPath) nach Dateien suchen: Dateien, Dirs, geblockte Dateien
        // BackupInfo nur für STOP
        for (File file : fromPathList) {
            runDirFindFiles(backupInfo, file, foundDirList, foundFileList, blockList);
        }
    }

    public static void getFileList(BackupInfo backupInfo,
                                   Set<File> foundDirList,
                                   Set<File> foundFileList,
                                   Set<File> blockList) {
        // in den Verzeichnissen des backupInfo nach Dateien suchen: Dateien, Dirs, geblockte Dateien
        // BackupInfo nur für STOP
        for (PathData p : backupInfo.getPathListFrom()) {
            runDirFindFiles(backupInfo, p.getFilePathFile(), foundDirList, foundFileList, blockList);
        }
    }

    private static int runDirFindFiles(BackupInfo backupInfo,
                                       File file,
                                       Set<File> foundDirList,
                                       Set<File> foundFileList,
                                       Set<File> blockFileList) {
        // Verzeichnis ablaufen und Dateien suchen: Dateien, Dirs, geblockte Dateien
        // BackupInfo nur für STOP
        try {
            new FileRunner(backupInfo) {
                @Override
                public void workDir(File file) {
                    // alle Dir eintragen
                    if (foundDirList != null) {
                        foundDirList.add(file);
                    }
                }

                @Override
                public void workFile(File file) {
                    // check file
                    if (checkFile(file, backupInfo)) {
                        foundFileList.add(file);
                    } else if (blockFileList != null) {
                        blockFileList.add(file);
                    }
                }
            }.recDir(file, true);
        } catch (Exception ex) {
            P2Log.errorLog(975102364, ex, "CreateHash.run - " + file.getPath());
        }
        return foundFileList.size();
    }

    public static boolean checkFile(File file, BackupInfo backupInfo) {
        // prüfen ob geblockt
        if (backupInfo.getPathListExcludeDir().isEmpty() &&
                backupInfo.getPathListExcludeFile().isEmpty()) {
            // dann nehmer alle :)
            return true;
        }

        String pathFile = file.getAbsolutePath();
        for (PathData p : backupInfo.getPathListExcludeDir().getValue()) {
            String pathDir = p.getPath();
            if (pathFile.startsWith(pathDir)) {
                return false;
            }
        }

        if (backupInfo.getPathListExcludeFile().isEmpty()) {
            // dann nehmer jetzt den Rest
            return true;
        }

        String strFile = file.getName();
        if (backupInfo.isFileFilterNot()) {
            // Treffer sollen nicht gesichert werden
            for (PathData p : backupInfo.getPathListExcludeFile().getValue()) {
                String exclude = p.getPath();
                if (treffer(strFile, exclude)) {
                    return false;
                }
            }
            return true;

        } else {
            // nur Treffer sollen gesichert werden
            for (PathData p : backupInfo.getPathListExcludeFile().getValue()) {
                String exclude = p.getPath();
                if (treffer(strFile, exclude)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static boolean treffer(String fileName, String exclude) {
        if (exclude.startsWith("*")) {
            final String s = exclude.substring(1);
            return fileName.endsWith(exclude.substring(1));
        } else if (exclude.endsWith("*")) {
            final String s = exclude.substring(0, exclude.length() - 1);
            return fileName.startsWith(exclude.substring(0, exclude.length() - 1));
        } else {
            return fileName.contains(exclude);
        }
    }
}
