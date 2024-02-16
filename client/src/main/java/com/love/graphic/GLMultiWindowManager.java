package com.love.graphic;

import com.love.core.Window;
import com.love.util.TimerWorker;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;

import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GLMultiWindowManager {
    static Thread main;

    private GLMultiWindowManager(){
    }
    static{
        init();
    }
    public static void init(){
        main = Thread.currentThread();
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();
        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        Optional.ofNullable(glfwSetMonitorCallback(monitorCallback)).ifPresent(NativeResource::close);
//        glfwMonitorCallback.close();
//        GLFWErrorCallback ec = glfwSetErrorCallback(ecb);
        Optional.ofNullable(glfwSetJoystickCallback(joystickCallback)).ifPresent(NativeResource::close);

    }
    private void resourceClose(NativeResource cb){
        if(Objects.isNull(cb))return;
        System.out.println("close callback:"+cb);
        cb.close();
    }
    void setIcon(int window, GLFWImage.Buffer buf){
        GLFWImage malloc = GLFWImage.malloc();
        //https://vimsky.com/examples/detail/java-class-org.lwjgl.glfw.GLFWImage.html
        malloc.close();
    }
    void register(WindowImpl w) {
        long hw = w.window;
        windows.put(hw,w);
//        glfwSetMonitorCallback()
//        glfwSetErrorCallback()
        resourceClose(glfwSetWindowPosCallback(hw, this.windowPosCallback));
        resourceClose(glfwSetWindowSizeCallback(hw, this.windowSizeCallback));
        resourceClose(glfwSetWindowCloseCallback(hw, this.windowCloseCallback));
        resourceClose(glfwSetWindowRefreshCallback(hw, this.windowRefreshCallback));
        resourceClose(glfwSetWindowFocusCallback(hw, this.windowFocusCallback));
        resourceClose(glfwSetWindowIconifyCallback(hw, this.windowIconifyCallback));
        resourceClose(glfwSetWindowMaximizeCallback(hw, this.windowMaximizeCallback));
        resourceClose(glfwSetFramebufferSizeCallback(hw, this.framebufferSizeCallback));
        resourceClose(glfwSetWindowContentScaleCallback(hw, this.windowContentScaleCallback));

        resourceClose(glfwSetKeyCallback(hw, this.keyCallback));
        resourceClose(glfwSetCharCallback(hw, this.charCallback));
        resourceClose(glfwSetCharModsCallback(hw, this.charModsCallback));
        resourceClose(glfwSetMouseButtonCallback(hw, this.mouseButtonCallback));
        resourceClose(glfwSetCursorPosCallback(hw, this.cursorPosCallback));
        resourceClose(glfwSetCursorEnterCallback(hw, this.cursorEnterCallback));
        resourceClose(glfwSetScrollCallback(hw, this.scrollCallback));
        resourceClose(glfwSetDropCallback(hw, this.dropCallback));
//        glfwSetJoystickCallback()

        callbacks.put(w.window,w);
    }



    final Map<Long,WindowImpl> windows = new HashMap<>();
    final Map<Long,Callbacks> callbacks = new HashMap<>();
    final ConcurrentLinkedDeque<Runnable> tasks = new ConcurrentLinkedDeque<>();
    // The window handle
    WindowImpl window;
    static final int X_INIT = 0;
    static final int Y_INIT = 30;
    private int lwx = X_INIT,lwy = Y_INIT;//last window x , last window y
    private final AtomicBoolean exit = new AtomicBoolean(false);
    void closeWindow(long window){

        final WindowImpl win = windows.get(window);
        if(Objects.isNull(win))return;
        final Runnable run = ()->{
            glfwSetWindowShouldClose(window, true);
            // Free the window callbacks and destroy the window
            glfwDestroyWindow(window);
            glfwFreeCallbacks(window);
            windows.remove(window);
            callbacks.remove(window);
        };
        if(Thread.currentThread()==main){
            run.run();
        }else{
            post(run);
        }

    }
    boolean debug = true;
    void createWindow(String name, WindowImpl win){

        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        if(debug) glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        long window = win.window = glfwCreateWindow(300, 300, name, NULL, NULL);
        if ( window == NULL ) throw new RuntimeException("Failed to create the GLFW window");
//        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        register(win);
        append(window);
//        setIcon(window,);
//        monitor();
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // Enable v-sync
        glfwShowWindow(window);// Make the window visible
//        glfwFocusWindow(window);
        GL.createCapabilities();
        // Set the clear color
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        this.window = win;
    }
    private void monitor(){
        PointerBuffer pb = glfwGetMonitors();
        assert null!=pb;
        for(int i=0;i<pb.limit();i++){
            GLFWVidMode m = glfwGetVideoMode(pb.get(i));
            assert null!=m;
            System.out.printf("%s,w:%d,h:%d,r:%d,s:%d\n",m,m.width(),m.height(),m.refreshRate(),m.sizeof());
        }
    }
    void append(long window){
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*
            glfwGetWindowSize(window, pWidth, pHeight);
            final int ww = pWidth.get(0);//window width
//            final int wh = pHeight.get(0);//window height
            GLFWVidMode vm = glfwGetVideoMode(glfwGetPrimaryMonitor());
            assert null!=vm;
            final int sw = vm.width();//screen width
            final int sh = vm.height();//screen height
            glfwSetWindowPos(window,lwx,lwy);
            lwx+=ww;
            if(lwx>=sw){
                lwx = X_INIT;
                lwy+= 100;
                if(lwy>=sh)lwy = Y_INIT;
            }
        } // the stack frame is popped automatically
    }
    public boolean post(Runnable runner){
        if(Objects.isNull(runner))return false;
        return tasks.offer(runner);
    }
    public void onTick(){
//        windows.values().forEach(WindowImpl::onTick);//ConcurrentModificationException
        for (WindowImpl value : windows.values()) {
            value.onTick();
        }
        Runnable runner;
        while(Objects.nonNull(runner = tasks.poll()))runner.run();
    }
    public Window newWindow(String name){
        window = new WindowImpl(name);
        createWindow(name,window);
        return window;
    }

    public void shutdown() {
        if(exit.get())return;
        new ArrayList<>(windows.values()).forEach(Window::close);
        glfwTerminate();
        Optional.ofNullable(glfwSetErrorCallback(null)).ifPresent(Callback::free);
        exit.set(true);
        while(!exit.get()){
            onTick();
            Thread.yield();
        }
    }
    public Collection<Long> windowsList() {
        return windows.keySet();
    }
    public void delWindow(long id) {
        Optional.ofNullable(windows.get(id)).ifPresent(Window::close);
    }
    final GLFWWindowPosCallback windowPosCallback = new GLFWWindowPosCallback() {
        int x,y;
        final TimerWorker worker = new TimerWorker(()-> System.out.printf("onWindowPos %d,%d\n",x,y));
        @Override
        public void invoke(long window, int xpos, int ypos) {
            x = xpos;
            y = ypos;
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(cb->cb.onWindowPos(xpos,ypos));
        }
    };
    final GLFWWindowSizeCallback windowSizeCallback = new GLFWWindowSizeCallback() {
        int width,height;
        final TimerWorker worker = new TimerWorker(()->System.out.printf("onWindowSize %d,%d\n",width,height));
        @Override
        public void invoke(long window, int width, int height) {
            this.width = width;
            this.height = height;
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(cb->cb.onWindowSize(width,height));
        }
    };
    final GLFWWindowCloseCallback windowCloseCallback = new GLFWWindowCloseCallback() {
        final TimerWorker worker = new TimerWorker(()->System.out.println("onWindowClose"));
        @Override
        public void invoke(long window) {
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(WindowCallback::onWindowClose);
        }
    };

    final GLFWWindowRefreshCallback windowRefreshCallback = new GLFWWindowRefreshCallback() {
        final TimerWorker worker = new TimerWorker(()->System.out.println("onWindowRefresh"));
        @Override
        public void invoke(long window) {
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(WindowCallback::onWindowRefresh);
        }
    };
    final GLFWWindowFocusCallback windowFocusCallback = new GLFWWindowFocusCallback() {
        boolean focused;
        final TimerWorker worker = new TimerWorker(()->System.out.println("onWindowFocus:"+focused));
        @Override
        public void invoke(long window, boolean focused) {
            this.focused = focused;
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(e->e.onWindowFocus(focused));
        }
    };
    final GLFWWindowIconifyCallback windowIconifyCallback = new GLFWWindowIconifyCallback() {
        boolean iconified;
        final TimerWorker worker = new TimerWorker(()->System.out.println("onWindowIconify:"+iconified));
        @Override
        public void invoke(long window, boolean iconified) {
            this.iconified = iconified;
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(e->e.onWindowIconify(iconified));
        }
    };
    final GLFWWindowMaximizeCallback windowMaximizeCallback = new GLFWWindowMaximizeCallback() {
        boolean maximized;
        final TimerWorker worker = new TimerWorker(()->System.out.println("onWindowMaximize:"+maximized));
        @Override
        public void invoke(long window, boolean maximized) {
            this.maximized = maximized;
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(e->e.onWindowMaximize(maximized));
        }
    };
    final GLFWFramebufferSizeCallback framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
        int width,height;
        final TimerWorker worker = new TimerWorker(()->System.out.printf("onFrameBufferSize %d,%d\n",width,height));
        @Override
        public void invoke(long window, int width, int height) {
            this.width = width;
            this.height = height;
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(cb->cb.onFrameBufferSize(width,height));
        }
    };
    final GLFWWindowContentScaleCallback windowContentScaleCallback = new GLFWWindowContentScaleCallback() {
        float xscale,yscale;
        final TimerWorker worker = new TimerWorker(()->System.out.printf("onWindowContentScale %f,%f\n",xscale,yscale));
        @Override
        public void invoke(long window, float xscale, float yscale) {
            this.xscale = xscale;
            this.yscale = yscale;
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(e->e.onWindowContentScale(xscale,yscale));
        }
    };
    final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        int key,scancode,action,mods;
        final TimerWorker worker = new TimerWorker(()->System.out.printf("onKey key:%d,code:%d,action:%d,mods:%d\n"
                ,key,scancode,action,mods));
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            this.key = key;
            this.scancode = scancode;
            this.action = action;
            this.mods = mods;
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(cb->cb.onKey(key,scancode,action,mods));
        }
    };
    final GLFWCharCallback charCallback = new GLFWCharCallback() {
        int codepoint;
        final TimerWorker worker = new TimerWorker(()->System.out.printf("onChar:0x%x %c\n",codepoint,(char)codepoint));
        @Override
        public void invoke(long window, int codepoint) {
            this.codepoint = codepoint;
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(cb->cb.onChar(codepoint));
        }
    };
    final GLFWCharModsCallback charModsCallback = new GLFWCharModsCallback() {
        int codepoint,mods;
        final TimerWorker worker = new TimerWorker(()->System.out.printf("onCharMods:0x%x %c,mod:%d\n",codepoint,(char)codepoint,mods));
        @Override
        public void invoke(long window, int codepoint, int mods) {
            this.codepoint = codepoint;
            this.mods = mods;
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(cb->cb.onCharMods(codepoint,mods));
        }
    };
    final GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
        int button,action,mods;
        final TimerWorker worker = new TimerWorker(()->System.out.printf("onMouseButton button:%d,action:%d,mods:%d\n"
                ,button,action,mods));
        @Override
        public void invoke(long window, int button, int action, int mods) {
            this.button = button;
            this.action = action;
            this.mods = mods;
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(cb->cb.onMouseButton(button,action,mods));
        }
    };

    final GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
        double xpos,ypos;
        final TimerWorker worker = new TimerWorker(()->System.out.printf("onCursorPos %f,%f\n",xpos,ypos));
        @Override
        public void invoke(long window, double xpos, double ypos) {
            this.xpos = xpos;
            this.ypos = ypos;
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(cb->cb.onCursorPos(xpos,ypos));
        }
    };
    final GLFWCursorEnterCallback cursorEnterCallback = new GLFWCursorEnterCallback() {
        boolean entered;
        final TimerWorker worker = new TimerWorker(()->System.out.println("onCursorEnter:"+entered));
        @Override
        public void invoke(long window, boolean entered) {
            this.entered = entered;
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(cb->cb.onCursorEnter(entered));
        }
    };

    final GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
        double xoffset,yoffset;
        final TimerWorker worker = new TimerWorker(()->System.out.printf("onCursorPos %f,%f\n",xoffset,yoffset));
        @Override
        public void invoke(long window, double xoffset, double yoffset) {
            this.xoffset = xoffset;
            this.yoffset = yoffset;
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(cb->cb.onScroll(xoffset,yoffset));
        }
    };

    final GLFWDropCallback dropCallback = new GLFWDropCallback() {
        final ArrayList<String> list = new ArrayList<>();
        final TimerWorker worker = new TimerWorker(()->System.out.printf("onDrop files:%s\n",list));

        @Override
        public void invoke(long window, int count, long names) {
//            PointerBuffer charPointers = MemoryUtil.memPointerBuffer(names, count);
//            for (int i = 0; i < count; i++) {
//                String name = MemoryUtil.memUTF8(charPointers.get(i));
//                System.err.println(name); // <- test: print out the path
//            }
            list.clear();
            for ( int i = 0; i < count; i++ ) {
                list.add(getName(names, i));
            }
            worker.onTick();
            Optional.ofNullable(callbacks.get(window)).ifPresent(cb->{cb.onDrop(count,names);cb.onDrop(list);});
        }
    };

    /*************************/
    static final GLFWJoystickCallback joystickCallback = new GLFWJoystickCallback() {
        int jid,event;
        final TimerWorker worker = new TimerWorker(()->System.out.printf("onMonitor jid:%d,event:%d\n",jid,event));
        @Override
        public void invoke(int jid, int event) {
            this.jid = jid;
            this.event = event;
            worker.onTick();
            getInstance().callbacks.values().forEach(c->c.onJoystick(jid,event));

        }
    };
    static final GLFWErrorCallbackI errorCallback = new GLFWErrorCallback() {
        @Override
        public void invoke(int error, long description) {
            System.out.printf("GLMainRender.GLFWErrorCallback,err:%d,desc:%s\n",error,description);
        }
    };
    static GLFWMonitorCallbackI monitorCallback = new GLFWMonitorCallback() {
        long monitor;
        int event;
        final TimerWorker worker = new TimerWorker(()->System.out.printf("onMonitor mid:%s,event:%d\n",monitor,event));
        @Override
        public void invoke(long monitor, int event) {
            this.monitor = monitor;
            this.event = event;
            worker.onTick();
            getInstance().callbacks.values().forEach(c->c.onMonitor(monitor,event));
        }
    };
    public static GLMultiWindowManager getInstance(){
        return Holder.i;
    }
    static class Holder{
        static GLMultiWindowManager i = new GLMultiWindowManager();
    }
}
