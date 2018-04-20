package juice;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public final class VAO {
    private int id;
    private int numAttribsEnabled;

    public VAO() {
        this.id = glGenVertexArrays();
    }
    public void destroy() {
        unbind();
        glDeleteVertexArrays(new int[]{id});
    }
    public void bind() {
        glBindVertexArray(id);
    }
    public void unbind() {
        glBindVertexArray(0);
    }
    public void enableAttrib(int index, int elementSize, int elementType, boolean normalise, int stride, long offset) {
        glEnableVertexAttribArray(index);
        glVertexAttribPointer(index, elementSize, elementType, normalise, stride, offset);
        numAttribsEnabled = numAttribsEnabled++;
    }
    public void enableAttrib(int index, int elementSize, int elementType, boolean normalise, int stride) {
        enableAttrib(index, elementSize, elementType, normalise, stride, 0);
    }
    public void enableAttrib(int index, int elementSize, int elementType, boolean normalise) {
        enableAttrib(index, elementSize, elementType, normalise, 0, 0);
    }
    public void enableAttrib(int index, int elementSize, int elementType) {
        enableAttrib(index, elementSize, elementType, false, 0, 0);
    }
    public void enableAttrib(int index, int elementSize) {
        enableAttrib(index, elementSize, GL_FLOAT, false, 0, 0);
    }
    public void disableAttribs() {
        if(numAttribsEnabled == 0) return;

        for(int i = 0; i < numAttribsEnabled; i++) {
            glDisableVertexAttribArray(i);
        }

        numAttribsEnabled = 0;
    }
}
