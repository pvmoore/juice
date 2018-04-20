package juice.components;

import juice.*;
import juice.types.RGBA;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

/**
 * Manage a movable, textured image.
 */
public class Sprite extends UIComponent {
    private VAO vao;
    private VBO vbo;
    private ByteBuffer buffer;
    private GLShaderProgram prog;
    private Texture texture;
    private boolean modelMatrixChanged = true;
    private Matrix4f translation = new Matrix4f();
    private Matrix4f scale = new Matrix4f();

    public Sprite() {
        this.vao = new VAO();
        this.prog = new GLShaderProgram()
            .shaderSource(GL_VERTEX_SHADER, VS)
            .shaderSource(GL_FRAGMENT_SHADER, FS)
            .link();

        prog.use().setUniform("SAMPLER0", 0);

        populateVbo();
    }
    @Override public void destroy() {
        super.destroy();

        if(vao!=null) {
            if(vbo != null) vbo.destroy();
            vao.destroy();
            prog.destroy();
            vao = null;
        }
    }
    public Sprite setVP(Matrix4f viewProj) {
        prog.use().setUniform("VP", viewProj);
        return this;
    }
    public Sprite setColour(RGBA colour) {
        prog.use().setUniform("COLOUR", colour.toVector4f());
        return this;
    }
    public Sprite setTexture(Texture t) {
        this.texture = t;
        return this;
    }
    @Override public void onMoved() {
        modelMatrixChanged = true;
    }
    @Override public void onResized() {
        modelMatrixChanged = true;
    }

    @Override public void update(Frame frame) {
        super.update(frame);

        if(modelMatrixChanged) {
            modelMatrixChanged = false;
            var p = getPos();
            var s = getSize();

            translation.translation(p.getX(), p.getY(), 0f);
            scale.scaling(s.getX());

            var world = translation.mul(scale);

//            System.out.println("trans = "+translation);
//            System.out.println("scale = "+scale);
//            System.out.println("world = "+world);

            prog.use().setUniform("MODEL", world);
        }
    }
    @Override public void render(Frame frame) {
        vao.bind();
        prog.use();

        glActiveTexture(GL_TEXTURE0 + 0);
        glBindTexture(GL_TEXTURE_2D, texture.id);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);   // 4 vertices

        super.render(frame);
    }
    //====================================================================================
    private void populateVbo() {
        final int VERTEX_SIZE = 2 * 4 +
                                2 * 4;

        int bytesRequired = VERTEX_SIZE * 4;

        vao.bind();

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

        // 0--2
        // | /|
        // |/ |
        // 1--3
        //
        buffer.putFloat(0); buffer.putFloat(0); // 0
        buffer.putFloat(0); buffer.putFloat(0);

        buffer.putFloat(0); buffer.putFloat(1); // 1
        buffer.putFloat(0); buffer.putFloat(1);

        buffer.putFloat(1); buffer.putFloat(0); // 2
        buffer.putFloat(1); buffer.putFloat(0);

        buffer.putFloat(1); buffer.putFloat(1); // 3
        buffer.putFloat(1); buffer.putFloat(1);

        buffer.position(0);
        vbo.addData(buffer);

        vao.enableAttrib(0, 2, GL_FLOAT, false, VERTEX_SIZE, 0);
        vao.enableAttrib(1, 2, GL_FLOAT, false, VERTEX_SIZE, 2*4);
    }
    private static final String VS = "#version 330 core\n" +
        "    layout(location = 0) in vec2 pos;" +
        "    layout(location = 1) in vec2 uvs;" +
        "" +
        "    uniform mat4 VP;" +
        "    uniform mat4 MODEL;" +
        "" +
        "    out VS_OUT {" +
        "        vec2 uvs;" +
        "    } vs_out;" +
        "" +
        "    void main() {" +
        "        gl_Position = VP * MODEL * vec4(pos,0,1);" +
        "        vs_out.uvs  = uvs;" +
        "    }" +
        "";
    private static final String FS = "#version 330 core\n" +
        "" +
        "    in VS_OUT {" +
        "        vec2 uvs;" +
        "    } fs_in;" +
        "" +
        "    out vec4 color;" +
        "    uniform sampler2D SAMPLER0;" +
        "    uniform vec4 COLOUR = vec4(1,1,1,1);" +
        "" +
        "    void main() {" +
        "        color = texture(SAMPLER0, fs_in.uvs) * COLOUR;" +
        "    }";
}
