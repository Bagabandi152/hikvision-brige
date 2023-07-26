module com.example.hikvisionbrige {
    requires javafx.controls;
    requires javafx.fxml;
    requires okhttp3;
    requires okhttp.digest;
    requires org.json;
    requires log4j.api;
    requires log4j.core;

    opens mn.sync.hikvisionbrige to javafx.fxml;
    opens mn.sync.hikvisionbrige.models to javafx.base;
    exports mn.sync.hikvisionbrige;
}