package com.alwaleed.afx.navbar;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.*;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.HashMap;
import java.util.Map;


public class NavBarSkin extends SkinBase<NavBar> {
    private final ScrollPane scroller = new ScrollPane();
    private final VBox content = new VBox(8);
    private final HBox headerBar = new HBox(6);
    private final Button modeBtn = new Button();
    private final VBox root = new VBox(6); // header + scroller
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final Map<NavItem, ToggleButton> itemMap = new HashMap<>();
    private final Map<NavGroup, VBox> itemsBoxMap = new HashMap<>();
    private final Map<NavGroup, HBox> headerMap = new HashMap<>();
    private ContextMenu activeMenu;
    private double savedPrefWidth = -1;

    public NavBarSkin(NavBar control) {
        super(control);

        scroller.setFitToWidth(true);
        scroller.setFocusTraversable(false);
        scroller.getStyleClass().add("nav-scroller");
        scroller.setContent(content);

        content.setPadding(new Insets(8));
        content.getStyleClass().add("nav-content");

        headerBar.getStyleClass().add("nav-header");
        modeBtn.getStyleClass().add("mode-btn");
        updateModeButtonIcon(getSkinnable().isCollapsed());
        modeBtn.setOnAction(e -> getSkinnable().setCollapsed(!getSkinnable().isCollapsed()));
        
        placeHeaderButton(headerBar, modeBtn);
        getSkinnable().nodeOrientationProperty().addListener((o, ov, nv) -> placeHeaderButton(headerBar, modeBtn));
        getSkinnable().localeProperty().addListener((o, ov, nv) -> placeHeaderButton(headerBar, modeBtn));


        root.getChildren().addAll(headerBar, scroller);
        getChildren().add(root);

        control.getGroups().addListener((ListChangeListener<NavGroup>) c -> rebuild());
        control.compactProperty().addListener((obs, o, n) -> updateCompact(n));
        updateCompact(control.isCompact());

        control.selectedItemProperty().addListener((obs, old, val) -> syncSelection(val));
        control.collapsedProperty().addListener((obs, o, n) -> updateCollapsed(n));
        updateCollapsed(control.isCollapsed());

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
        itemsBoxMap.clear();
        headerMap.clear();
        for (NavGroup grp : getSkinnable().getGroups()) {
            content.getChildren().add(buildGroupView(grp));
        }
    }

