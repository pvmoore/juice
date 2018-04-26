package juice.renderers;

import juice.Font;
import juice.GLShaderProgram;
import juice.VAO;
import juice.VBO;
import juice.types.RGBA;
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

final public class TextRenderer {
    private VAO vao;
    private VBO vbo;
    private ByteBuffer buffer;
    private GLShaderProgram prog, dsProg;
    private Font font;
    private List<Chunk> chunks = new ArrayList<>();
    private boolean textChanged = true;
    private int numCharacters;
    private RGBA colour = RGBA.WHITE;
    private float size;

    private static final class Chunk {
        String text;
        int x, y;
        RGBA colour;
        float size;
    }

    public TextRenderer(Font font) {
        this.font = font;
        this.size = font.size;
        this.vao = new VAO();
        this.prog = new GLShaderProgram()
            .shaderSource(GL_VERTEX_SHADER, VS)
            .shaderSource(GL_FRAGMENT_SHADER, FS)
            .link();
        this.dsProg = new GLShaderProgram()
            .shaderSource(GL_VERTEX_SHADER, VS)
            .shaderSource(GL_FRAGMENT_SHADER, FS_DropShadow)
            .link();

        prog.use().setUniform("SAMPLER0", 0);
        dsProg.use().setUniform("SAMPLER0", 0);
    }
    public void destroy() {
        if(vbo!=null) vbo.destroy();
        vao.destroy();
        prog.destroy();
        dsProg.destroy();
    }
    public TextRenderer setVP(Matrix4f viewProj) {
        prog.use().setUniform("VP", viewProj);
        dsProg.use().setUniform("VP", viewProj);
        return this;
    }
    public TextRenderer setColour(RGBA c) {
        this.colour = c;
        return this;
    }
    public TextRenderer setSize(int size) {
        this.size = size;
        return this;
    }
    public TextRenderer appendText(String text, int x, int y) {
        var ch = new Chunk();
        ch.text = text;
        ch.x = x;
        ch.y = y;
        ch.colour = colour;
        ch.size = size;
        chunks.add(ch);
        textChanged = true;
        return this;
    }
    public TextRenderer replaceText(int index, String text) {
        var ch = chunks.get(index);
        ch.text = text;
        textChanged = true;
        return this;
    }
    public TextRenderer clearText() {
        chunks.clear();
        textChanged = true;
        return this;
    }
    public void render() {
        if(chunks.size()==0) return;
        vao.bind();
        populateVbo();

        glActiveTexture(GL_TEXTURE0 + 0);
        glBindTexture(GL_TEXTURE_2D, font.texture.id);

        // Drop shadow
        dsProg.use();
        glDrawArrays(GL_TRIANGLES, 0, numCharacters * 6);   // 6 vertices per char

        // Normal
        prog.use();
        glDrawArrays(GL_TRIANGLES, 0, numCharacters * 6);   // 6 vertices per char
    }
    //====================================================================================
    private void populateVbo() {
        if(!textChanged) return;

        numCharacters = chunks.stream().mapToInt(it->it.text.length()).sum();

        final int VERTEX_SIZE = 2 * 4 +
                                2 * 4 +
                                4 * 4 +
                                1 * 4;

        int bytesRequired = numCharacters * VERTEX_SIZE * 6;

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
        chunks.forEach(chunk -> {

            float X  = (float)chunk.x;
            float Y  = (float)chunk.y;
            int i    = 0;

            for(var c : chunk.text.toCharArray()) {

                var g       = font.page.getChar(c);
                float ratio = (chunk.size / (float)font.size);
                float x     = X + g.xoffset * ratio;
                float y     = Y + g.yoffset * ratio;
                float w     = g.width * ratio;
                float h     = g.height * ratio;

                // 0--2
                // | /|
                // |/ |
                // 1--3
                //
                buffer.putFloat(x); buffer.putFloat(y);                   // 0
                buffer.putFloat(g.u); buffer.putFloat(g.v);
                buffer.putFloat(chunk.colour.r); buffer.putFloat(chunk.colour.g);
                buffer.putFloat(chunk.colour.b); buffer.putFloat(chunk.colour.a);
                buffer.putFloat(chunk.size);

                buffer.putFloat(x); buffer.putFloat(y+h);          // 1
                buffer.putFloat(g.u); buffer.putFloat(g.v2);
                buffer.putFloat(chunk.colour.r); buffer.putFloat(chunk.colour.g);
                buffer.putFloat(chunk.colour.b); buffer.putFloat(chunk.colour.a);
                buffer.putFloat(chunk.size);

                buffer.putFloat(x+w); buffer.putFloat(y);          // 2
                buffer.putFloat(g.u2); buffer.putFloat(g.v);
                buffer.putFloat(chunk.colour.r); buffer.putFloat(chunk.colour.g);
                buffer.putFloat(chunk.colour.b); buffer.putFloat(chunk.colour.a);
                buffer.putFloat(chunk.size);



                buffer.putFloat(x+w); buffer.putFloat(y);          // 2
                buffer.putFloat(g.u2); buffer.putFloat(g.v);
                buffer.putFloat(chunk.colour.r); buffer.putFloat(chunk.colour.g);
                buffer.putFloat(chunk.colour.b); buffer.putFloat(chunk.colour.a);
                buffer.putFloat(chunk.size);

                buffer.putFloat(x); buffer.putFloat(y+h);          // 1
                buffer.putFloat(g.u); buffer.putFloat(g.v2);
                buffer.putFloat(chunk.colour.r); buffer.putFloat(chunk.colour.g);
                buffer.putFloat(chunk.colour.b); buffer.putFloat(chunk.colour.a);
                buffer.putFloat(chunk.size);

                buffer.putFloat(x+w); buffer.putFloat(y+h); // 3
                buffer.putFloat(g.u2); buffer.putFloat(g.v2);
                buffer.putFloat(chunk.colour.r); buffer.putFloat(chunk.colour.g);
                buffer.putFloat(chunk.colour.b); buffer.putFloat(chunk.colour.a);
                buffer.putFloat(chunk.size);

                int kerning = 0;
                if(i+1<chunk.text.length()) {
                    kerning = font.page.getKerning(c, chunk.text.charAt(i + 1));
                }

                X += (g.xadvance + kerning) * ratio;
                i++;
            }
        });
        buffer.position(0);
        vbo.addData(buffer);

        vao.enableAttrib(0, 2, GL_FLOAT, false, VERTEX_SIZE, 0);
        vao.enableAttrib(1, 2, GL_FLOAT, false, VERTEX_SIZE, 2*4);
        vao.enableAttrib(2, 4, GL_FLOAT, false, VERTEX_SIZE, 2*4 + 2*4);
        vao.enableAttrib(3, 1, GL_FLOAT, false, VERTEX_SIZE, 2*4 + 2*4 + 4*4);
        textChanged = false;
    }
    private static final String VS = "#version 330 core\n" +
        "    layout(location = 0) in vec2 pos;" +
        "    layout(location = 1) in vec2 uv;" +
        "    layout(location = 2) in vec4 colour;" +
        "    layout(location = 3) in float size;" +
        "" +
        "    uniform mat4 VP;" +
        "" +
        "    out VS_OUT {" +
        "        vec2 uv;" +
        "        vec4 colour;" +
        "        float size;" +
        "    } vs_out;" +
        "" +
        "    void main() {" +
        "        gl_Position   = VP * vec4(pos,0,1);" +
        "        vs_out.uv     = uv;" +
        "        vs_out.colour = colour;" +
        "        vs_out.size   = size;" +
        "    }" +
        "";
    private static final String FS = "#version 330 core\n" +
        "" +
        "    in VS_OUT {" +
        "        vec2 uv;" +
        "        vec4 colour;" +
        "        float size;" +
        "    } fs_in;" +
        "" +
        "    out vec4 color;" +
        "    uniform sampler2D SAMPLER0;" +
        "" +
        "    void main() {" +
        "        float smoothing = (1.0 / (0.25*fs_in.size));" +
        "        float distance  = texture(SAMPLER0, fs_in.uv).a;" +
        "        float alpha     = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);" +
	    "        color           = vec4(fs_in.colour.rgb, fs_in.colour.a * alpha);" +
        "    }";
    private static final String FS_DropShadow = "#version 330 core\n" +
        "" +
        "    in VS_OUT {" +
        "        vec2 uv;" +
        "        vec4 colour;" +
        "        float size;" +
        "    } fs_in;" +
        "" +
        "    out vec4 color;" +
        "    uniform sampler2D SAMPLER0;" +
        "    uniform vec4 dsColour = vec4(0,0,0, 0.75);" +
        "    uniform vec2 dsOffset = vec2(-0.0025, 0.0025);" +
        "" +
        "    void main() {" +
        "        vec2 offset     = dsOffset;" +
        "        float smoothing = (1.0 / (0.25*fs_in.size)) * fs_in.size / 12;" +
        "        float distance  = texture(SAMPLER0, fs_in.uv - offset).a;" +
        "        float alpha     = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);" +
        "        vec4 col        = dsColour;" +
        "        color           = vec4(col.rgb, col.a * alpha);" +
        "    }";
}
