package juice.renderers;

import juice.GLShaderProgram;
import juice.VAO;
import juice.VBO;
import juice.types.Int2;
import juice.types.RGBA;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

/**
 * Display coloured (non-textured) rectangles.
 */
final public class RectangleRenderer {
    private VAO vao;
    private VBO vbo;
    private ByteBuffer buffer;
    private GLShaderProgram prog;
    private boolean verticesChanged = true;
    private List<Rectangle> rectangles = new ArrayList<>();

    // Asummes vertices are in clockwise order:
    //
    // 0-1
    // | |
    // 3-2
    public static final class Rectangle {
        public Int2 p0;
        public Int2 p1;
        public Int2 p2;
        public Int2 p3;
        public RGBA c0;
        public RGBA c1;
        public RGBA c2;
        public RGBA c3;

        public Rectangle(Int2 p0, Int2 p1, Int2 p2, Int2 p3, RGBA c0, RGBA c1, RGBA c2, RGBA c3) {
            this.p0 = p0;
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
            this.c0 = c0;
            this.c1 = c1;
            this.c2 = c2;
            this.c3 = c3;
        }
        public Rectangle(Int2 p0, Int2 p1, Int2 p2, Int2 p3, RGBA c) {
            this(p0, p1, p2, p3, c, c, c, c);
        }
        // A square
        public Rectangle(Int2 pos, Int2 size, RGBA c) {
            this.p0 = pos;
            this.p1 = pos.add(size.getX(), 0);
            this.p2 = pos.add(size);
            this.p3 = pos.add(0, size.getY());
            this.c0 = this.c1 = this.c2 = this.c3 = c;
        }
    }
    //====================================================================
    public int getNumRectangles() {
        return rectangles.size();
    }

    public RectangleRenderer() {
        this.vao  = new VAO();
        this.prog = new GLShaderProgram()
            .shaderSource(GL_VERTEX_SHADER, VS)
            .shaderSource(GL_FRAGMENT_SHADER, FS)
            .link();
    }
    public void destroy() {
        if(vbo!=null) vbo.destroy();
        prog.destroy();
        vao.destroy();
    }
    public RectangleRenderer setVP(Matrix4f viewProj) {
        prog.use().setUniform("VP", viewProj);
        return this;
    }
    public RectangleRenderer addRectangle(Rectangle r) {
        rectangles.add(r);
        verticesChanged = true;
        return this;
    }
    public RectangleRenderer setRectangle(int index, Rectangle r) {
        rectangles.set(index, r);
        verticesChanged = true;
        return this;
    }
    public RectangleRenderer removeRectangle(int index) {
        rectangles.remove(index);
        verticesChanged = true;
        return this;
    }
    public RectangleRenderer clearRectangles() {
        rectangles.clear();
        verticesChanged = true;
        return this;
    }
    public void render() {
        if(rectangles.size()==0) return;
        vao.bind();
        prog.use();
        populateVbo();

        glDrawArrays(GL_TRIANGLES, 0, rectangles.size() * 6);   // 6 vertices
    }
    //==========================================================================
    private void populateVbo() {
        if(!verticesChanged) return;

        final int VERTEX_SIZE = 2 * 4 + // pos
                                4 * 4;  // rgba

        int bytesRequired = rectangles.size() * VERTEX_SIZE * 6;

        // alloc and bind the VBO
        if(vbo == null) {
            vbo = VBO.makeArray(bytesRequired, GL_DYNAMIC_DRAW);
            buffer = createByteBuffer(bytesRequired);
        } else if(bytesRequired > (int)vbo.sizeBytes) {
            vbo.bind();
            vbo.realloc(bytesRequired, GL_DYNAMIC_DRAW);
            buffer = createByteBuffer(bytesRequired);
        } else {
            vbo.bind();
        }

        buffer.position(0);
        rectangles.forEach(q -> {
            // 0-1
            // |/|
            // 3-2
            buffer.putFloat(q.p0.getX()); buffer.putFloat(q.p0.getY());          // 0
            buffer.putFloat(q.c0.r); buffer.putFloat(q.c0.g);
            buffer.putFloat(q.c0.b); buffer.putFloat(q.c0.a);

            buffer.putFloat(q.p3.getX()); buffer.putFloat(q.p3.getY());          // 3
            buffer.putFloat(q.c3.r); buffer.putFloat(q.c3.g);
            buffer.putFloat(q.c3.b); buffer.putFloat(q.c3.a);

            buffer.putFloat(q.p1.getX()); buffer.putFloat(q.p1.getY());          // 1
            buffer.putFloat(q.c1.r); buffer.putFloat(q.c1.g);
            buffer.putFloat(q.c1.b); buffer.putFloat(q.c1.a);


            buffer.putFloat(q.p1.getX()); buffer.putFloat(q.p1.getY());          // 1
            buffer.putFloat(q.c1.r); buffer.putFloat(q.c1.g);
            buffer.putFloat(q.c1.b); buffer.putFloat(q.c1.a);

            buffer.putFloat(q.p3.getX()); buffer.putFloat(q.p3.getY());          // 3
            buffer.putFloat(q.c3.r); buffer.putFloat(q.c3.g);
            buffer.putFloat(q.c3.b); buffer.putFloat(q.c3.a);

            buffer.putFloat(q.p2.getX()); buffer.putFloat(q.p2.getY());         // 2
            buffer.putFloat(q.c2.r); buffer.putFloat(q.c2.g);
            buffer.putFloat(q.c2.b); buffer.putFloat(q.c2.a);
        });
        buffer.position(0);
        vbo.addData(buffer);

        vao.enableAttrib(0, 2, GL_FLOAT, false, VERTEX_SIZE, 0);
        vao.enableAttrib(1, 4, GL_FLOAT, false, VERTEX_SIZE, 2*4);

        verticesChanged = false;
    }
    private static final String VS =
        "#version 330 core\n" +
        "" +
        "    layout(location = 0) in vec2 pos;" +
        "    layout(location = 1) in vec4 colour;" +
        "" +
        "    uniform mat4 VP;" +
        "" +
        "    out VS_OUT {" +
        "        vec4 colour;" +
        "    } vs_out;" +
        "" +
        "    void main() {" +
        "        gl_Position   = VP * vec4(pos,0,1);" +
        "        vs_out.colour = colour;" +
        "    }" +
        "";
    private static final String FS =
        "#version 330 core\n" +
        "" +
        "    in VS_OUT {" +
        "        vec4 colour;" +
        "    } fs_in;" +
        "" +
        "    out vec4 color;" +
        "" +
        "    void main() {" +
        "        color = fs_in.colour;" +
        "    }";
}
