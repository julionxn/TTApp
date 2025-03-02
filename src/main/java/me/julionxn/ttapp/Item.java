package me.julionxn.ttapp;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

import java.util.Map;
import java.util.function.Consumer;

public class Item {

    @Getter
    private final int id;
    private Rectangle rect;
    private final StackPane pane;
    private final Map<String, Consumer<Item>> actions;

    public Item(int id, Map<String, Consumer<Item>> actions) {
        this.id = id;
        String display = "Usuario: " + id;
        this.pane = buildStackPane(display);
        this.actions = actions;
    }

    public void changeColor(Paint color) {
        rect.setFill(color);
    }

    private StackPane buildStackPane(String display) {
        StackPane pane = new StackPane();
        pane.setPrefSize(250, 20);
        rect = new Rectangle(250, 20);
        rect.setFill(Color.LIGHTGRAY);
        Label label = new Label(display);
        pane.getChildren().addAll(rect, label);
        pane.setOnMouseClicked(e -> {
            showContextMenu(e.getScreenX(), e.getScreenY());
        });
        return pane;
    }

    private void showContextMenu(double x, double y) {
        ContextMenu contextMenu = new ContextMenu();

        for (Map.Entry<String, Consumer<Item>> entry : actions.entrySet()) {
            MenuItem item = new MenuItem(entry.getKey());
            item.setOnAction(e -> entry.getValue().accept(this));
            contextMenu.getItems().add(item);
        }

        contextMenu.show(pane, x, y);
    }

    public void place(AnchorPane pane, double x, double y) {
        this.pane.setLayoutX(x);
        this.pane.setLayoutY(y);
        pane.getChildren().add(this.pane);
    }

}
