package mn.sync.hikvisionbrige.appender;

import javafx.application.Platform;
import javafx.scene.control.TableView;
import mn.sync.hikvisionbrige.models.LogData;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 26/07/2023 - 9:45 AM
 * @purpose
 * @definition
 */

public class LogTableViewAppender extends AbstractAppender {
    private TableView<LogData> tableView;
    private OnLogUpdateListener onLogUpdateListener;

    public LogTableViewAppender(TableView<LogData> tableView) {
        super("LogTableViewAppender", null, null);
        this.tableView = tableView;
    }

    @Override
    public void append(LogEvent event) {
        // Convert LogEvent to LogData and add it to the TableView
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeMillis()), ZoneId.systemDefault());
        LogData logData = new LogData(
                localDateTime.format(formatter),
                event.getLevel().toString(),
                event.getLoggerName(),
                event.getMessage().getFormattedMessage()
        );

        // Update the TableView on the JavaFX Application Thread
        Platform.runLater(() -> {
            tableView.getItems().add(logData);

            if (onLogUpdateListener != null) {
                onLogUpdateListener.onLogUpdate();
            }
        });
    }

    // Interface to listen for log updates
    public interface OnLogUpdateListener {
        void onLogUpdate();
    }

    public void setOnLogUpdate(OnLogUpdateListener listener) {
        this.onLogUpdateListener = listener;
    }
}