    private Node buildGroupView(NavGroup grp) {
        VBox box = new VBox(6);
        box.getStyleClass().add("nav-group");

        HBox header = new HBox(8);
        header.getStyleClass().add("nav-group-header");
         Label arrow = new Label("^");
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
        
        header.getChildren().addAll(iconWrap, title, spacer, arrow);
        headerMap.put(grp, header);
 

        VBox itemsBox = new VBox(4);
        itemsBox.getStyleClass().add("nav-items");
        itemsBoxMap.put(grp, itemsBox);

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
            arrow.setRotate(180);
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
                new KeyValue(arrow.rotateProperty(), expand ? 180 : 0, Interpolator.EASE_BOTH)
            ),
            new KeyFrame(Duration.millis(180),
                new KeyValue(itemsBox.maxHeightProperty(), endH, Interpolator.EASE_BOTH),
                new KeyValue(itemsBox.opacityProperty(), expand ? 1 : 0, Interpolator.EASE_BOTH),
                new KeyValue(arrow.rotateProperty(), expand ? 0 : 180, Interpolator.EASE_BOTH)
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
    private void updateCollapsed(boolean collapsed) {
        var sk = getSkinnable();
        // style class
        if (collapsed) { if (!sk.getStyleClass().contains("collapsed")) sk.getStyleClass().add("collapsed"); }
        else { sk.getStyleClass().remove("collapsed"); }
        updateModeButtonIcon(collapsed);

    if (collapsed) {
        // remember user's prefWidth only if NOT bound (so we can restore later)
        if (savedPrefWidth < 0 && !sk.prefWidthProperty().isBound()) {
            savedPrefWidth = sk.getPrefWidth() > 0 ? sk.getPrefWidth() : 260;
        }
        double s = sk.getIconSize() + 16; // icon + padding
        sk.setMinWidth(s);
        sk.setMaxWidth(s);
        if (!sk.prefWidthProperty().isBound()) {
            sk.setPrefWidth(s);
        }
    } else {
        sk.setMinWidth(Region.USE_COMPUTED_SIZE);
        sk.setMaxWidth(Region.USE_COMPUTED_SIZE);
        if (!sk.prefWidthProperty().isBound()) {
            if (savedPrefWidth > 0) {
                sk.setPrefWidth(savedPrefWidth);
            } else {
                sk.setPrefWidth(Region.USE_COMPUTED_SIZE);
            }
        }
    }

        // group behavior
        headerMap.forEach((grp, header) -> {
            VBox itemsBox = itemsBoxMap.get(grp);
            if (collapsed) {
                itemsBox.setManaged(false);
                itemsBox.setVisible(false);
                header.setOnMouseClicked(e -> showGroupMenu(header, grp));
                attachHoverMenu(header, grp);
            } else {
                header.setOnMouseClicked(e -> grp.setExpanded(!grp.isExpanded()));
                header.setOnMouseEntered(null);
                header.setOnMouseExited(null);
                boolean exp = grp.isExpanded();
                itemsBox.setManaged(exp);
                itemsBox.setVisible(exp);
            }
        });
        if (activeMenu != null && collapsed == false) { activeMenu.hide(); activeMenu = null; }
    }

    private void showGroupMenu(Node owner, NavGroup grp) {
        if (activeMenu != null) activeMenu.hide();
        ContextMenu menu = new ContextMenu();
        for (NavItem it : grp.getItems()) {
            MenuItem mi = new MenuItem();
            mi.textProperty().bind(it.textProperty());
            if (it.getGraphic() instanceof ImageView iv && iv.getImage() != null) {
                ImageView g = new ImageView(iv.getImage());
                g.fitWidthProperty().bind(getSkinnable().iconSizeProperty());
                g.fitHeightProperty().bind(getSkinnable().iconSizeProperty());
                g.setPreserveRatio(true); g.setSmooth(true);
                mi.setGraphic(g);
            }
            mi.setOnAction(e -> getSkinnable().setSelectedItem(it));
            menu.getItems().add(mi);
        }
        Side side = getSkinnable().getNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT ? Side.LEFT : Side.RIGHT;
        menu.setOnHidden(e -> { if (activeMenu == menu) activeMenu = null; });
        menu.show(owner, side, 0, 0);
        activeMenu = menu;
    }

    private void attachHoverMenu(Node header, NavGroup grp) {
        PauseTransition pt = new PauseTransition(Duration.millis(200));
        header.setOnMouseEntered(e -> { pt.setOnFinished(x -> showGroupMenu(header, grp)); pt.playFromStart(); });
        header.setOnMouseExited(e -> pt.stop());
    }

    // header toggle placement: trailing (LTR) or leading (RTL)
    private void placeHeaderButton(HBox bar, Button btn) {
        bar.getChildren().clear();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        var eff = bar.getEffectiveNodeOrientation();
        if (eff == NodeOrientation.RIGHT_TO_LEFT) {
            bar.getChildren().addAll(spacer, btn);
        } else {
            bar.getChildren().addAll(btn, spacer);
        }
    }

    private void updateModeButtonIcon(boolean collapsed) {
        String name = collapsed ? "expand" : "collapse";
        ImageView iv = loadIcon(name);
        if (iv != null) { modeBtn.setGraphic(iv); modeBtn.setText(null); }
        else { modeBtn.setGraphic(null); modeBtn.setText(collapsed ? "⇤" : "⇥"); }
    }

    private ImageView loadIcon(String name) {
        var url = NavBar.class.getResource("/icons/" + name + ".png");
        if (url == null) return null;
        ImageView iv = new ImageView(new Image(url.toExternalForm()));
        iv.fitWidthProperty().bind(getSkinnable().iconSizeProperty());
        iv.fitHeightProperty().bind(getSkinnable().iconSizeProperty());
        iv.setPreserveRatio(true); iv.setSmooth(true);
        return iv;
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