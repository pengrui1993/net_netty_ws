import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Recursive implements Supplier<Recursive>{
    private int i=0;
    @Override
    public Recursive get() {
        if(i++<10)
            return this;
        return null;
    }

    static void print(Object obj){
        System.out.print(obj+" ");
    }
    static void entity(){
        Recursive p = new Recursive();
        while(Objects.nonNull(p=p.get())){
            print(p.i);
        }
    }
    public static void main(String[] args) {
        boolean[][] allCondition = {
                {true,true}
                ,{true,false}
                ,{false,true}
                ,{false,false}
        };
        for (boolean[] a : allCondition) {
            asc = a[0];
            stackCopy = a[1];
            System.out.printf("order asc:%s,use stack copy:%s\n",asc,stackCopy);
            Recursive p = new Recursive();
            recursive(p, Recursive::print);
            System.out.println();
        }

    }
    static boolean asc = true;
    static boolean stackCopy = true;
    public static void recursive(Recursive r, Consumer<Object> c){
        if(r.i++<10){
            if(asc){
                if(stackCopy){
                    final int i = r.i;
                    recursive(r,c);
                    c.accept(i);
                }else{
                    recursive(r,c);
                    c.accept(r.i);
                }
            }else{
                c.accept(r.i);
                recursive(r,c);
            }
        }
    }
}
