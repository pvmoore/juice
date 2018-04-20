package juice;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.*;

public final class VBO {
    private int id;
    private int type;
    public long sizeBytes;

    private VBO(int type, long sizeBytes, int usage) {
        this.type = type;
        this.sizeBytes = sizeBytes;
        this.id = glGenBuffers();
        bind();
        glBufferData(type, sizeBytes, usage);
    }
    public static VBO makeArray(long sizeBytes, int usage) {
        return new VBO(GL_ARRAY_BUFFER, sizeBytes, usage);
    }
    public static VBO makeElements(long sizeBytes, int usage) {
        return new VBO(GL_ELEMENT_ARRAY_BUFFER, sizeBytes, usage);
    }
    public void destroy() {
        glDeleteBuffers(new int[]{id});
    }
    public VBO realloc(long sizeBytes, int usage) {
        this.sizeBytes = sizeBytes;
        glBufferData(type, sizeBytes, usage);
        return this;
    }
    public VBO bind() {
        glBindBuffer(type, id);
        return this;
    }
    public VBO addData(ByteBuffer data, long offset) {
        glBufferSubData(type, offset, data);
        return this;
    }
    public VBO addData(ByteBuffer data) {
        return addData(data, 0);
    }
}
