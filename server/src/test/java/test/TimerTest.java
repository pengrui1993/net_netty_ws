package test;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class TimerTest {
    static TimerTask t;
    static Timer timer = new Timer();

    static void test(){
        TimerTask task = t=new TimerTask() {
            @Override
            public void run() {
                System.out.println("TimerTest.run");
            }
        };
        timer.schedule(task,1000);
    }
    public static void main(String[] args) throws IOException {
        test();
        System.in.read();
        timer.cancel();
        System.out.println("done");

    }
}
