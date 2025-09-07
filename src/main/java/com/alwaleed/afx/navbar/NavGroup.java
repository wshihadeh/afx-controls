package com.alwaleed.afx.navbar;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class NavGroup {
    private final StringProperty title = new SimpleStringProperty(this, "title", "");
    private final ObservableList<NavItem> items = FXCollections.observableArrayList();
    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded", true);

    public NavGroup() { }
    public NavGroup(String title) { setTitle(title); }

    public final String getTitle() { return title.get(); }
    public final void setTitle(String value) { title.set(value); }
    public final StringProperty titleProperty() { return title; }

    public final ObservableList<NavItem> getItems() { return items; }

    public final boolean isExpanded() { return expanded.get(); }
    public final void setExpanded(boolean value) { expanded.set(value); }
    public final BooleanProperty expandedProperty() { return expanded; }
}
