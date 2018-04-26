package juice.events;

import juice.Lambda;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Finish me.
 */
final public class Events {
    private static Events instance = null;

    public static Events get() {
        if(instance == null) {
            instance = new Events();
        }
        return instance;
    }
    //=============================================================================
    private Events() {}

    private ConcurrentLinkedDeque<Lambda.A> queue = new ConcurrentLinkedDeque<>();
}
