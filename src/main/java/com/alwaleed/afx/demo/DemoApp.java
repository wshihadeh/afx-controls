package com.alwaleed.afx.demo;

import com.alwaleed.afx.navbar.*;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class DemoApp extends Application {
    private final NavBar navBar = new NavBar();
    private SplitPane rightSplit; // top: controls, bottom: demo

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        rightSplit = buildRightSplit();   // hidden until user selects "NavBar" in the tree
        rightSplit.setVisible(false);

        root.setLeft(buildExplorer());
        root.setCenter(rightSplit);

        Scene scene = new Scene(root, 980, 640);
        stage.setScene(scene);
        stage.setTitle("AFX Controls — NavBar Demo");
        stage.show();

        // Initial model content (so when user clicks “NavBar” it’s ready)
        populate(navBar.getLocale());
    }

    // LEFT: explorer (TreeView) with one item: NavBar
    private Region buildExplorer() {
        var rootItem = new TreeItem<>("Controls");
        rootItem.setExpanded(true);

        var navBarNode = new TreeItem<>("NavBar");
        rootItem.getChildren().add(navBarNode);

        TreeView<String> tree = new TreeView<>(rootItem);
        tree.setShowRoot(false);
        tree.setMinWidth(180);
        tree.setPrefWidth(220);

        tree.getSelectionModel().selectedItemProperty().addListener((obs, old, it) -> {
            if (it != null && "NavBar".equals(it.getValue())) {
                rightSplit.setVisible(true);
            }
        });

        VBox box = new VBox(tree);
        box.setPadding(new Insets(10));
        return box;
    }

    // RIGHT (container): vertical split — top controls / bottom demo
    private SplitPane buildRightSplit() {
        SplitPane sp = new SplitPane();
        sp.setOrientation(Orientation.VERTICAL);
        sp.getItems().addAll(buildControlsPane(), buildNavBarDemo());
        sp.setDividerPositions(0.15); // ~35% controls, 65% demo
        return sp;
    }

    // TOP (right side): controls only
    private VBox buildControlsPane() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));

        Label header = new Label("Demo Controls");
        header.getStyleClass().add("section-title");

        // Language selector
        ComboBox<String> lang = new ComboBox<>();
        lang.getItems().addAll("English", "العربية");
        lang.getSelectionModel().select(navBar.getLocale().getLanguage().equals("ar") ? 1 : 0);
        lang.valueProperty().addListener((obs, old, val) -> {
            Locale locale = "العربية".equals(val) ? new Locale("ar") : Locale.ENGLISH;
            navBar.setLocale(locale);
            populate(locale);
        });
        
        Label wLbl = new Label("NavBar width");
        Slider w = new Slider(100, 1200, navBar.getPrefWidth());
        navBar.prefWidthProperty().bind(w.valueProperty());
        box.getChildren().addAll(wLbl, w);

        w.setValue(300);


        // Compact toggle
        CheckBox compact = new CheckBox("Compact");
        navBar.compactProperty().bind(compact.selectedProperty());

        // Icon size slider (binds to NavBar.iconSize)
        Label iconSizeLbl = new Label("Icon size");
        Slider iconSize = new Slider(40, 200, navBar.getIconSize());
        iconSize.setShowTickMarks(true);
        iconSize.setMajorTickUnit(4);
        iconSize.setMinorTickCount(3);
        iconSize.setBlockIncrement(1);
        navBar.iconSizeProperty().bindBidirectional(iconSize.valueProperty());

        box.getChildren().addAll(header, lang, compact, iconSizeLbl, iconSize, new Separator());
        return box;
    }

    // BOTTOM (right side): the actual NavBar demo (NavBar on the left, content in center)
    private Pane buildNavBarDemo() {
        BorderPane demo = new BorderPane();
        demo.setPadding(new Insets(10));

        // Put the NavBar at the left side of the demo area
        demo.setLeft(navBar);

        BorderPane.setMargin(navBar, new Insets(0, 10, 0, 0));

        // A simple content area that shows the selected item text
        StackPane content = new StackPane();
        content.setPadding(new Insets(24));
        Label selectedLabel = new Label();
        selectedLabel.getStyleClass().add("selected-label");
        selectedLabel.textProperty().bind(Bindings.createStringBinding(
            () -> {
                NavItem it = navBar.getSelectedItem();
                return it == null ? "Select an item…" : ("Selected: " + it.getText());
            },
            navBar.selectedItemProperty()
        ));
        content.getChildren().add(selectedLabel);

        demo.setCenter(content);
        return demo;
    }

    // Builds demo data based on the active locale
    private void populate(Locale locale) {
        ResourceBundle rb = ResourceBundle.getBundle("i18n.messages", locale);

        navBar.getGroups().clear();

        NavGroup main = new NavGroup(rb.getString("group.main"));
        main.setGraphic(icon("home")); // group header icon
        main.getItems().add(item("home", rb.getString("item.home"), "home"));
        main.getItems().add(item("search", rb.getString("item.search"), "search"));
        main.getItems().add(item("downloads", rb.getString("item.downloads"), "downloads").withBadge(3));

        NavGroup tools = new NavGroup(rb.getString("group.tools"));
        tools.setGraphic(icon("settings"));
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
     * Load an icon from src/main/resources/icons/{name}.png and bind size to navBar.iconSize.
     */
    private ImageView icon(String name) {
        URL url = getClass().getResource("/icons/" + name + ".png");
        ImageView iv = new ImageView();
        iv.setPreserveRatio(true);
        iv.setSmooth(true);

        if (url != null) {
            iv.setImage(new Image(url.toExternalForm()));
        }
        // Bind to NavBar.iconSize so the slider affects both group and item icons
        iv.fitWidthProperty().bind(navBar.iconSizeProperty());
        iv.fitHeightProperty().bind(navBar.iconSizeProperty());
        return iv;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
