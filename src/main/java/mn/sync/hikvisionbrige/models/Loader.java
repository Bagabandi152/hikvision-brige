package mn.sync.hikvisionbrige.models;

import javafx.scene.Scene;

public class Loader {
    Scene scene;
    volatile Thread thread;

    public Loader(Scene scene, Thread thread) {
        this.scene = scene;
        this.thread = thread;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
}
