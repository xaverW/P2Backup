package de.p2tools.p2backup.controller.data.filedata;

import de.p2tools.p2lib.tools.date.P2LDateTimeProperty;

import java.time.LocalDateTime;

public class HistoryFileData extends FileData {
    private P2LDateTimeProperty startDate = new P2LDateTimeProperty(LocalDateTime.MIN); // Startzeit Backup

    public LocalDateTime getStartDate() {
        return startDate.get();
    }

    public P2LDateTimeProperty startDateProperty() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate.set(startDate);
    }
}
