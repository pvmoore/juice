import juice.Font;
import juice.Frame;
import juice.Texture;
import juice.Window;
import juice.components.*;
import juice.renderers.ParagraphTextRenderer;
import juice.renderers.RectangleRenderer;
import juice.renderers.RoundRectangleRenderer;
import juice.types.Int2;
import juice.types.RGBA;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

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

        mainComponent = new MainComponent();

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
        private Sprite sprite;
        private RectangleRenderer rectangles;
        private RoundRectangleRenderer roundRectangles;
        private ParagraphTextRenderer paraText;

        MainComponent() {

        }
        @Override public void onAddedToStage() {
            createSprite();
            createRectangles();
            createMenu();
            createText();
        }
        void createSprite() {
            // Large sprite
            this.sprite = new Sprite()
                .setVP(getStage().getCamera().VP())
                .setTexture(Texture.get("bishop-256.png", Texture.standardAttribs));
            sprite.setSize(new Int2(256,256));
            sprite.setRelPos(new Int2(10,100));
            add(sprite);
        }
        void createRectangles() {
            // A rectangle
            rectangles = new RectangleRenderer()
                .setVP(getStage().getCamera().VP())
                // 0-1
                // | |
                // 3-2
                .addRectangle(new RectangleRenderer.Rectangle(
                    new Int2(300,100), new Int2(400,100), new Int2(400,210), new Int2(300,210),
                    RGBA.WHITE, RGBA.BLUE, RGBA.GREEN, RGBA.RED
                ));

            // Some round rectangles
            var magenta = RGBA.BLUE.blend(RGBA.RED).gamma(0.7f);

            roundRectangles = new RoundRectangleRenderer()
                .setVP(getStage().getCamera().VP())
                // Flag
                .addRectangle(new RoundRectangleRenderer.Rectangle(
                    new Int2(430, 100), new Int2(100,100),
                    RGBA.WHITE, RGBA.WHITE, RGBA.WHITE, RGBA.RED,
                    0, 0, 10, 40
                ))
                // White border
                .addRectangle(new RoundRectangleRenderer.Rectangle(
                    new Int2(550, 100), new Int2(100,100),
                    RGBA.WHITE.gamma(0.7f), RGBA.WHITE.gamma(2), RGBA.WHITE.gamma(0.7f), RGBA.WHITE.gamma(0.05f),
                    32, 32, 32, 32
                ))
                .addRectangle(new RoundRectangleRenderer.Rectangle(
                    new Int2(555, 105), new Int2(90,90),
                    magenta, magenta, magenta.alpha(0.5f), magenta.alpha(0.5f),
                    30, 30, 30, 30
                ));
        }
        void createMenu() {
            var bar = new MenuBar();
            bar.setRelPos(new Int2(0,0));
            bar.setSize(new Int2(400, 30));
            add(bar);

            var file = new Menu("File", 60);
            var game = new Menu("Game", 80);
            var three = new Menu("Three things", 100);
            var about = new Menu("About", 70);



            file.add(new MenuItem("Exit", ()->{}));
            file.addSeparator();
            file.add(new MenuItem("Open", () -> {}));
            file.add(new MenuItem("Save", () -> {}));


            three.add(new MenuItem("Thing 1", () -> {}));
            three.add(new MenuItem("Thing 2", () -> {}).setEnabled(false));
            three.add(new MenuItem("Thing 3", () -> {}));
            three.addSeparator();
            three.add(new MenuItem("Thing 4", () -> System.out.println("Thing 4")));
            three.add(new MenuItem("Thing 5", () -> {}));

            bar.add(file);
            bar.add(game);
            bar.add(three);
            bar.add(about);
        }
        void createText() {
            var c = RGBA.WHITE.gamma(0.2f);

            rectangles.addRectangle(new RectangleRenderer.Rectangle(
                new Int2(20,400), new Int2(220,400), new Int2(220,700), new Int2(20,700),
                c,c,c,c
            ));

            paraText = new ParagraphTextRenderer(Font.get("segoe-ui"),
                                                 new Int2(20, 400), new Int2(200, 300))
                .setVP(getStage().getCamera().VP());

            paraText.setColour(RGBA.WHITE)
                    .setSize(18)
                    .left("I am left justified.")
                    .left("Me too...")
                    .newLine()
                    .setColour(RGBA.YELLOW)
                    .justified("I am an example of some justified text.\nLine.\nLine.")
                    .newLine()
                    .setColour(RGBA.WHITE)
                    .setSize(32)
                    .centred("I am centred")
                    .newLine()
                    .setSize(18)
                    .left("Left justified\nagain")
                    .left("...");
        }

        @Override public void destroy() {
            rectangles.destroy();
            roundRectangles.destroy();
            paraText.destroy();
        }

        @Override public void update(Frame frame) {
            var keys = window.getKeysPressed();

            if(keys.contains(GLFW_KEY_ESCAPE)) {
                window.close();
            }
        }

        @Override public void render(Frame frame) {
            rectangles.render();
            roundRectangles.render();
            paraText.render();
        }
    }
}



