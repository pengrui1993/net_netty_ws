package com.love.core;

import com.love._anno.Read;
import com.love._anno.Write;

import java.util.List;

public interface Context{
    @Write
    void stop();
    @Write
    void newWindow(String name);
    @Read
    List<Long> listWindow();
    @Write
    void closeWindow(long id);
    @Write
    boolean post(Runnable post);

    void run(Runnable runner);

    void requestWindowDraw();
    void requestWindowSwap();
}