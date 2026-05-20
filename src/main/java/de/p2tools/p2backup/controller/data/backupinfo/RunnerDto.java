package de.p2tools.p2backup.controller.data.backupinfo;

import de.p2tools.p2backup.controller.config.PEvents;
import de.p2tools.p2backup.controller.config.ProgData;
import de.p2tools.p2backup.controller.data.backupdata.BackupData;
import de.p2tools.p2backup.controller.data.filedata.FileDataList;
import de.p2tools.p2lib.p2event.P2Event;
import de.p2tools.p2lib.p2event.P2Listener;
import javafx.application.Platform;
import javafx.beans.property.*;

import java.nio.file.Path;

public class RunnerDto {
    private final BooleanProperty stop = new SimpleBooleanProperty(false); // wird in den nur in Running-Threads verwendet!!
    private final BooleanProperty ok = new SimpleBooleanProperty(false); // war der Lauf fehlerfrei
    private final BooleanProperty firstRun = new SimpleBooleanProperty(false); // wird nach dem ersten Lauf gesetzt
    private final IntegerProperty max = new SimpleIntegerProperty(0);
    private final IntegerProperty done = new SimpleIntegerProperty(0);
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final BooleanProperty running = new SimpleBooleanProperty(false); // wird im GUI ausgewertet
    private final StringProperty text = new SimpleStringProperty("");
    private final BooleanProperty goAlwaysOverError = new SimpleBooleanProperty(false);
    private final BooleanProperty ask = new SimpleBooleanProperty(false);

    // Infos die angezeigt werden beim Lauf
    private final StringProperty fileName = new SimpleStringProperty(""); // ist der Name der im Progress-Info angezeigt wird
    private final IntegerProperty runnerMax = new SimpleIntegerProperty(0); // Anzahl Dateien
    private final IntegerProperty runnerDone = new SimpleIntegerProperty(0); // schon fertig
    private final StringProperty runnerText = new SimpleStringProperty("");
    private final StringProperty runnerFileName = new SimpleStringProperty(""); // aktuelle Datei (zum Erstellen des Hash)

    // backupPath   /tmp/usb/backup
    // subPath      /2025-10-23__10-12-00
    // toPath       /tmp/usb/backup/2025-10-23__10-12-00
    // filePath     /home/emil/Desktop/daten/file1/1970/1960_05.jpg

    private BackupData backupData = new BackupData();

    private String dataSubPath = "";
    private Path toPath = null; // aktuelle Pfad zum Backup

    private final FileDataList dirFileList = new FileDataList(); // sind die FileData der Daten (Directory) -> gibt leere!
    private final FileDataList dataFileList = new FileDataList(); // sind die FileData der Daten (File)

    public RunnerDto() {
        ProgData.getInstance().pEventHandler.addListener(new P2Listener(PEvents.EVENT_TIMER_HALF_SECOND) {
            @Override
            public void pingGui(P2Event event) {
                if (runnerMax.get() != getMax()) {
                    max.set(runnerMax.get());
                    System.out.println("===> max " + max.get());
                }

                if (runnerDone.get() != getDone()) {
                    done.set(runnerDone.get());
                    if (max.get() > 0) {
                        progress.set(1.0 * done.get() / max.get());
                    }
                    System.out.println("===> done " + done.get());
                    System.out.println("     max " + max.get());
                    System.out.println("     progress " + progress.get());
                }

                if (!runnerText.get().equals(getText())) {
                    text.set(runnerText.get());
                    System.out.println("===> text " + text.get());
                }

                if (!runnerFileName.get().equals(getFileName())) {
                    fileName.set(runnerFileName.get());
                    System.out.println("===> fileName " + fileName.get());
                }
            }
        });
    }


    //===============
    public BackupData getBackupData() {
        return backupData;
    }

    public void setBackupData(BackupData backupData) {
        this.backupData = backupData;
    }

    public String getDataSubPath() {
        return dataSubPath;
    }

    public void setDataSubPath(String dataSubPath) {
        this.dataSubPath = dataSubPath;
    }

    public Path getToPath() {
        return toPath;
    }

    public void setToPath(Path toPath) {
        this.toPath = toPath;
    }

    public FileDataList getDirFileList() {
        // ist die Liste der Daten DIRs
        return dirFileList;
    }

    public FileDataList getDataFileList() {
        // ist die Liste der Daten-Files
        return dataFileList;
    }

    //===============
    public void startRunner(String name) {
        runnerMax.set(0);
        runnerDone.set(0);
        runnerText.set(name);
        runnerFileName.set("");

        Platform.runLater(() -> {
            stop.set(false);
            progress.set(0);
            running.set(true);
//            text.set(name);
        });
    }

    public void stopRunner() {
        runnerMax.set(0);
        runnerDone.set(0);
        runnerText.set("");
        runnerFileName.set("");

        Platform.runLater(() -> {
            stop.set(false);
            progress.set(0);
            running.set(false);
        });
    }

    public boolean isRunning() {
        return running.get();
    }

    public BooleanProperty runningProperty() {
        return running;
    }

    public boolean isStop() {
        return stop.get();
    }

    public void setStop() {
        this.stop.set(true);
    }

    public BooleanProperty stopProperty() {
        return stop;
    }

    public boolean isOk() {
        return ok.get();
    }

    public void setOk(boolean ok) {
        this.ok.set(ok);
    }

    public boolean isFirstRun() {
        return firstRun.get();
    }

    public void setFirstRun(boolean firstRun) {
        this.firstRun.set(firstRun);
    }

    public BooleanProperty firstRunProperty() {
        return firstRun;
    }

    // ============================
    public int getMax() {
        return max.get();
    }

    public IntegerProperty maxProperty() {
        return max;
    }

    public int getDone() {
        return done.get();
    }

    public IntegerProperty doneProperty() {
        return done;
    }

    public double getProgress() {
        return progress.get();
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    // =====================
    // runner
    public void setRunnerMax(int runnerMax) {
        this.runnerMax.set(runnerMax);
    }

    public void setRunnerDone(int runnerDone) {
        this.runnerDone.set(runnerDone);
    }

    public void setRunnerText(String runnerText) {
        this.runnerText.set(runnerText);
    }

    public void setRunnerFileName(String runnerFileName) {
        this.runnerFileName.set(runnerFileName);
    }

    public boolean getGoAlwaysOverError() {
        return goAlwaysOverError.get();
    }

    public BooleanProperty goAlwaysOverErrorProperty() {
        return goAlwaysOverError;
    }

    public boolean isAsk() {
        return ask.get();
    }

    public BooleanProperty askProperty() {
        return ask;
    }
}
