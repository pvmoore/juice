package juice;

import juice.components.Stage;
import juice.components.UIComponent;
import juice.types.Int2;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_SHADING_LANGUAGE_VERSION;
import static org.lwjgl.system.MemoryUtil.NULL;
/**
 * Handles window creation and event management.
 * This uses OpenGL and GLFW via LWJGL.
 */
final public class Window {
    private GLFWErrorCallback errorCallback;
    private Callback debugProc;
    private long window;
    private Set<Integer> keys = new HashSet<>();
    private Stage stage = new Stage();
    private Props props = new Props();
    private MouseState mouseState = new MouseState();
    //====================================================================
    public static final class Props {
        public boolean windowed  = true;
        public boolean vsync     = true;
        public int width         = 400;
        public int height        = 400;
        public String title      = "Change me";
        public String textureDir = "./";
        public String fontDir    = "./";
    }
    public final class MouseDrag {
        public boolean dragging = false;
        public int button       = 0;
        public Int2 start       = Int2.ZERO;
        public Int2 end         = Int2.ZERO;

        private void update(int button) {
            Int2 pos = mouseState.pos;
            if(mouseState.drag.dragging) {
                if(!mouseState.button[mouseState.drag.button]) {
                    mouseState.drag.dragging = false;
                    mouseState.drag.end = pos;
//                    if(mouseState.drag.start != pos) {
//                        stage.mouseDragEnd(mouseState.drag);
//                    }
                }
            } else { // not currently dragging
                if(mouseState.button[button]) {
                    mouseState.drag.dragging = true;
                    mouseState.drag.start = pos;
                    mouseState.drag.button = button;
                }
            }
        }
    };
    public static final class Modifier {
        public boolean SHIFT = false;
        public boolean CTRL  = false;
        public boolean ALT   = false;

        private void apply(int value) {
            SHIFT = (value & GLFW_MOD_SHIFT) != 0;
            CTRL  = (value & GLFW_MOD_CONTROL) != 0;
            ALT   = (value & GLFW_MOD_ALT) != 0;
        }
    }
    public final class MouseState {
        public Int2 pos          = Int2.ZERO;
        public boolean[] button = new boolean[3];
        public MouseDrag drag   = new MouseDrag();
        public Modifier mods    = new Modifier();
        public int wheelDelta   = 0;

        public void resetWheelDelta() { wheelDelta = 0; }
        public void resetDragStart() { drag.start = pos; }
    };
    //====================================================================
    public UIComponent getStage() { return stage; }
    public MouseState getMouseState() { return mouseState; }

    public void show(boolean show) {
        if(show) glfwShowWindow(window);
        else glfwHideWindow(window);
    }
    public Int2 getWindowSize() {
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(window, w, h);
        return new Int2(w.get(0), h.get(0));
    }
    public Set<Integer> getKeysPressed() {
        return Collections.unmodifiableSet(keys);
    }
    public void setWindowTitle(String title) {
        glfwSetWindowTitle(window, title);
    }
    public void setWindowIcon(String filename) {

    }
    //====================================================================
    /**
     *  new Window((p) -> {
     *      p.windowed = true;
     *      // etc
     *  });
     */
    public Window(Consumer<Props> propsConsumer) {
        propsConsumer.accept(props);

        Texture.setDirectory(props.textureDir);
        Font.setDirectory(props.fontDir);

        System.out.println("LWJGL ......... " + Version.getVersion());
        System.out.println("GLFW .......... " + glfwGetVersionString());

        glfwSetErrorCallback(errorCallback = new GLFWErrorCallback() {
            private GLFWErrorCallback delegate = GLFWErrorCallback.createPrint(System.err);
            @Override
            public void invoke(int error, long description) {
                if (error == GLFW_VERSION_UNAVAILABLE) System.err.println("OpenGL 3.3 or higher required");
                delegate.invoke(error, description);
            }
            @Override
            public void free() {
                delegate.free();
                super.free();
            }
            public GLFWErrorCallback getDelegate() {
                return delegate;
            }
            public void setDelegate(GLFWErrorCallback delegate) {
                this.delegate = delegate;
            }
        });
        if(!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 1);

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        long monitor        = glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = glfwGetVideoMode(monitor);

        if(props.windowed) {
            monitor = NULL;
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
            glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);
        } else {
            props.width  = vidmode.width();
            props.height = vidmode.height();
        }

