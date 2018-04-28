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

import static juice.Util.putFloats;
import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

final public class RoundRectangleRenderer {
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
        public Int2 pos;
        public Int2 size;
        public RGBA[] colour = new RGBA[4];
        public float[] radius = new float[4];

        public Rectangle(Int2 pos, Int2 size,
                         RGBA c1, RGBA c2, RGBA c3, RGBA c4,
                         float r1, float r2, float r3, float r4)
        {
            this.pos    = pos;
            this.size   = size;
            this.colour = new RGBA[]{c1,c2,c3,c4};
            this.radius = new float[]{r1, r2, r3, r4};
        }
        public Rectangle(Int2 pos, Int2 size, RGBA c, float r) {
            this(pos, size, c, c, c, c, r, r, r, r);
        }
    }
    //====================================================================
    public int getNumRectangles() {
        return rectangles.size();
    }

    public RoundRectangleRenderer() {
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
    public RoundRectangleRenderer setVP(Matrix4f viewProj) {
        prog.use().setUniform("VP", viewProj);
        return this;
    }
    public RoundRectangleRenderer addRectangle(Rectangle r) {
        rectangles.add(r);
        verticesChanged = true;
        return this;
    }
    public RoundRectangleRenderer setRectangle(int index, Rectangle r) {
        rectangles.set(index, r);
        verticesChanged = true;
        return this;
    }
    public RoundRectangleRenderer removeRectangle(int index) {
        if(index < rectangles.size()) {
            rectangles.remove(index);
            verticesChanged = true;
        }
        return this;
    }
    public RoundRectangleRenderer clearRectangles() {
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
    //===================================================================
    private void populateVbo() {
        if(!verticesChanged) return;

        final int FLOAT = 4;
        final int VEC2  = 2*4;
        final int VEC4  = 4*4;

        final int VERTEX_SIZE = VEC2 +  // vertex pos
                                FLOAT + // vertex radius
                                VEC4 +  // rect pos and size
                                VEC4;   // vertex colour

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
            putFloats(buffer, q.pos);                       // 0
            putFloats(buffer, q.radius[0]);
            putFloats(buffer, q.pos);
            putFloats(buffer, q.size);
            putFloats(buffer, q.colour[0]);

            putFloats(buffer, q.pos.add(0, q.size.getY())); // 3
            putFloats(buffer, q.radius[3]);
            putFloats(buffer, q.pos);
            putFloats(buffer, q.size);
            putFloats(buffer, q.colour[3]);

            putFloats(buffer, q.pos.add(q.size.getX(), 0)); // 1
            putFloats(buffer, q.radius[1]);
            putFloats(buffer, q.pos);
            putFloats(buffer, q.size);
            putFloats(buffer, q.colour[1]);



            putFloats(buffer, q.pos.add(q.size.getX(), 0)); // 1
            putFloats(buffer, q.radius[1]);
            putFloats(buffer, q.pos);
            putFloats(buffer, q.size);
            putFloats(buffer, q.colour[1]);

            putFloats(buffer, q.pos.add(0, q.size.getY())); // 3
            putFloats(buffer, q.radius[3]);
            putFloats(buffer, q.pos);
            putFloats(buffer, q.size);
            putFloats(buffer, q.colour[3]);

            putFloats(buffer, q.pos.add(q.size));           // 2
            putFloats(buffer, q.radius[2]);
            putFloats(buffer, q.pos);
            putFloats(buffer, q.size);
            putFloats(buffer, q.colour[2]);

        });
        buffer.position(0);
        vbo.addData(buffer);

        vao.enableAttrib(0, 2, GL_FLOAT, false, VERTEX_SIZE, 0);
        vao.enableAttrib(1, 1, GL_FLOAT, false, VERTEX_SIZE, VEC2);
        vao.enableAttrib(2, 4, GL_FLOAT, false, VERTEX_SIZE, VEC2+FLOAT);
        vao.enableAttrib(3, 4, GL_FLOAT, false, VERTEX_SIZE, VEC2+FLOAT+VEC4);

        verticesChanged = false;
    }
    private static final String VS =
        "#version 330 core\n" +
            "" +
            "    layout(location = 0) in vec2 pos;" +
            "    layout(location = 1) in float radius;" +
            "    layout(location = 2) in vec4 rectPosAndSize;" +
            "    layout(location = 3) in vec4 colour;" +
            "" +
            "    uniform mat4 VP;" +
            "" +
            "    out VS_OUT {" +
            "        vec2 pixelPos;" +
            "        flat vec2 rectPos;" +
            "        flat vec2 rectSize;" +
            "        vec4 colour;" +
            "        float radius;" +
            "    } vs_out;" +
            "" +
            "    void main() {" +
            "        gl_Position     = VP * vec4(pos,0,1);" +
            "        vs_out.pixelPos = pos;" +
            "        vs_out.rectPos  = rectPosAndSize.xy;" +
            "        vs_out.rectSize = rectPosAndSize.zw;" +
            "        vs_out.colour   = colour;" +
            "        vs_out.radius   = radius;" +
            "    }" +
            "";
    private static final String FS =
        "#version 330 core\n" +
            "" +
            "    in VS_OUT {" +
            "        vec2 pixelPos;" +
            "        flat vec2 rectPos;" +
            "        flat vec2 rectSize;" +
            "        vec4 colour;" +
            "        float radius;" +
            "    } fs_in;" +
            "" +
            "    out vec4 color;" +
            "" +
            "    void main() {" +
            "        // assuming an axis-aligned rectangle\n" +
            "        vec2 pos  = fs_in.pixelPos-fs_in.rectPos;" +
            "        vec2 size = fs_in.rectSize;" +
            "        vec2 mid  = size/2;" +
            "" +
            "        vec2 top    = fs_in.radius.xx;" +
            "        float alpha = fs_in.colour.a;" +
            "" +
            "        if(pos.x>mid.x) {" +
            "            pos.x = size.x-pos.x;" +
            "        }" +
            "        if(pos.y>mid.y) {" +
            "            pos.y = size.y-pos.y;" +
            "        }" +
            "" +
            "        float dfc = distance(pos, top);" +
            "" +
            "        if(pos.x<fs_in.radius && pos.y<fs_in.radius) {" +
            "            // we are in a corner\n" +
            "            float v = fs_in.radius-dfc;" +
            "            alpha   = clamp(v, 0, alpha);" +
            "        }" +
            "" +
            "        color = vec4(fs_in.colour.rgb, alpha);" +
            "    }";
}
