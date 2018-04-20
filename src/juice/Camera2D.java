package juice;

import juice.types.Int2;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class Camera2D {
    private Vector2f _position;
    private Vector2f up = new Vector2f(0, 1);
    private float rotationDegrees = 0;
    private float _zoomFactor = 1;
    private Int2 _windowSize;
    private Matrix4f view;
    private Matrix4f proj;
    private Matrix4f viewProj;
    private boolean recalculateView = true;
    private boolean recalculateProj = true;

    public float zoomFactor() { return 1 / _zoomFactor; }
    public Vector2f position() { return _position; }
    public Int2 windowSize() { return _windowSize; }

    public Camera2D(Int2 windowSize) {
        this._windowSize = windowSize;
        this._position = new Vector2f(_windowSize.getX() / 2, _windowSize.getY() / 2);
    }
    public Camera2D moveTo(float x, float y) {
        _position = new Vector2f(x, y);
        recalculateView = true;
        return this;
    }
    public Camera2D moveBy(float x, float y) {
        return moveTo(_position.x + x, _position.y + y);
    }
    public Camera2D zoomOut(float z) {
        this._zoomFactor += z;
        recalculateProj = true;
        return this;
    }
    public Camera2D zoomIn(float z) {
        if(_zoomFactor == 0.01f) return this;
        _zoomFactor -= z;
        if(_zoomFactor < 0.01f) {
            _zoomFactor = 0.01f;
        }
        recalculateProj = true;
        return this;
    }
    public Camera2D setZoom(float z) {
        this._zoomFactor = 1f / z;
        recalculateProj = true;
        recalculateView = true;
        return this;
    }
    public Camera2D rotateTo(float degrees) {
        this.rotationDegrees = degrees;
        Vector4f tempUp = new Vector4f(0, 1, 0, 0);
        Matrix4f rotateZ = new Matrix4f().rotate(Util.toRadians(rotationDegrees), 0, 0, 1);
        tempUp.mul(rotateZ);
        this.up = new Vector2f(tempUp.x, tempUp.y);
        recalculateView = true;
        return this;
    }
    public Camera2D rotateBy(float degrees) {
        return rotateTo(rotationDegrees + degrees);
    }
    public void screenResized(Int2 windowSize) {
        recalculateProj = true;
        this._windowSize = windowSize;
        this._position = new Vector2f(_windowSize.getX() / 2, _windowSize.getY() / 2);
    }
    public Matrix4f P() {
        if(recalculateProj) {
            float width = _windowSize.getX() * _zoomFactor;
            float height = _windowSize.getY() * _zoomFactor;
            proj = new Matrix4f().ortho(-width / 2, width / 2, height / 2, -height / 2, 0f, 100f);
            recalculateProj = false;
        }
        return proj;
    }
    public Matrix4f V() {
        if(recalculateView) {
            view = new Matrix4f().lookAt(new Vector3f(_position, 1), new Vector3f(_position, 0), new Vector3f(up, 0));
            recalculateView = false;
        }
        return view;
    }
    public Matrix4f VP() {
        if (recalculateView || recalculateProj || viewProj==null) {
            V();
            P();
            viewProj = proj.mul(view, new Matrix4f());
        }
        return viewProj;
    }
}
