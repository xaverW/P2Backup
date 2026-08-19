package de.p2tools.p2backup.controller.runner.copyrunner;

import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.backupinfo.BackupInfo;
import de.p2tools.p2backup.controller.data.resetdata.CopyBackData;
import de.p2tools.p2backup.controller.data.resetdata.CopyBackDataList;
import de.p2tools.p2backup.controller.sqlite.SqlCopyBackData;
import de.p2tools.p2lib.P2LibConst;
import de.p2tools.p2lib.alert.P2Alert;
import de.p2tools.p2lib.alert.P2AlertAppThread;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.tools.log.P2Log;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CopyBackFactory {
    public CopyBackFactory() {
    }

    public static boolean getCopyBackDataList(BackupInfo backupInfo, BackupData backupData, CopyBackDataList copyBackDataList) {
        List<BackupData> list = new ArrayList<>();
        HashMap<String, CopyBackData> hashMap = new HashMap<>();

        backupInfo.getBackupDataList().forEach(ba -> {
            if (ba.getId() >= backupData.getId()) {
                list.add(ba);
            }
        });

        list.sort(Comparator.reverseOrder()); // beim Einfügen wird der alte Wert überschrieben!!!
        list.forEach(ba -> {
            CopyBackDataList reset = new CopyBackDataList();
            SqlCopyBackData.getCopyBackData(backupInfo, ba, reset);
            reset.forEach(r -> hashMap.put(r.getFileData().getFilePathStr(), r));
        });

        copyBackDataList.setAll(hashMap.values());
        return true;
    }

    public static boolean copyBackBackup(Stage stage, BackupInfo backupInfo, CopyBackDataList copyBackDataList, String destDir) {
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

        backupInfo.runnerDto.startRunner(backupInfo.getName());
        backupInfo.runnerDto.setRunnerMax(copyBackDataList.getSize());
        new Thread(() -> {
            ProgData.getInstance().pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
            boolean ask = false;
            for (CopyBackData r : copyBackDataList) {
                System.out.println("Backup kopieren: " + r.getFileName());
                if (backupInfo.runnerDto.isStop()) {
                    break;
                }

                try {
                    Path from = r.getFileData().getBackupFilePath();
                    String destStr = r.getFileData().getCorrParentFilePathStr();
                    Path dest = Path.of(destDir, destStr);
                    backupInfo.runnerDto.setRunnerFileName(r.getFileName());
                    backupInfo.runnerDto.addRunnerAlreadyDone();

                    FileUtils.copyFileToDirectory(from.toFile(), dest.toFile(), true);
                } catch (IOException e) {
                    if (!ask) {
                        if (P2Alert.BUTTON.YES.equals(P2AlertAppThread.showAlert_yes_no(stage, "Backup", "Backup kopieren",
                                "Die Datei:" +
                                        "\n\n" + r.getFileName() + "\n\n" +
                                        "konnte nicht kopiert werden. Soll auch bei weiteren Fehlern " +
                                        "weiter gemacht werden?"))) {
                            ask = true;
                        } else {
                            break;
                        }
                    }
                    P2Log.errorLog(953254126, "copy file: " + r.getFileData().getBackupFilePathStr() +
                            " to " + destDir);
                }
            }

            backupInfo.runnerDto.stopRunner();
            ProgData.getInstance().pEventHandler.notifyListener(new P2Event(PEvents.EVENT_RUNNER_RUN));
        }).start();

        return true;
    }
}