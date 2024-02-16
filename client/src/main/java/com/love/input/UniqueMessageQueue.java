package com.love.input;

import com.love.core.Command;
import com.love.core.SystemQueue;

import java.util.concurrent.ConcurrentLinkedDeque;

public class UniqueMessageQueue implements SystemQueue {
    ConcurrentLinkedDeque<Command> lines = new ConcurrentLinkedDeque<>();
    public boolean offer(Command cmd){
        return lines.offer(cmd);
    }
    public Command poll(){
        return lines.poll();
    }
    @Override
    public boolean isEmpty() {
        return lines.isEmpty();
    }
}
