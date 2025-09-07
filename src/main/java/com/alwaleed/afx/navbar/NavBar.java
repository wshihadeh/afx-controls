package com.alwaleed.afx.navbar;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.geometry.NodeOrientation;

import java.util.Locale;
import java.util.Set;

public class NavBar extends Control {
    private final ObservableList<NavGroup> groups = FXCollections.observableArrayList();
    private final ObjectProperty<NavItem> selectedItem = new SimpleObjectProperty<>(this, "selectedItem");
    private final BooleanProperty compact = new SimpleBooleanProperty(this, "compact", false);
    private final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(this, "locale", Locale.getDefault());

    private static final Set<String> RTL_LANGS = Set.of("ar", "he", "fa", "ur");

    public NavBar() {
        getStyleClass().add("nav-bar");
        locale.addListener((obs, oldV, newV) -> applyLocale(newV));
        applyLocale(getLocale());
    }

    private void applyLocale(Locale loc) {
        boolean rtl = loc != null && RTL_LANGS.contains(loc.getLanguage());
        setNodeOrientation(rtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
        // Any internal localized strings would be (re)loaded here if needed.
    }

    public ObservableList<NavGroup> getGroups() { return groups; }

    public final NavItem getSelectedItem() { return selectedItem.get(); }
    public final void setSelectedItem(NavItem value) { selectedItem.set(value); }
    public final ObjectProperty<NavItem> selectedItemProperty() { return selectedItem; }

    public final boolean isCompact() { return compact.get(); }
    public final void setCompact(boolean value) { compact.set(value); }
    public final BooleanProperty compactProperty() { return compact; }

    public final Locale getLocale() { return locale.get(); }
    public final void setLocale(Locale value) { locale.set(value); }
    public final ObjectProperty<Locale> localeProperty() { return locale; }

    @Override protected Skin<?> createDefaultSkin() { return new NavBarSkin(this); }

    @Override public String getUserAgentStylesheet() {
        return NavBar.class.getResource("navbar.css").toExternalForm();
    }
}