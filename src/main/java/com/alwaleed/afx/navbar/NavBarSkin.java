package com.alwaleed.afx.navbar;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.Map;

public class NavBarSkin extends SkinBase<NavBar> {
    private final ScrollPane scroller = new ScrollPane();
    private final VBox content = new VBox(8);
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final Map<NavItem, ToggleButton> itemMap = new HashMap<>();

    public NavBarSkin(NavBar control) {
        super(control);

        scroller.setFitToWidth(true);
        scroller.setFocusTraversable(false);
        scroller.getStyleClass().add("nav-scroller");
        scroller.setContent(content);

        content.setPadding(new Insets(8));
        content.getStyleClass().add("nav-content");

        getChildren().add(scroller);

        control.getGroups().addListener((ListChangeListener<NavGroup>) c -> rebuild());
        control.compactProperty().addListener((obs, o, n) -> updateCompact(n));
        updateCompact(control.isCompact());

        control.selectedItemProperty().addListener((obs, old, val) -> syncSelection(val));

        rebuild();
    }

    private void updateCompact(boolean compact) {
        if (compact) {
            if (!getSkinnable().getStyleClass().contains("compact")) {
                getSkinnable().getStyleClass().add("compact");
            }
        } else {
            getSkinnable().getStyleClass().remove("compact");
        }
    }

    private void syncSelection(NavItem val) {
        itemMap.values().forEach(t -> t.setSelected(false));
        if (val != null) {
            ToggleButton t = itemMap.get(val);
            if (t != null) t.setSelected(true);
        }
    }

    private void rebuild() {
        content.getChildren().clear();
        itemMap.clear();
        for (NavGroup grp : getSkinnable().getGroups()) {
            content.getChildren().add(buildGroupView(grp));
        }
    }

    private Node buildGroupView(NavGroup grp) {
        VBox box = new VBox(6);
        box.getStyleClass().add("nav-group");

        HBox header = new HBox(8);
        header.getStyleClass().add("nav-group-header");
        Label arrow = new Label("▾");
        arrow.getStyleClass().add("chevron");
        Label title = new Label();
        title.getStyleClass().add("group-title");
        title.textProperty().bind(grp.titleProperty());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(arrow, title, spacer);
        header.setOnMouseClicked(e -> grp.setExpanded(!grp.isExpanded()));

        VBox itemsBox = new VBox(4);
        itemsBox.getStyleClass().add("nav-items");

        Runnable syncExpanded = () -> {
            boolean exp = grp.isExpanded();
            itemsBox.setManaged(exp);
            itemsBox.setVisible(exp);
            arrow.setRotate(exp ? 0 : -90);
        };
        syncExpanded.run();
        grp.expandedProperty().addListener((obs, o, n) -> syncExpanded.run());

        grp.getItems().addListener((ListChangeListener<NavItem>) c -> rebuildItems(itemsBox, grp));
        rebuildItems(itemsBox, grp);

        box.getChildren().addAll(header, itemsBox);
        return box;
    }

    private void rebuildItems(VBox itemsBox, NavGroup grp) {
        itemsBox.getChildren().clear();
        for (NavItem item : grp.getItems()) {
            itemsBox.getChildren().add(buildItemNode(item));
        }
    }

    private Node buildItemNode(NavItem item) {
        ToggleButton btn = new ToggleButton();
        btn.getStyleClass().add("nav-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setToggleGroup(toggleGroup);

        Label text = new Label();
        text.getStyleClass().add("item-text");
        text.textProperty().bind(item.textProperty());

        StackPane badge = new StackPane();
        badge.getStyleClass().add("badge");
        Label badgeLabel = new Label();
        badgeLabel.textProperty().bind(item.badgeProperty().asString());
        badge.getChildren().add(badgeLabel);
        badge.visibleProperty().bind(item.badgeProperty().greaterThan(0));
        badge.managedProperty().bind(badge.visibleProperty());

        HBox content = new HBox(8);
        content.getStyleClass().add("item-content");

        if (item.getGraphic() != null) {
            content.getChildren().add(item.getGraphic());
        }
        item.graphicProperty().addListener((obs, old, val) -> {
            if (old != null) content.getChildren().remove(old);
            if (val != null) content.getChildren().add(0, val);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        content.getChildren().addAll(text, spacer, badge);

        btn.setGraphic(content);
        btn.disableProperty().bind(item.disabledProperty());

        btn.selectedProperty().addListener((obs, was, isSel) -> {
            if (isSel) getSkinnable().setSelectedItem(item);
        });

        itemMap.put(item, btn);
        return btn;
    }
}