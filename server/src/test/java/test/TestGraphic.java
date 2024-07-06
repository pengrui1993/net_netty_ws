package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InvocationEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;

public class TestGraphic extends JFrame {
    //java ... --add-opens java.desktop/java.awt=ALL-UNNAMED
    public static void main(String[] args) throws NoSuchFieldException {
        TestGraphic g = new TestGraphic();
        g.setDefaultCloseOperation(TestGraphic.EXIT_ON_CLOSE);
        JButton b;
        g.add(b=new JButton("hello"));
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        b.setSize(100,100);
        g.setLocation(size.width/2,size.height/2);
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println(e.getX());
            }
        });
        g.pack();
        //Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(b,0,System.currentTimeMillis(),0,0,0,0,false));
        System.out.println(Toolkit.getDefaultToolkit());
        EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        Class<EventQueue> clazz = EventQueue.class;
        Field fq = clazz.getDeclaredField("queues");
        SwingUtilities.invokeLater(()-> System.out.println(clazz));
        EventQueue.invokeLater(()->{
            try {
                fq.setAccessible(true);
                Object o = fq.get(queue);
                Object[] qs = (Object[])o;
                Class<?> qc = qs[0].getClass();
                System.out.println(qc);
            } catch (IllegalAccessException e) {
                System.out.println(e.getLocalizedMessage());
            } catch (Throwable t){
                System.out.println(t.getMessage());
            }
        });
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
                new InvocationEvent(Toolkit.getDefaultToolkit(), ()-> System.out.println(fq)));
        g.setVisible(true);
    }
}
