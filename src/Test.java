import juice.Camera2D;
import juice.Frame;
import juice.Window;
import juice.components.UIComponent;

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
            p.width      = 800;
            p.height     = 600;
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

        MainComponent(Window w) {
            this.window = w;
            this.camera2d = new Camera2D(w.getWindowSize());
        }

        @Override public void update(Frame frame) {
            var keys = window.getKeysPressed();

            if(keys.contains(GLFW_KEY_ESCAPE)) {
                window.close();
            }

            //animations.update(frame.delta);

            super.update(frame);
        }
    }
}



