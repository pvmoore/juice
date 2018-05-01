package juice.components;

import juice.Frame;
import juice.types.Int2;
import juice.types.Rect;

import java.util.ArrayList;
import java.util.List;

public class UIComponent {
    private Int2 pos  = Int2.ZERO;
    private Int2 size = Int2.ZERO;
    private List<UIComponent> children = new ArrayList<>();
    private UIComponent parent;
    //====================================================================
    /** Returns the sum of all relative positions. */
    public Int2 getAbsPos() {
        if(parent==null) return pos;
        return pos.add(parent.getAbsPos());
    }
    public Int2 getRelPos() {
        return pos;
    }
    public UIComponent setRelPos(Int2 p) {
        boolean changed = p!=pos;
        pos = p;
        if(changed) onMoved();
        return this;
    }
    public Int2 getSize() {
        return size;
    }
    public UIComponent setSize(Int2 s) {
        boolean changed = s!=size;
        size = s;
        if(changed) onResized();
        return this;
    }
    public UIComponent getParent() {
        return parent;
    }
    public List<UIComponent> getChildren() {
        return children;
    }
    public int indexOf(UIComponent child) {
        return children.indexOf(child);
    }
    /**
     * Move child to start of children list so that it will be
     * rendered first and updated last.
     */
    public void moveToBack(UIComponent child) {
        var i = children.indexOf(child);
        if(i > 0) {
            children.remove(i);
            children.add(0, child);
        }
    }
    /**
     * Move child to end of children list so that it will be
     * rendered last and updated first.
     */
    public void moveToFront(UIComponent child) {
        var i = children.indexOf(child);
        if(i!=-1 && i!=children.size()-1) {
            children.remove(i);
            children.add(child);
        }
    }
    public boolean enclosesPoint(Int2 p) {
        return Rect.of(getAbsPos(), getSize()).contains(p);
    }
    public Stage getStage() {
        if(this instanceof Stage) return (Stage)this;
        if(parent==null) {
            return null;
        }
        return parent.getStage();
    }
    //====================================================================
    public void add(UIComponent child) {
        if(child.parent!=null) {
            if(child.parent==this) {
                // Child is already a child of this parent
                return;
            }
            // Detach first
            child.detach();
        }
        child.parent = this;
        children.add(child);

        // Call events
        child.onAdded();
        onChildAdded(child);
        if(getStage()!=null) child.fireOnAddedToStage();
    }
    public void remove(UIComponent child) {
        boolean isOnStage = getStage()!=null;
        child.parent = null;
        if(children.remove(child)) {
            // Call events of child was actually removed
            child.onRemoved();
            onChildRemoved(child);
            if(isOnStage) child.fireOnRemovedFromStage();
        }
    }
    public boolean isAttached() {
        return parent!=null;
    }
    public void detach() {
        if(parent==null) return;
        parent.remove(this);
    }
    public void destroy() {

    }
    public void update(Frame frame) {

    }
    public void render(Frame frame) {

    }
    //====================================================================
    // Child events
    //====================================================================
    public void onAdded() {
        // override this if you need to do things after you are added
    }
    public void onRemoved() {
        // override if you need to do things after you are removed
    }
    public void onAddedToStage() {
        // override if you need to do things after you are
        // added to the stage (directly or indirectly)
    }
    public void onRemovedFromStage() {
        // override if you need to do things after you are
        // removed from the stage (directly or indirectly)
    }
    public void onMoved() {
        // override if you are interested in move events
    }
    public void onResized() {
        // override if you are interested in size events
    }
    //====================================================================
    // Parent events
    //====================================================================
    public void onChildAdded(UIComponent child) {

    }
    public void onChildRemoved(UIComponent child) {

    }
    //====================================================================
    @Override public String toString() {
        var s =  String.format("%s[%s : %s %d children] parent:%s",
                             getClass().getSimpleName(),
                             pos, size, children.size(),
                             parent==null ? "null" :
                             parent.getClass().getSimpleName());
        for(var c : children) {
            s += "\n\t" + c.toString();
        }
        return s;
    }
    //====================================================================
    protected void fireDestroy() {
        destroy();
        for(var c : children) {
            c.fireDestroy();
        }
    }
    protected void fireUpdate(Frame frame) {
        update(frame);

        // Update children in reverse order
        for(int i = children.size()-1; i>=0; i--) {
            children.get(i).fireUpdate(frame);
        }
    }
    protected void fireRender(Frame frame) {
        render(frame);
        for(var c : children) {
            c.fireRender(frame);
        }
    }
    private void fireOnAddedToStage() {
        onAddedToStage();
        for(var c : children) {
            c.fireOnAddedToStage();
        }
    }
    private void fireOnRemovedFromStage() {
        onRemovedFromStage();
        for(var c : children) {
            c.fireOnRemovedFromStage();
        }
    }
}
