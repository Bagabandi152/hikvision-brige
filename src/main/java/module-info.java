module com.example.hikvisionbrige {
    requires javafx.controls;
    requires javafx.fxml;
    requires okhttp3;
    requires okhttp.digest;
    requires android.json;

    opens mn.sync.hikvisionbrige to javafx.fxml;
    exports mn.sync.hikvisionbrige;
}