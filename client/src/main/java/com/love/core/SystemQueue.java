package com.love.core;

public interface SystemQueue {
    boolean offer(Command cmd);
    Command poll();
    boolean isEmpty();
}
