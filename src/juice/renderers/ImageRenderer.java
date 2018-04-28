package juice.renderers;

import juice.GLShaderProgram;
import juice.Texture;
import juice.VAO;
import juice.VBO;
import juice.types.RGBA;
import juice.types.Rect;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

/**
 * Draw textured quads. All quads use the same texture.
 */
final public class ImageRenderer {
    private VAO vao;
    private VBO vbo;
    private ByteBuffer buffer;
    private GLShaderProgram prog;
    private Texture texture;
    private List<Quad> quads = new ArrayList<>();
    private boolean quadsChanged = true;

    private class Quad {
        Rect<Integer> rect;
        Rect<Float> uvs;
        RGBA colour;
    }
    //====================================================================================
    public int getNumQuads() {
        return quads.size();
    }

    public ImageRenderer(Texture t) {
        this.texture = t;
        this.vao = new VAO();
        this.prog = new GLShaderProgram()
            .shaderSource(GL_VERTEX_SHADER, VS)
            .shaderSource(GL_FRAGMENT_SHADER, FS)
            .link();

        prog.use().setUniform("SAMPLER0", 0);
    }
    public void destroy() {
        if(vbo!=null) vbo.destroy();
        vao.destroy();
        prog.destroy();
    }
    public ImageRenderer setVP(Matrix4f viewProj) {
        prog.use().setUniform("VP", viewProj);
        return this;
    }
    public ImageRenderer addQuad(Rect<Integer> rect, Rect<Float> uvs, RGBA colour) {
        Quad q = new Quad();
        q.rect = rect;
        q.uvs = uvs;
        q.colour = colour;
        quads.add(q);
        quadsChanged = true;
        return this;
    }
    public ImageRenderer setQuad(int index, Rect<Integer> rect, Rect<Float> uvs, RGBA colour) {
        Quad q = new Quad();
        q.rect = rect;
        q.uvs = uvs;
        q.colour = colour;
        quads.set(index, q);
        quadsChanged = true;
        return this;
    }
    public ImageRenderer removeQuad(int index) {
        quads.remove(index);
        quadsChanged = true;
        return this;
    }
    public ImageRenderer clearQuads() {
        quads.clear();
        quadsChanged = true;
        return this;
    }
    public void render() {
        if(quads.size()==0) return;
        vao.bind();
        prog.use();
        populateVbo();

        glActiveTexture(GL_TEXTURE0 + 0);
        glBindTexture(GL_TEXTURE_2D, texture.id);
        glDrawArrays(GL_TRIANGLES, 0, quads.size() * 6);   // 6 vertices
    }
    //====================================================================================
    private void populateVbo() {
        if(!quadsChanged) return;

        final int VERTEX_SIZE = 2 * 4 +
                                2 * 4 +
                                4 * 4;

        int bytesRequired = quads.size() * VERTEX_SIZE * 6;

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
        quads.forEach(q -> {
            // 0--2
            // | /|
            // |/ |
            // 1--3
            //
            buffer.putFloat(q.rect.x); buffer.putFloat(q.rect.y);                   // 0
            buffer.putFloat(q.uvs.x); buffer.putFloat(q.uvs.y);
            buffer.putFloat(q.colour.r); buffer.putFloat(q.colour.g);
            buffer.putFloat(q.colour.b); buffer.putFloat(q.colour.a);

            buffer.putFloat(q.rect.x); buffer.putFloat(q.rect.y+q.rect.h);          // 1
            buffer.putFloat(q.uvs.x); buffer.putFloat(q.uvs.y+q.uvs.h);
            buffer.putFloat(q.colour.r); buffer.putFloat(q.colour.g);
            buffer.putFloat(q.colour.b); buffer.putFloat(q.colour.a);

            buffer.putFloat(q.rect.x+q.rect.w); buffer.putFloat(q.rect.y);          // 2
            buffer.putFloat(q.uvs.x+q.uvs.w); buffer.putFloat(q.uvs.y);
            buffer.putFloat(q.colour.r); buffer.putFloat(q.colour.g);
            buffer.putFloat(q.colour.b); buffer.putFloat(q.colour.a);



            buffer.putFloat(q.rect.x+q.rect.w); buffer.putFloat(q.rect.y);          // 2
            buffer.putFloat(q.uvs.x+q.uvs.w); buffer.putFloat(q.uvs.y);
            buffer.putFloat(q.colour.r); buffer.putFloat(q.colour.g);
            buffer.putFloat(q.colour.b); buffer.putFloat(q.colour.a);

            buffer.putFloat(q.rect.x); buffer.putFloat(q.rect.y+q.rect.h);          // 1
            buffer.putFloat(q.uvs.x); buffer.putFloat(q.uvs.y+q.uvs.h);
            buffer.putFloat(q.colour.r); buffer.putFloat(q.colour.g);
            buffer.putFloat(q.colour.b); buffer.putFloat(q.colour.a);

            buffer.putFloat(q.rect.x+q.rect.w); buffer.putFloat(q.rect.y+q.rect.h); // 3
            buffer.putFloat(q.uvs.x+q.uvs.w); buffer.putFloat(q.uvs.y+q.uvs.h);
            buffer.putFloat(q.colour.r); buffer.putFloat(q.colour.g);
            buffer.putFloat(q.colour.b); buffer.putFloat(q.colour.a);
        });
        buffer.position(0);
        vbo.addData(buffer);

        vao.enableAttrib(0, 2, GL_FLOAT, false, VERTEX_SIZE, 0);
        vao.enableAttrib(1, 2, GL_FLOAT, false, VERTEX_SIZE, 2*4);
        vao.enableAttrib(2, 4, GL_FLOAT, false, VERTEX_SIZE, 2*4 + 2*4);
        quadsChanged = false;
    }
    //====================================================================================
    private static final String VS =
        "#version 330 core\n" +
        "" +
        "    layout(location = 0) in vec2 pos;" +
        "    layout(location = 1) in vec2 uvs;" +
        "    layout(location = 2) in vec4 colour;" +
        "" +
        "    uniform mat4 VP;" +
        "" +
        "    out VS_OUT {" +
        "        vec2 uvs;" +
        "        vec4 colour;" +
        "    } vs_out;" +
        "" +
        "    void main() {" +
        "        gl_Position   = VP * vec4(pos,0,1);" +
        "        vs_out.uvs    = uvs;" +
        "        vs_out.colour = colour;" +
        "    }" +
        "";
    private static final String FS =
        "#version 330 core\n" +
        "" +
        "    in VS_OUT {" +
        "        vec2 uvs;" +
        "        vec4 colour;" +
        "    } fs_in;" +
        "" +
        "    out vec4 color;" +
        "    uniform sampler2D SAMPLER0;" +
        "" +
        "    void main() {" +
        "        color = texture(SAMPLER0, fs_in.uvs) * fs_in.colour;" +
        "    }";
}
