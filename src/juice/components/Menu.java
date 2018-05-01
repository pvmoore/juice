package juice.components;

import juice.Frame;
import juice.graphics.RoundRectangleRenderer;
import juice.graphics.TextRenderer;
import juice.types.Int2;
import juice.types.RGBA;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final public class Menu extends UIComponent {
    private static final int MENU_HIGHLIGHT = 2;
    private static final int ITEM_HIGHLIGHT = 3;
    private static final int SEPARATORS     = 4;

    private static final RGBA INVISIBLE     = new RGBA(0,0,0,0);
    private static final RGBA SEP_COLOUR    = RGBA.WHITE.gamma(0.8f);

    private RoundRectangleRenderer roundRectangles;
    private TextRenderer text;
    private String label;
    private int width;
    private boolean isOpen = false;
    private Int2 originalSize;

    private List<MenuItem> items = new ArrayList<>();
    private Set<Integer> separators = new HashSet<>();

    public Menu(String label, int width) {
        this.label = label;
        this.width = width;
    }

    public String getLabel() { return label; }
    public int getWidth() { return width; }
    public RoundRectangleRenderer getRoundRectangles() { return roundRectangles; }
    public TextRenderer getText() { return text; }
    public int getIndex(MenuItem item) { return items.indexOf(item); }

    public MenuItem getItem(int index) {
        return items.get(index);
    }

    public MenuBar getBar() {
        var p = getParent();
        if(p instanceof MenuBar) return (MenuBar)p;
        return ((Menu)p).getBar();
    }

    @Override public void add(UIComponent child) {
        if(!(child instanceof MenuItem)) throw new RuntimeException("Only add MenuItems to Menus");

        items.add((MenuItem)child);
    }
    public void addSeparator() {
        separators.add(items.size());
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
            ))
            // Menu highlight
            .addRectangle(new RoundRectangleRenderer.Rectangle(
                Int2.ZERO,
                Int2.ZERO,
                INVISIBLE,
                0
            ))
            // Item highlight
            .addRectangle(new RoundRectangleRenderer.Rectangle(
                Int2.ZERO,
                Int2.ZERO,
                INVISIBLE,
                0
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
        var c     = getBar().getHighlightColour().alpha(0.3f);
        var r     = 0;

        if(index==items.size()-1) {
            r = 5;
        }

        roundRectangles.setRectangle(ITEM_HIGHLIGHT, new RoundRectangleRenderer.Rectangle(
            item.getAbsPos(),
            item.getSize(),
            c, c, c.alpha(0.2f), c.alpha(0.2f),
            0, 0, r, r
        ));
    }
    private void openMenu() {
        if(items.size()==0) return;

        isOpen = true;

        final int ITEM_HEIGHT = 26;
        final int SEP_HEIGHT  = 7;
        final int SEP_WIDTH   = getSize().getX() / 4 * 3;

        setSize(getSize().add(0, items.size()*ITEM_HEIGHT + separators.size()*SEP_HEIGHT + 5));

        var pos  = getAbsPos();
        var size = getSize();
        var c1   = RGBA.BLACK;//RGBA.WHITE.gamma(0.5f);
        var c2   = RGBA.WHITE;
        var c3   = getBar().getHighlightColour().alpha(0.4f);

        roundRectangles.setRectangle(0, new RoundRectangleRenderer.Rectangle(
            pos,
            size,
            c1.alpha(0.1f), c1.alpha(0.1f), c1.alpha(0.4f), c1.alpha(0.4f),
            0, 0, 10, 10
        ));
        roundRectangles.setRectangle(1, new RoundRectangleRenderer.Rectangle(
            pos.add(2,0),
            size.sub(4,2),
            c2,c2.gamma(2),c2.gamma(0.9f),c2.gamma(0.7f),
            0, 0, 8, 8
        ));
        // Show highlight
        roundRectangles.setRectangle(MENU_HIGHLIGHT, new RoundRectangleRenderer.Rectangle(
            pos,
            originalSize,
            c3,c3, c3.alpha(0.2f),c3.alpha(0.2f),
            0, 0, 0, 0
        ));

        getStage().addAfterUpdateHook(() -> {
            var y = originalSize.getY();
            var i = 0;
            for(var item : items) {

                if(separators.contains(i)) {
                    // Add a separator rectangle here

                    roundRectangles.addRectangle(new RoundRectangleRenderer.Rectangle(
                        pos.add((originalSize.getX() - SEP_WIDTH)/2, y+3),
                        new Int2(SEP_WIDTH, 2),
                        SEP_COLOUR, SEP_COLOUR, SEP_COLOUR, SEP_COLOUR,
                        2,2,2,2
                    ));

                    y += SEP_HEIGHT;
                }

                item.setRelPos(new Int2(0, y));
                item.setSize(originalSize);
                super.add(item);

                y += ITEM_HEIGHT;
                i++;
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
        var c3   = RGBA.RED.blend(RGBA.BLUE).alpha(0.3f);
        var c4   = RGBA.RED.blend(RGBA.BLUE).alpha(0.2f);

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
        roundRectangles.setRectangle(MENU_HIGHLIGHT, new RoundRectangleRenderer.Rectangle(
            Int2.ZERO,
            Int2.ZERO,
            INVISIBLE,
            0
        ));
        roundRectangles.setRectangle(ITEM_HIGHLIGHT, new RoundRectangleRenderer.Rectangle(
            Int2.ZERO,
            Int2.ZERO,
            INVISIBLE,
            0
        ));

        // Remove separators
        while(roundRectangles.getNumRectangles() > SEPARATORS) {
            roundRectangles.removeRectangle(SEPARATORS);
        }

        getStage().addAfterUpdateHook(() -> {
            for(var item : items) {
                remove(item);
            }
        });
    }
}
