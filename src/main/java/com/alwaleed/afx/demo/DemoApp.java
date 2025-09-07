package com.alwaleed.afx.demo;

import com.alwaleed.afx.navbar.*;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class DemoApp extends Application {
    private final NavBar navBar = new NavBar();

    @Override public void start(Stage stage) {
        BorderPane root = new BorderPane();

        SplitPane split = new SplitPane();
        split.getItems().addAll(buildSidebar(), buildContent());
        split.setDividerPositions(0.28);
        root.setCenter(split);

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

        box.setMinWidth(160);
        box.setPrefWidth(260);

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

        // NEW: Icon size slider (12..40 px)
        Label iconSizeLbl = new Label("Icon size");
        Slider iconSize = new Slider(60, 120, navBar.getIconSize());
        iconSize.setShowTickMarks(true);
        iconSize.setMajorTickUnit(4);
        iconSize.setMinorTickCount(3);
        iconSize.setBlockIncrement(1);
        navBar.iconSizeProperty().bindBidirectional(iconSize.valueProperty());

        Label controls = new Label("Demo Controls");
        controls.getStyleClass().add("section-title");
        box.getChildren().addAll(controls, lang, compact, iconSizeLbl, iconSize, new Separator(), navBar);
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
        main.setGraphic(icon("home")); // group header icon (image)
        main.getItems().add(item("home", rb.getString("item.home"), "home"));
        main.getItems().add(item("search", rb.getString("item.search"), "search"));
        main.getItems().add(item("downloads", rb.getString("item.downloads"), "downloads").withBadge(3));

        NavGroup tools = new NavGroup(rb.getString("group.tools"));
        tools.setGraphic(icon("settings")); // group header icon (image)
        tools.getItems().add(item("reports", rb.getString("item.reports"), "reports"));
        tools.getItems().add(item("settings", rb.getString("item.settings"), "settings"));

        navBar.getGroups().addAll(main, tools);
        navBar.setSelectedItem(main.getItems().get(0));
    }

    private NavItem item(String id, String text, String iconName) {
        NavItem i = new NavItem(id, text);
        i.setGraphic(icon(iconName));
        return i;
    }

    /**
     * Loads a 18x18 icon from src/main/resources/icons/{name}.png, with fallback emoji when missing.
     * If you want RTL mirroring for directional icons, uncomment the scaleX binding below.
     */
    private ImageView icon(String name) {
        URL url = getClass().getResource("/icons/" + name + ".png");
        if (url == null) {
            // Fallback: simple emoji label-as-image for dev visibility
            ImageView iv = new ImageView();
            iv.setPreserveRatio(true);
            return iv; // empty — you can also return new Label("⬜") if preferred
        }
        ImageView iv = new ImageView(new Image(url.toExternalForm()));
        // Bind to NavBar.iconSize so user can adjust from the demo
        iv.fitWidthProperty().bind(navBar.iconSizeProperty());
        iv.fitHeightProperty().bind(navBar.iconSizeProperty());
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        // Optional RTL mirroring for directional icons:
        // iv.scaleXProperty().bind(Bindings.when(navBar.nodeOrientationProperty()
        //         .isEqualTo(NodeOrientation.RIGHT_TO_LEFT)).then(-1.0).otherwise(1.0));
        return iv;
    }

    public static void main(String[] args) { launch(args); }
}
