package com.love.util;

import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class TimerWorker {
    double last;
    double delta;
    Runnable runner;
    final boolean continues;
    static final Runnable NULL = ()->{};
    static double now(){
        return GLFW.glfwGetTime();
    }
    public TimerWorker(Runnable runner){
        this(runner,0.5,false);
    }
    public TimerWorker(Runnable runner,double delta){
        this(runner,delta,false);
    }
    public TimerWorker(Runnable runner,double delta,boolean continues){
        this.runner= Optional.ofNullable(runner).orElse(NULL);
        this.delta = delta<0?0.5:delta;
        last = now();
        this.continues = continues;
    }
    public void onTick(){
        double tmpNow = now();
        if(tmpNow-last<delta)return;
        if(continues)last+=delta;
        if(!continues)last = tmpNow;
        runner.run();
    }
}
