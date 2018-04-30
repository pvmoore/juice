package juice;

import juice.components.UIComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final public class Frame {
    public Window window;
    public long number;
    public long nsecs;
    public double delta;

    /**package*/ List<Mouse.Event> mouseEvents = new ArrayList<>();

    public List<Mouse.Event> getLocalMouseEvents(UIComponent forComponent) {
        return mouseEvents.stream()
                          .filter(it->forComponent.enclosesPoint(it.pos))
                          .collect(Collectors.toList());
    }
    public List<Mouse.Event> getLocalMouseEvents(UIComponent forComponent, Mouse.EventType type) {
        return mouseEvents.stream()
                          .filter(it->forComponent.enclosesPoint(it.pos))
                          .filter(it->it.type==type)
                          .collect(Collectors.toList());
    }
}
