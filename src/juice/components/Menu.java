package juice.components;

import juice.Frame;
import juice.renderers.RoundRectangleRenderer;
import juice.renderers.TextRenderer;
import juice.types.Int2;
import juice.types.RGBA;

import java.util.ArrayList;
import java.util.List;

final public class Menu extends UIComponent {
    private RoundRectangleRenderer roundRectangles;
    private TextRenderer text;
    private String label;
    private int width;
    private boolean isOpen = false;
    private Int2 originalSize;

    private List<MenuItem> items = new ArrayList<>();

    public Menu(String label, int width) {
        this.label = label;
        this.width = width;
    }

    public String getLabel() { return label; }
    public int getWidth() { return width; }
    public RoundRectangleRenderer getRoundRectangles() { return roundRectangles; }
    public TextRenderer getText() { return text; }
    public int getIndex(MenuItem item) { return items.indexOf(item); }

    public MenuBar getBar() {
        var p = getParent();
        if(p instanceof MenuBar) return (MenuBar)p;
        return ((Menu)p).getBar();
    }

    @Override public void add(UIComponent child) {
        if(!(child instanceof MenuItem)) throw new RuntimeException("Only add MenuItems to Menus");

        items.add((MenuItem)child);
    }
    @Override public void onAddedToStage() {
        this.originalSize = getSize();

        roundRectangles = new RoundRectangleRenderer()
            .setVP(getStage().getCamera().VP());

        text = new TextRenderer(getBar().getFont())
            .setVP(getStage().getCamera().VP())
            .setColour(RGBA.BLACK)
            .setDropShadowColour(RGBA.BLACK.alpha(0.2f))
            .setSize(17);

        var pos  = getAbsPos();
        var size = getSize();
        var c1   = RGBA.WHITE.gamma(0.8f);
        var c2   = RGBA.WHITE;

        roundRectangles
            .clearRectangles()
            .addRectangle(new RoundRectangleRenderer.Rectangle(
                pos,
                size,
                c1,c1, c1.alpha(0.5f), c1.alpha(0.5f),
                0, 0, 10, 10
            ))
            .addRectangle(new RoundRectangleRenderer.Rectangle(
                pos.add(2,0),
                size.sub(4,2),
                c2,c2,c2,c2,
                0, 0, 5, 5
            ));

        var font = getBar().getFont();
        var tp   = font.centreText(label, 17, pos.add(size.div(2).getX(), -2));

        text.appendText(label, tp);
    }
    @Override public void destroy() {
        if(roundRectangles!=null) roundRectangles.destroy();
        roundRectangles = null;
    }
    @Override public void update(Frame frame) {

        var events = frame.getLocalMouseEvents(this);

        if(enclosesPoint(frame.window.getMousePos())) {
            if(!isOpen) openMenu();
        } else {
            if(isOpen) closeMenu();
        }
    }
    @Override public void render(Frame frame) {
        roundRectangles.render();
        text.render();
    }
    public void highlightItem(MenuItem item) {
        if(!isOpen) return;

        var index = item.getIndex();
        var y     = originalSize.getY() + index*26;
        var c     = RGBA.RED.blend(RGBA.BLUE).alpha(0.2f);
        var r     = 0;

        if(index==items.size()-1) {
            r = 5;
        }

        if(roundRectangles.getNumRectangles()==3) {
            roundRectangles.addRectangle(new RoundRectangleRenderer.Rectangle(
                item.getAbsPos(),
                item.getSize(),
                c, c, c, c,
                0, 0, r, r
            ));
        } else {
            roundRectangles.setRectangle(3, new RoundRectangleRenderer.Rectangle(
                item.getAbsPos(),
                item.getSize(),
                c, c, c, c,
                0, 0, r, r
            ));
        }
    }
    private void openMenu() {
        if(items.size()==0) return;

        isOpen = true;

        setSize(getSize().add(0, items.size()*26 + 5));

        var pos  = getAbsPos();
        var size = getSize();
        var c1   = RGBA.WHITE.gamma(0.8f);
        var c2   = RGBA.WHITE;
        var c3   = RGBA.RED.blend(RGBA.BLUE).alpha(0.3f);

        roundRectangles.setRectangle(0, new RoundRectangleRenderer.Rectangle(
            pos,
            size,
            c1,c1, c1.alpha(0.5f), c1.alpha(0.5f),
            0, 0, 10, 10
        ));
        roundRectangles.setRectangle(1, new RoundRectangleRenderer.Rectangle(
            pos.add(2,0),
            size.sub(4,2),
            c2,c2.gamma(2),c2.gamma(0.9f),c2.gamma(0.7f),
            0, 0, 8, 8
        ));

        roundRectangles.addRectangle(new RoundRectangleRenderer.Rectangle(
            pos,
            originalSize,
            c3,c3,c3,c3,
            0, 0, 5, 5
        ));

        getStage().addAfterUpdateHook(() -> {
            var y = originalSize.getY();
            for(var item : items) {
                item.setRelPos(new Int2(0, y));
                item.setSize(originalSize);
                super.add(item);
                y += 26;
            }
        });
    }
    public void closeMenu() {
        if(!isOpen) return;

        isOpen = false;

        setSize(originalSize);

        var pos  = getAbsPos();
        var size = getSize();
        var c1   = RGBA.WHITE.gamma(0.8f);
        var c2   = RGBA.WHITE;

        roundRectangles.setRectangle(0, new RoundRectangleRenderer.Rectangle(
            pos,
            size,
            c1,c1, c1.alpha(0.5f), c1.alpha(0.5f),
            0, 0, 10, 10
        ));
        roundRectangles.setRectangle(1, new RoundRectangleRenderer.Rectangle(
            pos.add(2,0),
            size.sub(4,2),
            c2,c2,c2,c2,
            0, 0, 5, 5
        ));

        roundRectangles.removeRectangle(2); // top highlight
        roundRectangles.removeRectangle(2); // item highlight

        getStage().addAfterUpdateHook(() -> {
            for(var item : items) {
                remove(item);
            }
        });
    }
}
