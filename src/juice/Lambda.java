package juice;

final public class Lambda {
    public interface VOID {
        void call();
    }
    public interface VOIDThrows {
        void call() throws Exception;
    }
    public interface R <R> {
        R call();
    }
    public interface RThrows <R> {
        R call() throws Exception;
    }
    public interface A <A1> {
        void call(A1 a1);
    }
    public interface AR <A1,R> {
        R call(A1 a1);
    }
    public interface AAR <A1,A2,R> {
        R call(A1 a1, A2 a2);
    }
}
