package mn.sync.hikvisionbrige.models;

import javafx.scene.Scene;

public class Loader {
    Scene scene;

    public Loader(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
