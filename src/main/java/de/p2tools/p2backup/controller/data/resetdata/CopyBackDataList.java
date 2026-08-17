package de.p2tools.p2backup.controller.data.resetdata;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class CopyBackDataList extends SimpleListProperty<CopyBackData> {
    public CopyBackDataList() {
        super(FXCollections.observableArrayList());
    }
}
