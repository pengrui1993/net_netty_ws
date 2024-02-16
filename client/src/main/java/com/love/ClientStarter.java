package com.love;

import com.love.cmd.WindowNew;
import com.love.core.Context;
import com.love.graphic.GLMainRender;
import com.love.input.CommandLineReader;
import com.love.input.UniqueInputHandler;
import com.love.input.UniqueMessageQueue;

import java.util.concurrent.atomic.AtomicBoolean;

public class ClientStarter {
    static Context ctx;
    public static void main(String[] args) {
        final UniqueMessageQueue queue = new UniqueMessageQueue();
        final GLMainRender render = GLMainRender.getInstance();
        final CommandLineReader line = new CommandLineReader(queue);
        final UniqueInputHandler handler = new UniqueInputHandler();
        final AtomicBoolean running = new AtomicBoolean(true);
        ctx = new Client(render,line,queue,running,handler);
        queue.offer(new WindowNew("hello"));
        while(running.get()){
            try{
                render.onTick();
                handler.input(ctx,queue);
            }catch (Throwable e){
                e.printStackTrace(System.err);
            }
        }
        line.close();
        render.shutdown();
    }
}