        //glfwWindowHint(GLFW_AUTO_ICONIFY, hints.autoIconify ? GL_TRUE : GL_FALSE);

        window = glfwCreateWindow(props.width, props.height, props.title, monitor, NULL);
        if(window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        if(props.windowed) {
            glfwSetWindowPos(
                window,
                ((vidmode.width() - props.width) / 2),
                ((vidmode.height() - props.height) / 2)
            );
        }

        glfwMakeContextCurrent(window);

        if(props.vsync) {
            glfwSwapInterval(1);
        } else {
            glfwSwapInterval(0);
        }

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        GLCapabilities caps = GL.createCapabilities();
        debugProc = GLUtil.setupDebugMessageCallback();

        System.out.println("Vendor ........ " + glGetString(GL_VENDOR));
        System.out.println("Version ....... " + glGetString(GL_VERSION));
        System.out.println("GLSL version .. " + glGetString(GL_SHADING_LANGUAGE_VERSION));
        System.out.println("Renderer ...... " + glGetString(GL_RENDERER));

        System.out.println("Video mode .... (" + vidmode.width() + "x" + vidmode.height() + ") " + vidmode.refreshRate() + "Hz");
        System.out.println("Window size ... " + getWindowSize());


        glfwSetKeyCallback(window, (window1, key, scancode, action, mods) -> {
            if(action == GLFW_PRESS) {
                keys.add(key);
            } else if(action==GLFW_RELEASE) {
                keys.remove(key);
            }
        });
        glfwSetMouseButtonCallback(window, (window1, button, action, mods) -> {
            mouseState.button[button] = (action == GLFW_PRESS);
            mouseState.mods.apply(mods);
            mouseState.drag.update(button);

            //stage.mouseButton(button, mouseState);
        });
        glfwSetCursorPosCallback(window, (window1, xpos, ypos) -> {
            mouseState.pos = new Int2((int)xpos, (int)ypos);
            mouseState.drag.update(0);
            mouseState.drag.update(1);
            mouseState.drag.update(2);

            //stage.mouseMoved(mouseState);
        });
        glfwSetScrollCallback(window, (window1, xoffset, yoffset) -> {
            mouseState.wheelDelta = (int)yoffset;
        });

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glViewport(0, 0, props.width, props.height);

        stage.setRelPos(Int2.ZERO);
        stage.setSize(getWindowSize());
    }
    public void close() {
        glfwSetWindowShouldClose(window, true);
    }
    public void destroy() {
        stage.destroy();
        debugProc.free();
        errorCallback.free();
        Texture.destroy();
        Font.destroy();
        glfwDestroyWindow(window);
        glfwTerminate();
    }
    public void loop() {
        long frameNumber        = 0;
        long startTimestamp     = System.nanoTime();
        long lastFrameTimestamp = startTimestamp;
        int prevSecond          = (int)(lastFrameTimestamp * 1e-9);
        int prev5Seconds        = prevSecond;
        double delta            = 1;
        var frame               = new Frame();
        frame.window = this;

        while(!glfwWindowShouldClose(window)) {

            frame.number = frameNumber;
            frame.nsecs  = System.nanoTime()-startTimestamp;
            frame.delta  = delta;

            stage.update(frame);
            stage.render(frame);

            glfwSwapBuffers(window);
            glfwPollEvents();

            /// Update timing info
            var timestamp  = System.nanoTime();
            var frameNsecs = (timestamp - lastFrameTimestamp);
            lastFrameTimestamp = timestamp;

            delta = (float)((double)frameNsecs * 1e-9);
            frameNumber++;

            /// Every 10 seconds
            var totalSeconds = timestamp * 1e-9;
            if((int)totalSeconds > prevSecond+10) {
                prevSecond = (int)totalSeconds;
                double ms  = frameNsecs * 1e-6;
                double fps = 1000.0 / ms;

//                System.out.println(String.format("Frame [%d] Delta: %.4f, Elapsed: %.3fms, FPS: %.3f",
//                                                 frameNumber, delta, ms, fps));

                /// Every 30 seconds
//                if((int)totalSeconds>prev5Seconds+30) {
//                    prev5Seconds = (int)totalSeconds;
//
//
//                }
            }
        }
    }
}

