package com.love.util;

import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.*;

public class TimerGetter {
    public long frequency(){
        return GLFW.glfwGetTimerFrequency();
    }
    public long timerValue(){
        return GLFW.glfwGetTimerValue();
    }
    public double time(){
        return GLFW.glfwGetTime();
    }

    void test(){
        System.out.println("WindowImpl.onTick:"+glfwGetTime());//start 0
        System.out.println("WindowImpl.onTick:"+glfwGetTimerValue());//like timestamp
        System.out.println("WindowImpl.onTick:"+glfwGetTimerFrequency());//10000000
    }
}
