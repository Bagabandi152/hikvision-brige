package mn.sync.hikvisionbrige.constants;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import mn.sync.hikvisionbrige.holders.LoadingHolder;
import mn.sync.hikvisionbrige.models.Loader;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class Components {

    public abstract void draw();

    public static HBox getSeparatorWithLabel(String text) {
        // Create a label
        Label label = new Label(text);
        label.setPrefWidth(135);
        label.setAlignment(Pos.CENTER);
        label.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 13.5));

        // Create a horizontal separator
        Line sepLeft = new Line(0, 0, 80, 0);
        sepLeft.getStrokeDashArray().addAll(1.0, 5.0);
        Line sepRight = new Line(0, 0, 80, 0);
        sepRight.getStrokeDashArray().addAll(1.0, 5.0);

        // Place them in an HBox
        HBox hbox = new HBox(sepLeft, label, sepRight);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setSpacing(10);
        return hbox;
    }

    public static void getSpinningLoader(Stage stage, Boolean loading) {

        LoadingHolder loadingHolder = LoadingHolder.getInstance();
        if (loading) {
            loadingHolder.setLoader(new Loader(stage.getScene(), new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            })));
        }

        Loader loader = loadingHolder.getLoader();
        System.out.println("Loading: " + loading);

        if (loading) {
            loader.getThread().start();
            HBox root = new HBox();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());

            final ProgressIndicator progress = new ProgressIndicator();
            progress.setMaxSize(30, 30);
            root.setAlignment(Pos.CENTER);
            root.getChildren().add(progress);
            stage.setScene(scene);
        } else {
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

            int delayInSeconds = 2;
            Runnable delayedTask = () -> {
                stage.setScene(loader.getScene());
                loadingHolder.clear();
            };

            executor.schedule(delayedTask, delayInSeconds, TimeUnit.SECONDS);
        }
    }
}