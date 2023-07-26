package mn.sync.hikvisionbrige.constants;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import mn.sync.hikvisionbrige.holders.LoadingHolder;
import mn.sync.hikvisionbrige.models.Loader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    private static LoadingHolder loadingHolder = LoadingHolder.getInstance();
    public static void getSpinningLoader(Stage stage, Boolean loading) {
        if (loading) {
            loadingHolder.setLoader(new Loader(stage.getScene()));
        }

        Loader loader = loadingHolder.getLoader();
        System.out.println("Loading: " + loading);

        if (loading) {
            HBox root = new HBox();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());

            final ProgressIndicator progress = new ProgressIndicator();
            progress.setMaxSize(30, 30);
            root.setAlignment(Pos.CENTER);
            root.getChildren().add(progress);
            stage.setScene(scene);
        } else {
            stage.setScene(loader.getScene());
            loadingHolder.clear();
        }
    }

    public static ScrollPane getTableWithScroll(ObservableList list, JSONArray columns) {
        TableView<Object> table = new TableView<>();
        table.setPrefWidth(295);
        table.setPrefHeight(180);

        List<TableColumn<Object, ?>> columnArrayList = new ArrayList<>();
        for (int i = 0; i < columns.length(); i++) {
            JSONObject colObj = columns.getJSONObject(i);
            String colName = colObj.getString("name");
            String colKey = colObj.getString("key");
            TableColumn<Object, ?> tableColumn = new TableColumn<>(colName);
            tableColumn.setCellValueFactory(new PropertyValueFactory<>(colKey));
            if (colObj.has("sortType")) {
                String sortType = colObj.getString("sortType");
                if (sortType.equalsIgnoreCase("DESC")) {
                    tableColumn.setSortType(TableColumn.SortType.DESCENDING);
                } else {
                    tableColumn.setSortType(TableColumn.SortType.ASCENDING);
                }
            }
            if (colObj.has("sortAble")) {
                tableColumn.setSortable(colObj.getBoolean("sortAble"));
            }
            columnArrayList.add(tableColumn);
        }
        table.setItems(list);
        table.getColumns().addAll(columnArrayList);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(table);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        return scrollPane;
    }
}
