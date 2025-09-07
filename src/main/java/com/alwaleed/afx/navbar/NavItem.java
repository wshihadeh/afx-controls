package com.alwaleed.afx.navbar;

import javafx.beans.property.*;
import javafx.scene.Node;

public class NavItem {
    private final StringProperty id = new SimpleStringProperty(this, "id", "");
    private final StringProperty text = new SimpleStringProperty(this, "text", "");
    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, "graphic");
    private final IntegerProperty badge = new SimpleIntegerProperty(this, "badge", 0);
    private final BooleanProperty disabled = new SimpleBooleanProperty(this, "disabled", false);

    public NavItem() { }
    public NavItem(String id, String text) { setId(id); setText(text); }

    public final String getId() { return id.get(); }
    public final void setId(String value) { id.set(value); }
    public final StringProperty idProperty() { return id; }

    public final String getText() { return text.get(); }
    public final void setText(String value) { text.set(value); }
    public final StringProperty textProperty() { return text; }

    public final Node getGraphic() { return graphic.get(); }
    public final void setGraphic(Node value) { graphic.set(value); }
    public final ObjectProperty<Node> graphicProperty() { return graphic; }

    public final int getBadge() { return badge.get(); }
    public final void setBadge(int value) { badge.set(value); }
    public final IntegerProperty badgeProperty() { return badge; }

    public final boolean isDisabled() { return disabled.get(); }
    public final void setDisabled(boolean value) { disabled.set(value); }
    public final BooleanProperty disabledProperty() { return disabled; }

    public NavItem withBadge(int count) { setBadge(count); return this; }
}