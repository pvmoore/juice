package juice.components;

import juice.Frame;
import juice.Mouse;
import juice.types.Int2;
/**
 * Handle UIComponent mouse dragging.
 */
final public class DragComponent {
    private Listener listener;

    private Int2 lastDragPos = Int2.ZERO;
    private boolean currentlyDragging = false;
    private boolean moved = false;
    private boolean isEnabled = true;

    private UIComponent component;
    private Int2 absDragStart, relDragStart;
    private int button = -1;

    public interface Listener {
        UIComponent getComponent();
        /** @param delta Delta from drag start */
        default void onDragMoved(Int2 delta) {}
        /** @param delta Delta from drag start */
        default void onDragDropped(Int2 delta) {}
    }

    public DragComponent(Listener listener) {
        this.listener  = listener;
        this.component = listener.getComponent();
    }
    public void enable() {
        isEnabled = true;
    }
    public void disable() {
        isEnabled = false;
        reset();
    }
    public void update(Frame frame) {
        if(!isEnabled) return;

        // Process all events
        for(var e : frame.getGlobalMouseEvents()) {

            var pos = e.pos;

            if(currentlyDragging) {
                boolean isRelease = e.type==Mouse.EventType.BUTTON_RELEASE &&
                                    e.button==button;
                var delta         = pos.sub(absDragStart);

                if(lastDragPos != pos) {
                    lastDragPos = pos;

                    component.setRelPos(relDragStart.add(delta));
                    listener.onDragMoved(delta);
                    moved = true;
                }
                if(isRelease) {
                    if(moved) {
                        // Only inform the listener if anything was actually dragged
                        listener.onDragDropped(delta);
                    }
                    reset();
                }
                frame.consume(e);
            } else {
                boolean isLocal = component.enclosesPoint(e.pos);
                boolean isPress = e.type==Mouse.EventType.BUTTON_PRESS;

                if(isPress && isLocal) {
                    currentlyDragging = true;
                    button            = e.button;
                    lastDragPos       = pos;
                    absDragStart      = pos;
                    relDragStart      = component.getRelPos();

                    frame.consume(e);
                }
            }
        }
    }
    //=========================================================
    private void reset() {
        lastDragPos       = Int2.ZERO;
        currentlyDragging = false;
        moved             = false;
        button            = -1;
    }
}

