import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;

public class HttpPost {


    public void post(String req,boolean sync) throws InterruptedException {
        final ConditionEvent cond = new ConditionEvent(req);
        final Object o = cond.reposGetter.get(DomainRepository.class);
        final DomainContext ctx = DomainContext.class.cast(o);
        final Consumer<Throwable> failedHandler = (e)->{};
        final WeakReference<CountDownLatch> ref = sync
                ?new WeakReference<>(new CountDownLatch(1))
                :new WeakReference<>(null);
        GlobalSync.execute(Transaction.wrapper(()->{
            try{
                final PostEvent evt = new PostEvent(ctx,req,sync);
                if(!get(evt.type()).check(cond)){
                    failedHandler.accept(null);
                    return;
                }
                emit(evt).forEach(Runnable::run);
            }catch(Throwable t){
                failedHandler.accept(t);
                throw t;
            }finally {
                if(sync){
                    final CountDownLatch l = ref.get();
                    assert l != null;
                    l.countDown();
                }
            }
        }));
        if(sync){
            final CountDownLatch l = ref.get();
            assert l != null;
            l.await();
            System.out.println(ctx.result);
        }else{
            System.out.println("http code:200");
        }
    }
    public void postInThreadPool(String req){
        final ConditionEvent cond = new ConditionEvent(req);
        final Object o = cond.reposGetter.get(DomainRepository.class);
        final DomainContext ctx = DomainContext.class.cast(o);
        final Consumer<Throwable> failedHandler = (e)->{};
        try{
            final PostEvent evt = new PostEvent(ctx,req,false);
            if(!get(evt.type()).check(cond)){
                failedHandler.accept(null);
                return;
            }
            Transaction.wrapper(()-> emit(evt).forEach(Runnable::run)).run();
        }catch(Throwable t){
            failedHandler.accept(t);
        }
    }
    static List<Runnable> emit(Event e){
        final List<Listener> all = Listener.all();
        if(all.isEmpty())return Collections.emptyList();
        return all.stream()
                .map(l->l.onEvent(e))
                .filter(Objects::nonNull)
                .toList();
    }
    //get checker by command id
    static ConditionEventChecker get(int cmdId){
        return new ConditionEventChecker();
    }
}

