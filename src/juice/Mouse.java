package juice;

import juice.types.Int2;

final public class Mouse {

    public enum EventType {
        BUTTON_PRESS, BUTTON_RELEASE, MOVE, WHEEL
    }
    public static final class Event {
        public EventType type;
        public Int2 pos;
        public int button;
        public Window.Modifier mods;
        public int wheel;
        public static Event move(Int2 pos) {
            var e  = new Event();
            e.type = EventType.MOVE;
            e.pos  = pos;
            return e;
        }
        public static Event button(int b, boolean press, Window.Modifier mods, Int2 pos) {
            var e = new Event();
            e.type = press ? EventType.BUTTON_PRESS : EventType.BUTTON_RELEASE;
            e.pos = pos;
            e.button = b;
            e.mods = mods;
            return e;
        }
        public static Event wheel(int delta, Int2 pos) {
            var e = new Event();
            e.type = EventType.WHEEL;
            e.pos = pos;
            e.wheel = delta;
            return e;
        }
    }
}
