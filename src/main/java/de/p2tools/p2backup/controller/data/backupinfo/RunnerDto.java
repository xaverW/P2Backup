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
    private final BooleanProperty doneFirstRun = new SimpleBooleanProperty(false); // wird nach dem ersten Lauf gesetzt
    private final BooleanProperty goAlwaysOverError = new SimpleBooleanProperty(false); // Lesefehler überspringen
    private final BooleanProperty ask = new SimpleBooleanProperty(true); // bei Lesefehler fragen

    // Infos im GUI
    private final BooleanProperty guiRunning = new SimpleBooleanProperty(false); // wird im GUI ausgewertet
    private final IntegerProperty guiMax = new SimpleIntegerProperty(0);
    private final IntegerProperty guiDone = new SimpleIntegerProperty(0); // sind die erledigten
    private final IntegerProperty guiToDo = new SimpleIntegerProperty(0); // noch zu erledigen
    private final DoubleProperty guiProgress = new SimpleDoubleProperty(0);
    private final StringProperty guiText = new SimpleStringProperty(""); // ist der RunnerText im GUI
    private final StringProperty guiFileName = new SimpleStringProperty(""); // ist der Name der im Progress-Info angezeigt wird

    // Infos die angezeigt werden beim Lauf
    private final IntegerProperty runnerMax = new SimpleIntegerProperty(0); // Anzahl Dateien
    private final BooleanProperty runnerDouble = new SimpleBooleanProperty(false); // dann wird die doppelte Menge gemeldet
    private final IntegerProperty runnerAlreadyDone = new SimpleIntegerProperty(0); // schon fertig
    private final StringProperty runnerText = new SimpleStringProperty(""); // Text, was Runner macht
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
                if (runnerMax.get() != getGuiMax()) {
                    if (runnerDouble.get()) {
                        guiMax.set(runnerMax.get() / 2);
                    } else {
                        guiMax.set(runnerMax.get());
                    }
                    System.out.println("===> max " + guiMax.get());
                }

                if (runnerAlreadyDone.get() != getGuiDone()) {
                    if (runnerDouble.get()) {
                        guiDone.set(runnerAlreadyDone.get() / 2);
                    } else {
                        guiDone.set(runnerAlreadyDone.get());
                    }
                    System.out.println("===> done " + guiDone.get());
                    System.out.println("     max " + guiMax.get());
                    System.out.println("     progress " + guiProgress.get());
                }

                if (guiMax.get() > 0) {
                    guiProgress.set(1.0 * guiDone.get() / guiMax.get());
                    if (runnerDouble.get()) {
                        guiToDo.set(guiMax.get() / 2 - guiDone.get() / 2);
                    } else {
                        guiToDo.set(guiMax.get() - guiDone.get());
                    }
                } else {
                    guiProgress.set(0);
                    guiToDo.set(0);
                }

                if (!runnerText.get().equals(getGuiText())) {
                    guiText.set(runnerText.get());
                    System.out.println("===> text " + guiText.get());
                }

                if (!runnerFileName.get().equals(getGuiFileName())) {
                    guiFileName.set(runnerFileName.get());
                    System.out.println("===> fileName " + guiFileName.get());
                }
            }
        });
    }

    public void initRunner() {
        getDataFileList().clear();
        getDirFileList().clear();

        setDataSubPath("");
        setToPath(null);
        askProperty().set(true);
        goAlwaysOverErrorProperty().set(false);
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
        stop.set(false);
        runnerMax.set(0);
        runnerDouble.set(false);
        runnerAlreadyDone.set(0);
        runnerText.set(name);
        runnerFileName.set("");

        Platform.runLater(() -> {
            guiMax.set(0);
            guiDone.set(0);
            guiProgress.set(0);
            guiRunning.set(true);
            guiFileName.set("");
        });
    }

    public void stopRunner() {
        runnerMax.set(0);
        runnerDouble.set(false);
        runnerAlreadyDone.set(0);
        runnerText.set("");
        runnerFileName.set("");

        Platform.runLater(() -> {
            stop.set(false);
            guiProgress.set(0);
            guiRunning.set(false);
        });
    }

    public boolean getGuiRunning() {
        return guiRunning.get();
    }

    public BooleanProperty guiRunningProperty() {
        return guiRunning;
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

    public boolean getDoneFirstRun() {
        return doneFirstRun.get();
    }

    public void setDoneFirstRun(boolean doneFirstRun) {
        this.doneFirstRun.set(doneFirstRun);
    }

    public BooleanProperty doneFirstRunProperty() {
        return doneFirstRun;
    }

    // ============================
    public int getGuiMax() {
        return guiMax.get();
    }

    public IntegerProperty guiMaxProperty() {
        return guiMax;
    }

    public int getGuiDone() {
        return guiDone.get();
    }

    public IntegerProperty guiDoneProperty() {
        return guiDone;
    }

    public int getGuiToDo() {
        return guiToDo.get();
    }

    public IntegerProperty guiToDoProperty() {
        return guiToDo;
    }

    public double getGuiProgress() {
        return guiProgress.get();
    }

    public DoubleProperty guiProgressProperty() {
        return guiProgress;
    }

    public String getGuiText() {
        return guiText.get();
    }

    public StringProperty guiTextProperty() {
        return guiText;
    }

    public String getGuiFileName() {
        return guiFileName.get();
    }

    public StringProperty guiFileNameProperty() {
        return guiFileName;
    }

    // =====================
    // runner
    public void resetRunner() {
        this.runnerMax.set(0);
        this.runnerDouble.set(false);
        this.runnerAlreadyDone.set(0);
        this.runnerFileName.set("");
        this.runnerText.set("");
    }

    public void setRunnerMax(int runnerMax) {
        this.runnerMax.set(runnerMax);
        this.runnerAlreadyDone.set(0);
        this.runnerFileName.set("");
    }

    public boolean isRunnerDouble() {
        return runnerDouble.get();
    }

    public BooleanProperty runnerDoubleProperty() {
        return runnerDouble;
    }

    public void setRunnerAlreadyDone(int runnerAlreadyDone) {
        this.runnerAlreadyDone.set(runnerAlreadyDone);
    }

    public void addRunnerAlreadyDone() {
        this.runnerAlreadyDone.set(this.runnerAlreadyDone.get() + 1);
    }

    public void setRunnerText(String runnerText) {
        this.runnerText.set(runnerText);
    }

    public void setRunnerFileName(String runnerFileName) {
        this.runnerFileName.set(runnerFileName);
    }

    // =====================
    // Einlesefehler
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
