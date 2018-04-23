package juice.animation;

import juice.animation.easing.EasingExponential;
import juice.animation.easing.IEasing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final public class Animation {
    public enum EndPolicy {
        STOP, RESTART, DISCARD
    }
    //=========================================================================
    private boolean running = false;
    private double[] startValues;
    private IEasing easing;
    private double fps;
    private List<Key> keys = new ArrayList<>();
    private double currentFrame;
    private int lastFrame;
    private int currentKeyIndex;
    private Key previousKey;
    private Key currentKey;
    private EndPolicy endPolicy;
    //=========================================================================
    public Animation(int fps, double[] startValues, EndPolicy ep) {
        this.fps         = fps;
        this.startValues = startValues;
        this.endPolicy   = ep;
        this.easing      = new EasingExponential();
    }
    public Animation easing(IEasing e) {
        this.easing = e;
        return this;
    }
    public Animation addKey(Consumer<Key> c) {
        Key k = new Key();
        c.accept(k);
        keys.add(k);
        return this;
    }
    public Animation addKey(Key k) {
        keys.add(k);
        return this;
    }
    public void start() {
        if(keys.size()==0) return;
        reset(true);
    }
    public void pause() {
        running = false;
    }
    public void resume() {
        running = true;
    }
    public void reset(boolean andStart) {
        running         = andStart;
        currentKeyIndex = 0;
        currentFrame    = 0;
        lastFrame       = 0;
        previousKey     = new Key().values(startValues);
        nextKey();
    }
    public boolean update(double perSecond) {
        if(!running) return false;

        currentFrame += (perSecond * fps);
        int framesPassed = (int)currentFrame-lastFrame;

        if(framesPassed>0) {
            lastFrame = (int)currentFrame;
            double[] values = easing.get(currentFrame-previousKey.frame);

            if(currentKey.frameCallback!=null) {
                currentKey.frameCallback.call(lastFrame, values);
            }

            if(lastFrame>=currentKey.frame) {
                if(currentKey.endCallback!=null) {
                    currentKey.endCallback.call();
                }
                if(keys.size()>currentKeyIndex+1) {
                    currentKeyIndex++;
                    previousKey = currentKey;
                    nextKey();
                } else {
                    if(endPolicy==EndPolicy.STOP) {
                        reset(false);
                    } else if(endPolicy== EndPolicy.RESTART) {
                        reset(true);
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    //=========================================================================
    private void nextKey() {
        currentKey = keys.get(currentKeyIndex);
        easing.set(currentKey.easingType,
                   previousKey.values,
                   currentKey.values,
                   currentKey.frame-previousKey.frame);
    }
}
