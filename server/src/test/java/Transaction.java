import java.util.Collections;
import java.util.List;

public class Transaction {
    public static Runnable wrapper(Runnable... rs){
//     TransactionTemplate
        return ()->{
            //begin transaction
            try{
                for (Runnable r : rs) {
                    r.run();
                }
            }catch (Throwable t){
                //rollback transaction
                t.printStackTrace(System.err);
                return;
            }
            //end transaction
            System.out.println("1");
        };
    }
}
class DomainRepository{}
class DomainContext{
    Object result;
}
interface PostEventAccessor{
}
class PostEvent implements Event,PostEventAccessor{
    private final DomainContext mCtx;
    private final String mReq;
    private final boolean mSync;
    PostEvent(DomainContext mCtx, String mReq, boolean mSync) {
        this.mCtx = mCtx;
        this.mReq = mReq;
        this.mSync = mSync;
    }
}
interface Event{
    default int type(){ return 0;}
}
interface Listener{
    Runnable onEvent(Event e);
    static List<Listener> all(){ return Collections.emptyList();}
}