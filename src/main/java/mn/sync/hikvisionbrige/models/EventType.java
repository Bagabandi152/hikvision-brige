package mn.sync.hikvisionbrige.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 26/09/2023 - 2:41 PM
 * @purpose
 * @definition
 */
public class EventType {
    String name;
    Integer id;

    public EventType(String name, Integer id) {
        this.name = name;
        this.id = id;
    }

    public static ObservableList getEventTypes() {
        ObservableList<EventType> list = FXCollections.observableArrayList();
        list.addAll(new EventType("Card log", 1), new EventType("Face log", 75));
        return list;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return name;
    }
}
