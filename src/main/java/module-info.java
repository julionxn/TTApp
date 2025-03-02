module me.julionxn.ttapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.net.http;
    requires static lombok;
    requires org.apache.poi.ooxml;


    opens me.julionxn.ttapp to javafx.fxml;
    exports me.julionxn.ttapp;
    opens me.julionxn.ttapp.endpoint.model;
    exports me.julionxn.ttapp.endpoint.model;
}