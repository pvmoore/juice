package juice.components;

import juice.Font;
import juice.Frame;
import juice.renderers.RoundRectangleRenderer;
import juice.types.Int2;
import juice.types.RGBA;

final public class MenuBar extends UIComponent {
    private RoundRectangleRenderer roundRectangles;
    private Font font;
    private int nextX = 5;

    public Font getFont() { return font; }

    @Override public void add(UIComponent child) {
        if(!(child instanceof Menu)) throw new RuntimeException("Only add Menus to MenuBar");

        var m    = (Menu)child;
        var size = getSize();

        var reqWidth = m.getWidth() + 10;

        m.setRelPos(new Int2(nextX, 2));
        m.setSize(new Int2(reqWidth, size.getY()-4));

        nextX += reqWidth;

        setSize(new Int2(nextX + 5, size.getY()));

        super.add(child);
    }
    @Override public void onAddedToStage() {
        this.font = Font.get("segoe-ui");

        var pos  = getAbsPos();
        var size = getSize();
        var c1   = RGBA.WHITE.gamma(0.5f);
        var c2   = RGBA.WHITE;

        roundRectangles = new RoundRectangleRenderer()
            .setVP(getStage().getCamera().VP())
            .addRectangle(new RoundRectangleRenderer.Rectangle(
                pos,
                size,
                c1.gamma(2),c1,c1.alpha(0.5f),c1.alpha(0.5f),
                0, 0, 10, 0
            ))
            .addRectangle(new RoundRectangleRenderer.Rectangle(
                pos,
                size.sub(2,2),
                c2,c2,c2.alpha(0.5f),c2.alpha(0.5f),
                0, 0, 8, 0
            ));
    }
    @Override public void destroy() {
        if(roundRectangles!=null) roundRectangles.destroy();
        roundRectangles = null;
    }
    @Override public void render(Frame frame) {
        roundRectangles.render();
    }
    @Override public void onResized() {
        if(roundRectangles==null) return;

        var pos  = getAbsPos();
        var size = getSize();
        var c1   = RGBA.WHITE.gamma(0.5f);
        var c2   = RGBA.WHITE;

        roundRectangles
            .setRectangle(0, new RoundRectangleRenderer.Rectangle(
                pos,
                size,
                c1.gamma(2),c1,c1.alpha(0.5f),c1.alpha(0.5f),
                0, 0, 10, 0
            ))
            .setRectangle(1, new RoundRectangleRenderer.Rectangle(
                pos,
                size.sub(2,2),
                c2,c2,c2.alpha(0.5f),c2.alpha(0.5f),
                0, 0, 8, 0
            ));
    }
}
