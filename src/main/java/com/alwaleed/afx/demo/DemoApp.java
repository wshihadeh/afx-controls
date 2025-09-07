package com.alwaleed.afx.demo;

import com.alwaleed.afx.navbar.*;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class DemoApp extends Application {
    private final NavBar navBar = new NavBar();

    @Override public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setCenter(buildContent());

        Scene scene = new Scene(root, 980, 640);
        stage.setScene(scene);
        stage.setTitle("AFX Controls — NavBar Demo");
        stage.show();

        // initial content
        populate(navBar.getLocale());
    }

    private VBox buildSidebar() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));

        // Language selector
        ComboBox<String> lang = new ComboBox<>();
        lang.getItems().addAll("English", "العربية");
        lang.getSelectionModel().select(navBar.getLocale().getLanguage().equals("ar") ? 1 : 0);
        lang.valueProperty().addListener((obs, old, val) -> {
            Locale locale = "العربية".equals(val) ? new Locale("ar") : Locale.ENGLISH;
            navBar.setLocale(locale);
            populate(locale);
        });

        // Compact toggle
        CheckBox compact = new CheckBox("Compact");
        navBar.compactProperty().bind(compact.selectedProperty());

        // Add to sidebar
        Label controls = new Label("Demo Controls");
        controls.getStyleClass().add("section-title");
        box.getChildren().addAll(controls, lang, compact, new Separator(), navBar);
        VBox.setVgrow(navBar, Priority.ALWAYS);
        return box;
    }

    private Pane buildContent() {
        StackPane center = new StackPane();
        center.setPadding(new Insets(24));

        Label selectedLabel = new Label();
        selectedLabel.getStyleClass().add("selected-label");
        selectedLabel.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    NavItem it = navBar.getSelectedItem();
                    return it == null ? "Select an item…" : ("Selected: " + it.getText());
                }, navBar.selectedItemProperty()));

        center.getChildren().add(selectedLabel);
        return center;
    }

    private void populate(Locale locale) {
        ResourceBundle rb = ResourceBundle.getBundle("i18n.messages", locale);

        navBar.getGroups().clear();

        NavGroup main = new NavGroup(rb.getString("group.main"));
        main.getItems().add(item("home", rb.getString("item.home")));
        main.getItems().add(item("search", rb.getString("item.search")));
        main.getItems().add(item("downloads", rb.getString("item.downloads")).withBadge(3));

        NavGroup tools = new NavGroup(rb.getString("group.tools"));
        tools.getItems().add(item("reports", rb.getString("item.reports")));
        tools.getItems().add(item("settings", rb.getString("item.settings")));

        navBar.getGroups().addAll(main, tools);
        navBar.setSelectedItem(main.getItems().get(0));
    }

    private NavItem item(String id, String text) {
        NavItem i = new NavItem(id, text);
        return i;
    }

    public static void main(String[] args) { launch(args); }
}