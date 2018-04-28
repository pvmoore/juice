package juice;

import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;

public class GLShaderProgram {
    private int id;
    private List<GLShader> shaders = new ArrayList<>();
    private FloatBuffer buffer2  = BufferUtils.createFloatBuffer(2);
    private FloatBuffer buffer3  = BufferUtils.createFloatBuffer(3);
    private FloatBuffer buffer9  = BufferUtils.createFloatBuffer(9);
    private FloatBuffer buffer16 = BufferUtils.createFloatBuffer(16);

    private class GLShader {
        int type;
        int id;

        GLShader(int type, String src) {
            this.type = type;
            this.id   = glCreateShader(type);
            compile(src);
        }
        void destroy() {
            glDeleteShader(id);
        }
        private void compile(String src) {
            PointerBuffer strings = BufferUtils.createPointerBuffer(1);
            IntBuffer lengths = BufferUtils.createIntBuffer(1);
            ByteBuffer source = MemoryUtil.memUTF8(src, false);

            strings.put(0, source);
            lengths.put(0, source.remaining());

            GL20.glShaderSource(id, strings, lengths);
            GL20.glCompileShader(id);
            int compiled = GL20.glGetShaderi(id, GL_COMPILE_STATUS);
            String shaderLog = GL20.glGetShaderInfoLog(id);
            if(shaderLog.trim().length() > 0) {
                System.err.println(shaderLog);
            }
            if(compiled == 0) {
                throw new AssertionError("Could not compile shader");
            }
        }
    }
    //====================================================================================
    public GLShaderProgram() {
        this.id = glCreateProgram();
    }
    public GLShaderProgram shaderSource(int type, String src) {
        shaders.add(new GLShader(type, src));
        return this;
    }
    public void destroy() {
        shaders.forEach(GLShader::destroy);
        shaders.clear();
        glUseProgram(0);
        glDeleteProgram(id);
    }
    public GLShaderProgram link() {
        shaders.forEach(s -> glAttachShader(id, s.id));
        glLinkProgram(id);
        int linked = glGetProgrami(id, GL_LINK_STATUS);
        String log = glGetProgramInfoLog(id);
        if(log.trim().length() > 0) {
            System.err.println(log);
        }
        if(linked == 0) {
            throw new RuntimeException("Could not link program");
        }
        shaders.forEach(GLShader::destroy);
        shaders.clear();
        return this;
    }
    public GLShaderProgram use() {
        glUseProgram(id);
        return this;
    }
    public GLShaderProgram setUniform(String name, int value) {
        glUniform1i(getLocation(name), value);
        return this;
    }
    public GLShaderProgram setUniform(String name, float value) {
        glUniform1f(getLocation(name), value);
        return this;
    }
    public GLShaderProgram setUniform(String name, Vector2f value) {
        glUniform2fv(getLocation(name), value.get(buffer2));
        return this;
    }
    public GLShaderProgram setUniform(String name, Vector3f value) {
        glUniform3fv(getLocation(name), value.get(buffer3));
        return this;
    }
    public GLShaderProgram setUniform(String name, Vector4f value) {
        var buf = BufferUtils.createFloatBuffer(4);
        glUniform4fv(getLocation(name), value.get(buf));
        return this;
    }
    public GLShaderProgram setUniform(String name, Matrix3f matrix) {
        buffer9.position(0);
        glUniformMatrix3fv(getLocation(name), false, matrix.get(buffer9));
        return this;
    }
    public GLShaderProgram setUniform(String name, Matrix4f matrix) {
        buffer16.position(0);
        glUniformMatrix4fv(getLocation(name), false, matrix.get(buffer16));
        return this;
    }
    //====================================================================================
    private int getLocation(String name) {
        int loc = glGetUniformLocation(id, name);
        if(loc < 0) System.out.println("WARN: Shader program uniform '" + name + "' not found");
        return loc;
    }
}
