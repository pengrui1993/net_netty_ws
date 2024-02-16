package com.love.graphic;

import com.love.core.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;

class WindowImpl implements Window,Callbacks{
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    final String initName;
    long window;
    final GLMainRender render = GLMainRender.getInstance();
    WindowImpl(String initName){
        this.initName = initName;
    }

    boolean swap = false;
    public void onTick() {
        if(glfwWindowShouldClose(window))return;
        if(window!=glfwGetCurrentContext())glfwMakeContextCurrent(window);
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        render();
        if(swap)glfwSwapBuffers(window); // swap the color buffers
        // Poll for window events. The key callback above will only be
        // invoked during this call.
        glfwPollEvents();
    }
    @Override
    public void close() {
        render.closeWindow(window);
        if(render.window == this){
            //
        }
    }

    @Override
    public void onWindowClose() {
        close();
    }

    @Override
    public void onKey(int key, int scancode, int action, int mods) {
        if(GLFW_KEY_ESCAPE==key && GLFW_RELEASE==action){
        }
    }
    @Override
    public void setting() {
        glClearColor(0.8f, 0f, 0.4f, 0.0f);
    }

    @Override
    public void render() {
//        triangleRotate();
        testRender();
    }
    boolean requestedSwap = false;
    boolean requestedDraw = false;
                                                                                                                                                                                        void testRender(){
        if(requestedDraw){
            renderTriangle();
            logger.info("draw done");
            requestedDraw = false;
        }
        if(requestedSwap){
            glfwSwapBuffers(window);
            logger.info("swap done");
            requestedSwap = false;
        }
    }
    void triangleRotate(){
        rotateTriangle();
        glfwSwapBuffers(window);
    }
}