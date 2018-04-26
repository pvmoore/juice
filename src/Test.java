import juice.Camera2D;
import juice.Frame;
import juice.Texture;
import juice.Window;
import juice.components.Sprite;
import juice.components.UIComponent;
import juice.renderers.RectangleRenderer;
import juice.renderers.RoundRectangleRenderer;
import juice.types.Int2;
import juice.types.RGBA;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.opengl.GL11.*;

public class Test {
    private Window window;
    private MainComponent mainComponent;

    public static void main(String[] args) {
        Test main = null;
        try{
            main = new Test();
            main.run();
        }catch(Throwable t) {
            t.printStackTrace();
        }finally{
            if(main!=null) main.destroy();
        }
    }
    private Test() {
        this.window = new Window((p) -> {
            p.windowed   = true;
            p.vsync      = true;
            p.width      = 1024;
            p.height     = 800;
            p.title      = "JUICE";
            p.textureDir = "./images/";
            p.fontDir    = "./fonts/";
        });

        mainComponent = new MainComponent(window);

        // Add our main UI component
        window.getStage().add(mainComponent);

        //System.out.println(""+window.getStage());

        window.show(true);
    }
    private void destroy() {
        if(window !=null) window.destroy();
    }
    private void run() {
        window.loop();
    }
    //======================================================================
    class MainComponent extends UIComponent {
        private Camera2D camera2d;
        private Window window;
        private Sprite sprite;
        private RectangleRenderer rectangles;
        private RoundRectangleRenderer roundRectangles;

        MainComponent(Window w) {
            this.window = w;
            this.camera2d = new Camera2D(w.getWindowSize());
            this.sprite = new Sprite()
                .setVP(camera2d.VP())
                .setTexture(Texture.get("bishop-256.png", Texture.standardAttribs));

            glClearColor(0,0,0,0f);

            // Enable alpha blending
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            sprite.setSize(new Int2(256,256));
            sprite.setRelPos(new Int2(10,10));

            rectangles = new RectangleRenderer()
                .setVP(camera2d.VP())
                // 0-1
                // | |
                // 3-2
                .addRectangle(new RectangleRenderer.Rectangle(
                    new Int2(300,10), new Int2(400,10), new Int2(400,110), new Int2(300,110),
                    RGBA.WHITE, RGBA.BLUE, RGBA.GREEN, RGBA.RED
                ));

            roundRectangles = new RoundRectangleRenderer()
                .setVP(camera2d.VP())
                // Flag
                .addRectangle(new RoundRectangleRenderer.Rectangle(
                    new Int2(430, 10), new Int2(100,100),
                    RGBA.WHITE, RGBA.WHITE, RGBA.WHITE, RGBA.RED,
                    0, 0, 10, 40
                ))
                // White border
                .addRectangle(new RoundRectangleRenderer.Rectangle(
                    new Int2(550, 10), new Int2(100,100),
                    RGBA.WHITE, 32
                ))
                .addRectangle(new RoundRectangleRenderer.Rectangle(
                    new Int2(555, 15), new Int2(90,90),
                    RGBA.BLUE.blend(RGBA.RED), 30
                ));

            add(sprite);
        }

        @Override public void destroy() {
            super.destroy();

            rectangles.destroy();
            roundRectangles.destroy();
        }

        @Override public void update(Frame frame) {
            var keys = window.getKeysPressed();

            if(keys.contains(GLFW_KEY_ESCAPE)) {
                window.close();
            }

            //animations.update(frame.delta);

            super.update(frame);
        }

        @Override public void render(Frame frame) {

            glClear(GL_COLOR_BUFFER_BIT);

            super.render(frame);

            rectangles.render(frame);
            roundRectangles.render(frame);
        }
    }
}



