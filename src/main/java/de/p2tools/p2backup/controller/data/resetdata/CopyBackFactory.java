package de.p2tools.p2backup.controller.data.resetdata;

import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.sqlite.SqlResetData;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.alert.P2Alert;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import de.p2tools.p2lib.tools.log.P2Log;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CopyBackFactory {
    private CopyBackFactory() {
    }

    public static boolean getResetDataList(BackupInfo backupInfo, BackupData backupData, CopyBackDataList copyBackDataList) {
        List<BackupData> list = new ArrayList<>();
        HashMap<String, CopyBackData> hashMap = new HashMap<>();

        backupInfo.getBackupDataList().forEach(ba -> {
            if (ba.getId() >= backupData.getId()) {
                list.add(ba);
            }
        });

        list.sort(Comparator.reverseOrder());
        list.forEach(ba -> {
            CopyBackDataList reset = new CopyBackDataList();
            SqlResetData.getResetData(backupInfo, ba, reset);
            reset.forEach(r -> hashMap.put(r.getFileData().getFilePathStr(), r));
        });

        copyBackDataList.setAll(hashMap.values());
        return true;
    }

    public static boolean copyResetFiles(Stage stage, BackupInfo backupInfo, CopyBackDataList copyBackDataList, String destDir) {
        if (copyBackDataList.isEmpty()) {
            P2Alert.showErrorAlert(stage, "Backup kopieren", "Die Liste der Dateien " +
                    "zum Kopieren ist leer.");
            return false;
        }

        if (destDir.isEmpty()) {
            P2Alert.showErrorAlert(stage, "Backup kopieren", "Es wurde kein Ziel angegeben.");
            return false;
        }

        Path path = Path.of(destDir);
        if (!path.toFile().exists()) {
            P2Alert.showErrorAlert(stage, "Backup kopieren", "Der ZielOrdner:" + P2LibConst.LINE_SEPARATOR +
                    destDir + P2LibConst.LINE_SEPARATORx2 +
                    "existiert nicht.");
            return false;
        }

        if (!path.toFile().isDirectory()) {
            P2Alert.showErrorAlert(stage, "Backup kopieren", "Der ZielOrdner:" + P2LibConst.LINE_SEPARATOR +
                    destDir + P2LibConst.LINE_SEPARATORx2 +
                    "ist kein ein Verzeichnis.");
            return false;
        }

        if (path.toFile().listFiles().length > 0) {
            P2Alert.showErrorAlert(stage, "Backup kopieren", "Der ZielOrdner:" + P2LibConst.LINE_SEPARATOR +
                    destDir + P2LibConst.LINE_SEPARATORx2 +
                    "ist nicht leer.");
            return false;
        }

        new Thread(() -> {
            BooleanProperty ret = new SimpleBooleanProperty(true);
            copyBackDataList.forEach(r -> {
                System.out.println("Backup kopieren: " + r.getFileName());
                try {
                    Path from = r.getFileData().getBackupFilePath();
                    String destStr = r.getFileData().getCorrParentFilePathStr();
                    Path dest = Path.of(destDir, destStr);
                    FileUtils.copyFileToDirectory(from.toFile(), dest.toFile(), true);
                } catch (IOException e) {
                    ret.set(false);
                    P2Log.errorLog(953254126, "copy file: " + r.getFileData().getBackupFilePathStr() +
                            " to " + destDir);
                }
            });
            if (!ret.get()) {
                P2AlertAppThread.showErrorAlert(stage, "Backup kopieren",
                        "Nicht alle Dateien des Backups konnten kopiert werden");
            }
        }).start();

        return true;
    }
}
