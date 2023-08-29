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
import mn.sync.hikvisionbrige.appender.LogTableViewAppender;
import mn.sync.hikvisionbrige.holders.LoadingHolder;
import mn.sync.hikvisionbrige.models.Loader;
import mn.sync.hikvisionbrige.models.LogData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class Components {

    public abstract void draw();

    public static TableView<LogData> getLogTableView() {
        TableView<LogData> logTableView = new TableView<>();

        TableColumn<LogData, String> timestampColumn = new TableColumn<>("Time");
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timestampColumn.setPrefWidth(115);

        TableColumn<LogData, String> levelColumn = new TableColumn<>("Level");
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        levelColumn.setPrefWidth(45);

        TableColumn<LogData, String> loggerColumn = new TableColumn<>("Logger");
        loggerColumn.setCellValueFactory(new PropertyValueFactory<>("logger"));
        loggerColumn.prefWidthProperty().bind(logTableView.widthProperty().subtract(165).divide(2));

        TableColumn<LogData, String> messageColumn = new TableColumn<>("Message");
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        messageColumn.prefWidthProperty().bind(logTableView.widthProperty().subtract(165).divide(2));

        logTableView.getColumns().addAll(timestampColumn, levelColumn, messageColumn, loggerColumn);

        // Initialize Log4j2 and set up a custom appender to add log messages to the TableView
        LogTableViewAppender appender = new LogTableViewAppender(logTableView);
        appender.start();

        // Add the appender to Log4j2's root logger
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        config.getRootLogger().addAppender(appender, null, null);

        // Update the TableView whenever new log messages are received
        appender.setOnLogUpdate(logTableView::refresh);

        return logTableView;
    }

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
