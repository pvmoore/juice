package juice.components;

import juice.Frame;
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

    public interface Listener {
        void onMoved(Int2 delta);
        void onDropped(Int2 delta);
    }

    public DragComponent(Listener listener) {
        this.listener = listener;
    }
    public void enable() {
        isEnabled = true;
    }
    public void disable() {
        isEnabled = false;
    }
    public void update(Frame frame) {
        if(!isEnabled) {
            return;
        }
        var state       = frame.window.getMouseState();
        var nowDragging = state.drag.dragging;

        if(currentlyDragging) {
            if(lastDragPos != state.pos) {
                lastDragPos = state.pos;
                listener.onMoved(state.pos.sub(state.drag.start));
                moved = true;
            }
            if(!nowDragging) {
                if(moved) {
                    // Only inform the listener if anything was actually dragged
                    listener.onDropped(lastDragPos.sub(state.drag.start));
                }
                lastDragPos = Int2.ZERO;
                currentlyDragging = false;
                moved = false;
            }
        } else if(nowDragging) {
            var ui = (UIComponent)listener;
            if(ui.enclosesPoint(state.drag.start)) {
                currentlyDragging = true;
                lastDragPos       = state.drag.start;
            }
        }
    }
}
