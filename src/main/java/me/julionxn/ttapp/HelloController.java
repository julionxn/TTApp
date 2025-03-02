package me.julionxn.ttapp;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import me.julionxn.ttapp.endpoint.EndpointsManager;
import me.julionxn.ttapp.endpoint.model.Product;
import me.julionxn.ttapp.endpoint.model.Stops;
import me.julionxn.ttapp.endpoint.model.User;
import me.julionxn.ttapp.excel.ExcelGenerator;
import me.julionxn.ttapp.util.DatesUtil;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class HelloController implements Initializable {

    @FXML private AnchorPane root;
    @FXML private AnchorPane queuePane;
    @FXML private AnchorPane attendancePane;
    @FXML private AnchorPane preparationQueue;
    @FXML private AnchorPane cashQueue;
    
    private final int margin = 3;

    private int currentId = 0;
    private final HashMap<Integer, User> usersInSystem = new HashMap<>();

    private QueueManager queue;
    private QueueManager attendance;
    private QueueManager preparation;
    private QueueManager cash;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.queue = new QueueManager(queuePane);
        this.attendance = new QueueManager(attendancePane);
        this.preparation = new QueueManager(preparationQueue);
        this.cash = new QueueManager(cashQueue);
    }

    private final Map<String, Consumer<Item>> queueActions = new HashMap<>(){{
        put("Comienza atención", HelloController.this::startAttendance);
        put("Salida", HelloController.this::end);
    }};

    private final Map<String, Consumer<Item>> attendanceActions = new HashMap<>(){{
        put("Producción", HelloController.this::startProduction);
        put("Elote", item -> addProduct(item, Product.ELOTE));
        put("Esquite", item -> addProduct(item, Product.ESQUITE));
        put("Charola", item -> addProduct(item, Product.CHAROLA));
        put("Preparación", HelloController.this::startPreparation);
        put("Pago", HelloController.this::startCash);
        put("Listo", HelloController.this::endAttendance);
    }};

    private void startProduction(Item item){
        item.changeColor(Color.PALEVIOLETRED);
        int id = item.getId();
        User currentUser = usersInSystem.get(id);
        currentUser.setTimeStartProduction(DatesUtil.now());
    }

    private final Map<String, Consumer<Item>> cashActions = new HashMap<>(){{
        put("Preparación", HelloController.this::startPreparation);
        put("Listo", HelloController.this::endCash);
        put("Salida", HelloController.this::end);
    }};

    private final Map<String, Consumer<Item>> preparationActions = new HashMap<>(){{
        put("Pago", HelloController.this::startCash);
        put("Listo", HelloController.this::endPreparation);
        put("Salida", HelloController.this::end);
    }};

    private void startCash(Item item) {
        int id = item.getId();
        User currentUser = usersInSystem.get(id);
        currentUser.getFlow().add(Stops.CASH);
        currentUser.setTimeStartCash(DatesUtil.now());
        Item aItem = new Item(id, cashActions);
        cash.addItem(aItem);
    }

    private void endCash(Item item) {
        int id = item.getId();
        User currentUser = usersInSystem.get(id);
        Long current = DatesUtil.now();
        Long preparationStart = currentUser.getTimeStartPreparation();
        if (preparationStart != null && current - preparationStart <= 3){
            currentUser.setTimeEndCash(preparationStart);
        } else {
            currentUser.setTimeEndCash(current);
        }
        
        cash.removeItem(item);
    }

    private void endPreparation(Item item) {
        int id = item.getId();
        User currentUser = usersInSystem.get(id);
        Long cashStart = currentUser.getTimeStartCash();
        Long current = DatesUtil.now();
        if (cashStart != null && current - cashStart <= margin){
            currentUser.setTimeEndPreparation(cashStart);
        } else {
            currentUser.setTimeEndPreparation(current);
        }
        preparation.removeItem(item);
    }

    private void startPreparation(Item item) {
        int id = item.getId();
        User currentUser = usersInSystem.get(id);
        currentUser.getFlow().add(Stops.PREPARATION);
        currentUser.setTimeStartPreparation(DatesUtil.now());
        Item aItem = new Item(id, preparationActions);
        preparation.addItem(aItem);
    }

    private void endAttendance(Item item) {
        System.out.println("===== On end attendance =====");
        int id = item.getId();
        User user = usersInSystem.get(id);
        Long startPreperation = user.getTimeStartPreparation();
        Long startCash = user.getTimeStartCash();
        Long current = DatesUtil.now();
        Long lastProduct = user.getProducts().keySet().stream()
                .max(Long::compare).orElse(null);

        if (startPreperation != null && current - startPreperation <= margin){
            user.setTimeEndProductGive(startPreperation);
            System.out.println("Setting same time as preparation");
        } else if (startCash != null && current - startCash <= margin) {
            user.setTimeEndProductGive(startCash);
            System.out.println("Setting same time as cash");
        } else {
            if (lastProduct != null && current - lastProduct <= margin){
                user.setTimeEndProductGive(lastProduct);
                System.out.println("Setting same time as last product");
            } else {
                user.setTimeEndProductGive(current);
                System.out.println("Setting current time");
            }
        }
        attendance.removeItem(item);
    }

    private void addProduct(Item item, Product product) {
        int id = item.getId();
        User currentUser = usersInSystem.get(id);
        if (currentUser.getProducts().isEmpty()){
            currentUser.setTimeStartProductGive(DatesUtil.now());
        }
        currentUser.getFlow().add(Stops.GIVE);
        currentUser.getProducts().put(DatesUtil.now(), product);
    }

    @FXML
    private void newQueue() {
        User newUser = new User();
        newUser.setTimeArrival(DatesUtil.now());
        System.out.println(DatesUtil.now());
        newUser.getFlow().add(Stops.QUEUE);
        usersInSystem.put(currentId, newUser);
        addUserToQueue();
    }

    private void addUserToQueue() {
        Item item = new Item(currentId, queueActions);
        queue.addItem(item);
        currentId++;
    }

    private void end(Item item){
        int id = item.getId();
        User currentUser = usersInSystem.get(id);
        Long current = DatesUtil.now();
        currentUser.setEndTime(current);
        Long preparationStart = currentUser.getTimeStartPreparation();
        Long preparationEnd = currentUser.getTimeEndPreparation();
        Long cashStart = currentUser.getTimeStartCash();
        Long cashEnd = currentUser.getTimeEndCash();
        if (preparationStart != null && preparationEnd == null){
            currentUser.setTimeEndPreparation(current);
        }
        if (cashStart != null && cashEnd == null){
            currentUser.setTimeEndCash(current);
        }
        System.out.println("END");
        cleanItems(id);
    }

    private void cleanItems(int id){
        System.out.println(id);
        queue.removeItem(id);
        attendance.removeItem(id);
        preparation.removeItem(id);
        cash.removeItem(id);
        User currentUser = usersInSystem.get(id);
        EndpointsManager.getInstance().USERS.postItem(currentUser, System.out::println)
                .thenAccept(res -> usersInSystem.remove(id));
    }

    private void startAttendance(Item item) {
        int id = item.getId();
        User user = usersInSystem.get(id);
        user.setTimeAttendance(DatesUtil.now());
        user.getFlow().add(Stops.ATTENDANCE);
        queue.removeItem(item);
        Item aItem = new Item(id, attendanceActions);
        attendance.addItem(aItem);
    }

    @FXML
    private void gen(){
        ExcelGenerator gen = new ExcelGenerator();
        gen.build();
    }

}