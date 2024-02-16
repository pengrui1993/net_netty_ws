package com.love;

import com.love.cmd.PostRunner;
import com.love.core.Context;
import com.love.graphic.GLMainRender;
import com.love.input.CommandLineReader;
import com.love.input.UniqueInputHandler;
import com.love.input.UniqueMessageQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client implements Context {
    final GLMainRender render;
    final CommandLineReader line;
    final UniqueMessageQueue queue;
    final UniqueInputHandler cmdHandler;
    final AtomicBoolean running;
    public Client(GLMainRender render
            , CommandLineReader line
            , UniqueMessageQueue queue
            , AtomicBoolean running
            , UniqueInputHandler cmdHandler
    ) {
        this.render = render;
        this.line = line;
        this.queue = queue;
        this.running = running;
        this.cmdHandler = cmdHandler;
    }
    @Override
    public void stop() {
        running.set(false);
    }
    @Override
    public void newWindow(String name) {
        render.newWindow(name);
    }
    public List<Long> listWindow(){
        return new ArrayList<>(render.windowsList());
    }
    @Override
    public void closeWindow(long id) {
        render.delWindow(id);
    }

    @Override
    public boolean post(Runnable post) {
        return queue.offer(PostRunner.create(post));
    }
    @Override
    public void run(Runnable runner) {
        if(Objects.isNull(runner))return;
        runner.run();
    }

    @Override
    public void requestWindowDraw() {
        render.requestWindowDraw();
    }

    @Override
    public void requestWindowSwap() {
        render.requestWindowSwap();
    }

}
