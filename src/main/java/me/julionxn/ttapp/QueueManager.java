package me.julionxn.ttapp;

import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;

public class QueueManager {

    private final AnchorPane root;
    private final List<Item> items = new ArrayList<>();

    public QueueManager(AnchorPane root) {
        this.root = root;
    }

    public void addItem(Item item) {
        items.add(item);
        repaint();
    }

    public void removeItem(Item item) {
        int id = item.getId();
        items.removeIf(i -> i.getId() == id);
        repaint();
    }

    public void removeItem(int id){
        items.removeIf(i -> i.getId() == id);
        repaint();
    }

    private void repaint(){
        root.getChildren().clear();
        double y = 0;
        for (Item item : items) {
            item.place(root, 0, y);
            y += 20;
        }
    }

}
