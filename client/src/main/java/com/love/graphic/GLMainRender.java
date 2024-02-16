package com.love.graphic;

import com.love.core.Window;
import com.love.util.TimerWorker;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GLMainRender {
    static Thread main;
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private GLMainRender(){
    }
    static{
        init();
    }
    public static void init(){
        main = Thread.currentThread();
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        Optional.ofNullable(glfwSetMonitorCallback(monitorCallback)).ifPresent(NativeResource::close);
        Optional.ofNullable(glfwSetJoystickCallback(joystickCallback)).ifPresent(NativeResource::close);
    }
    private void resourceClose(NativeResource cb){
        if(Objects.isNull(cb))return;
        logger.info("close callback:"+cb);
        cb.close();
    }
    void setIcon(int window, GLFWImage.Buffer buf){
        GLFWImage malloc = GLFWImage.malloc();
        //https://vimsky.com/examples/detail/java-class-org.lwjgl.glfw.GLFWImage.html
        malloc.close();
    }
    void register(WindowImpl w) {
        long hw = w.window;
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
    }

    final ConcurrentLinkedDeque<Runnable> tasks = new ConcurrentLinkedDeque<>();
    // The window handle
    WindowImpl window;
    Callbacks cbs;
    private final AtomicBoolean exit = new AtomicBoolean(false);
    void closeWindow(long window){
        if(window!=this.window.window)return;
        final Runnable run = ()->{
            glfwSetWindowShouldClose(window, true);
            // Free the window callbacks and destroy the window
            glfwDestroyWindow(window);
            glfwFreeCallbacks(window);
            this.window = null;
            this.cbs = null;
        };
        if(Thread.currentThread()==main){
            run.run();
        }else{
            post(run);
        }

    }
    final boolean debug = false;
    private void createWindow(String name, WindowImpl win,int width,int height){
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        if(debug) glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        long window = win.window = glfwCreateWindow(width, height, name, NULL, NULL);
        if ( window == NULL ) throw new RuntimeException("Failed to create the GLFW window");
//        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        register(win);
        center(window);
//        setIcon(window,);
        monitor();
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // Enable v-sync
        glfwShowWindow(window);// Make the window visible
//        glfwFocusWindow(window);
        GL.createCapabilities();
        // Set the clear color

        win.setting();
        this.cbs = win;
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
    private void center(long window){
        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically
    }
    public boolean post(Runnable runner){
        if(Objects.isNull(runner))return false;
        return tasks.offer(runner);
    }
    public void onTick(){
        Optional.ofNullable(window).ifPresent(WindowImpl::onTick);
        Runnable runner;
        while(Objects.nonNull(runner = tasks.poll()))runner.run();
    }
    public Window newWindow(String name){
        if(Objects.nonNull(window))return window;
        createWindow(name,window=new WindowImpl(name),800,600);
        return window;
    }
    public void shutdown() {
        if(exit.get())return;
        if(Objects.nonNull(window))closeWindow(window.window);
        glfwTerminate();
        Optional.ofNullable(glfwSetErrorCallback(null)).ifPresent(Callback::free);
        exit.set(true);
        while(!exit.get()){
            onTick();
            Thread.yield();
        }
    }
    public Collection<Long> windowsList() {
        return Objects.nonNull(window)?List.of(window.window):Collections.emptyList();
    }
    public void delWindow(long id) {
        if(Objects.isNull(window))return;
        if(!Objects.equals(id,window.window))return;
        window.close();
    }
    final GLFWWindowPosCallback windowPosCallback = new GLFWWindowPosCallback() {
        int x,y;
        final TimerWorker worker = new TimerWorker(()-> System.out.printf("onWindowPos %d,%d\n",x,y));
        @Override
        public void invoke(long window, int xpos, int ypos) {
            x = xpos;
            y = ypos;
            worker.onTick();
            Optional.ofNullable(cbs).ifPresent(cb->cb.onWindowPos(xpos,ypos));
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
            Optional.ofNullable(cbs).ifPresent(cb->cb.onWindowSize(width,height));
        }
    };
    final GLFWWindowCloseCallback windowCloseCallback = new GLFWWindowCloseCallback() {
        final TimerWorker worker = new TimerWorker(()->logger.info("onWindowClose"));
        @Override
        public void invoke(long window) {
            worker.onTick();
            Optional.ofNullable(cbs).ifPresent(WindowCallback::onWindowClose);
        }
    };

    final GLFWWindowRefreshCallback windowRefreshCallback = new GLFWWindowRefreshCallback() {
        final TimerWorker worker = new TimerWorker(()->logger.info("onWindowRefresh"));
        @Override
        public void invoke(long window) {
            worker.onTick();
            Optional.ofNullable(cbs).ifPresent(WindowCallback::onWindowRefresh);
        }
    };
    final GLFWWindowFocusCallback windowFocusCallback = new GLFWWindowFocusCallback() {
        boolean focused;
        final TimerWorker worker = new TimerWorker(()->logger.info("onWindowFocus:"+focused));
        @Override
        public void invoke(long window, boolean focused) {
            this.focused = focused;
            worker.onTick();
            Optional.ofNullable(cbs).ifPresent(e->e.onWindowFocus(focused));
        }
    };
    final GLFWWindowIconifyCallback windowIconifyCallback = new GLFWWindowIconifyCallback() {
        boolean iconified;
        final TimerWorker worker = new TimerWorker(()->logger.info("onWindowIconify:"+iconified));
        @Override
        public void invoke(long window, boolean iconified) {
            this.iconified = iconified;
            worker.onTick();
            Optional.ofNullable(cbs).ifPresent(e->e.onWindowIconify(iconified));
        }
    };
    final GLFWWindowMaximizeCallback windowMaximizeCallback = new GLFWWindowMaximizeCallback() {
        boolean maximized;
        final TimerWorker worker = new TimerWorker(()->logger.info("onWindowMaximize:"+maximized));
        @Override
        public void invoke(long window, boolean maximized) {
            this.maximized = maximized;
            worker.onTick();
            Optional.ofNullable(cbs).ifPresent(e->e.onWindowMaximize(maximized));
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
            Optional.ofNullable(cbs).ifPresent(cb->cb.onFrameBufferSize(width,height));
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
            Optional.ofNullable(cbs).ifPresent(e->e.onWindowContentScale(xscale,yscale));
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
            Optional.ofNullable(cbs).ifPresent(cb->cb.onKey(key,scancode,action,mods));
        }
    };
    final GLFWCharCallback charCallback = new GLFWCharCallback() {
        int codepoint;
        final TimerWorker worker = new TimerWorker(()->System.out.printf("onChar:0x%x %c\n",codepoint,(char)codepoint));
        @Override
        public void invoke(long window, int codepoint) {
            this.codepoint = codepoint;
            worker.onTick();
            Optional.ofNullable(cbs).ifPresent(cb->cb.onChar(codepoint));
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
            Optional.ofNullable(cbs).ifPresent(cb->cb.onCharMods(codepoint,mods));
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
            Optional.ofNullable(cbs).ifPresent(cb->cb.onMouseButton(button,action,mods));
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
            Optional.ofNullable(cbs).ifPresent(cb->cb.onCursorPos(xpos,ypos));
        }
    };
    final GLFWCursorEnterCallback cursorEnterCallback = new GLFWCursorEnterCallback() {
        boolean entered;
        final TimerWorker worker = new TimerWorker(()->logger.info("onCursorEnter:"+entered));
        @Override
        public void invoke(long window, boolean entered) {
            this.entered = entered;
            worker.onTick();
            Optional.ofNullable(cbs).ifPresent(cb->cb.onCursorEnter(entered));
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
            Optional.ofNullable(cbs).ifPresent(cb->cb.onScroll(xoffset,yoffset));
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
            Optional.ofNullable(cbs).ifPresent(cb->{cb.onDrop(count,names);cb.onDrop(list);});
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
            Optional.ofNullable(getInstance().cbs).ifPresent(c->c.onJoystick(jid,event));

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
            Optional.ofNullable(getInstance().cbs).ifPresent(c->c.onMonitor(monitor,event));
        }
    };
    public static GLMainRender getInstance(){
        return Holder.i;
    }
    public void requestWindowDraw() {
        if(Objects.isNull(window))return;
        logger.info("GLMainRender.requestWindowDraw");
        window.requestedDraw = true;
    }
    public void requestWindowSwap() {
        if(Objects.isNull(window))return;
        logger.info("GLMainRender.requestWindowSwap");
        window.requestedSwap = true;
    }

    static class Holder{
        static GLMainRender i = new GLMainRender();
    }
}
