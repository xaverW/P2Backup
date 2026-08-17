package de.p2tools.p2backup.controller.data.resetdata;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class ResetDataList extends SimpleListProperty<ResetData> {
    public ResetDataList() {
        super(FXCollections.observableArrayList());
    }
}
