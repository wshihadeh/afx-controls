package com.alwaleed.afx.navbar;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.*;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.image.ImageView;

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

        StackPane iconWrap = new StackPane();
        iconWrap.getStyleClass().add("group-icon");
        sizeIconSlot(iconWrap);
        if (grp.getGraphic() != null) {
            iconWrap.getChildren().setAll(grp.getGraphic());
            bindImageViewSizeIfNeeded(grp.getGraphic()); // NEW
        }
        grp.graphicProperty().addListener((obs, old, val) -> {
            iconWrap.getChildren().clear();
            if (val != null) {
                iconWrap.getChildren().add(val);
                bindImageViewSizeIfNeeded(val);
            }
        });

        Label title = new Label();
        title.getStyleClass().add("group-title");
        title.textProperty().bind(grp.titleProperty());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(arrow, iconWrap, title, spacer);

        VBox itemsBox = new VBox(4);
        itemsBox.getStyleClass().add("nav-items");

        // Initial state (no animation on first build)
        if (grp.isExpanded()) {
            itemsBox.setManaged(true);
            itemsBox.setVisible(true);
            itemsBox.setOpacity(1);
            itemsBox.setMaxHeight(Region.USE_COMPUTED_SIZE);
            arrow.setRotate(0);
        } else {
            itemsBox.setManaged(false);
            itemsBox.setVisible(false);
            itemsBox.setOpacity(0);
            itemsBox.setMaxHeight(0);
            arrow.setRotate(-90);
        }

        header.setOnMouseClicked(e -> grp.setExpanded(!grp.isExpanded()));

        grp.expandedProperty().addListener((obs, o, expand) -> animateExpanded(itemsBox, arrow, expand));

        grp.getItems().addListener((ListChangeListener<NavItem>) c -> rebuildItems(itemsBox, grp));
        rebuildItems(itemsBox, grp);

        box.getChildren().addAll(header, itemsBox);
        return box;
    }

    private void animateExpanded(VBox itemsBox, Label arrow, boolean expand) {
        // Ensure measured size is up-to-date
        double targetHeight = snapSize(itemsBox.prefHeight(-1));
        if (Double.isNaN(targetHeight) || targetHeight <= 0) {
            itemsBox.applyCss();
            itemsBox.layout();
            targetHeight = snapSize(itemsBox.prefHeight(-1));
        }

        itemsBox.setManaged(true);
        itemsBox.setVisible(true);

        double startH = expand ? 0 : itemsBox.getHeight();
        double endH = expand ? targetHeight : 0;

        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(itemsBox.maxHeightProperty(), startH, Interpolator.EASE_BOTH),
                new KeyValue(itemsBox.opacityProperty(), expand ? 0 : 1, Interpolator.EASE_BOTH),
                new KeyValue(arrow.rotateProperty(), expand ? -90 : 0, Interpolator.EASE_BOTH)
            ),
            new KeyFrame(Duration.millis(180),
                new KeyValue(itemsBox.maxHeightProperty(), endH, Interpolator.EASE_BOTH),
                new KeyValue(itemsBox.opacityProperty(), expand ? 1 : 0, Interpolator.EASE_BOTH),
                new KeyValue(arrow.rotateProperty(), expand ? 0 : -90, Interpolator.EASE_BOTH)
            )
        );

        boolean collapse = !expand;
        tl.setOnFinished(e -> {
            if (collapse) {
                itemsBox.setManaged(false);
                itemsBox.setVisible(false);
            } else {
                itemsBox.setMaxHeight(Region.USE_COMPUTED_SIZE);
            }
        });
        tl.play();
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

        StackPane iconWrap = new StackPane();
        iconWrap.getStyleClass().add("item-icon");
        sizeIconSlot(iconWrap);
        if (item.getGraphic() != null) {
            iconWrap.getChildren().setAll(item.getGraphic());
            bindImageViewSizeIfNeeded(item.getGraphic()); // NEW
        }
        item.graphicProperty().addListener((obs, old, val) -> {
            iconWrap.getChildren().clear();
            if (val != null) {
                iconWrap.getChildren().add(val);
                bindImageViewSizeIfNeeded(val); // NEW
            }
        });

        StackPane badge = new StackPane();
        badge.getStyleClass().add("badge");
        Label badgeLabel = new Label();
        badgeLabel.textProperty().bind(item.badgeProperty().asString());
        badge.getChildren().add(badgeLabel);
        badge.visibleProperty().bind(item.badgeProperty().greaterThan(0));
        badge.managedProperty().bind(badge.visibleProperty());

        HBox content = new HBox(8);
        content.getStyleClass().add("item-content");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        content.getChildren().addAll(iconWrap, text, spacer, badge);

        btn.setGraphic(content);
        btn.disableProperty().bind(item.disabledProperty());

        btn.selectedProperty().addListener((obs, was, isSel) -> {
            if (isSel) getSkinnable().setSelectedItem(item);
        });

        itemMap.put(item, btn);
        return btn;
    }
    private void sizeIconSlot(StackPane slot) {
        var szProp = getSkinnable().iconSizeProperty();
        // set initial size
        double s = szProp.get();
        slot.setMinSize(s, s);
        slot.setPrefSize(s, s);
        // update on changes
        szProp.addListener((o, old, val) -> {
            double nv = val.doubleValue();
            slot.setMinSize(nv, nv);
            slot.setPrefSize(nv, nv);
        });
    }

    private void bindImageViewSizeIfNeeded(Node n) {
        if (n instanceof ImageView iv) {
            iv.fitWidthProperty().bind(getSkinnable().iconSizeProperty());
            iv.fitHeightProperty().bind(getSkinnable().iconSizeProperty());
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
        }
    }
}