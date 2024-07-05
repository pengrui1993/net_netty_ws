import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class GlobalSync extends Thread {
    public static void execute(Runnable r){
        if(Objects.isNull(r))
            return;
        try {
            getInstance().queue.put(r);
        } catch (InterruptedException e) {
            Optional.ofNullable(Holder.EX_HANDLER).ifPresent(h->h.accept(e));
        }
    }
    public static Consumer<InterruptedException> set(Consumer<InterruptedException> h){
        final Consumer<InterruptedException> older = Holder.EX_HANDLER;
        Holder.EX_HANDLER =h;
        return older;
    }

    private static GlobalSync getInstance(){
        return Holder.INSTANCE;
    }
    @Override
    public void run() {
        while(true){
            try {
                Runnable take = queue.take();
                take.run();
            }catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }catch (Throwable t){
                t.printStackTrace(System.out);
            }
        }
    }
    private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    static class Holder{
        static GlobalSync INSTANCE = new GlobalSync();
        static Consumer<InterruptedException> EX_HANDLER =(e)->e.printStackTrace(System.err);
        static{
            GlobalSync ref = INSTANCE;
            ref.setName("backend synchronizer");
            ref.setDaemon(true);
            ref.start();
        }
    }

}
