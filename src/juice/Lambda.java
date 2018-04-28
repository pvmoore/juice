package juice;

final public class Lambda {
    public interface V {
        void call();
    }
    public interface V_Throws {
        void call() throws Exception;
    }
    public interface R <R> {
        R call();
    }
    public interface R_Throws<R> {
        R call() throws Exception;
    }
    public interface AV<A1> {
        void call(A1 a1);
    }
    public interface AR <A1,R> {
        R call(A1 a1);
    }
    public interface AAR <A1,A2,R> {
        R call(A1 a1, A2 a2);
    }
}
