package com.love.input;

import com.love.cmd.ConsoleCommand;
import com.love.cmd.InnerCommand;
import com.love.core.Command;
import com.love.core.Context;
import com.love.core.SystemQueue;

import java.util.Objects;

public class UniqueInputHandler {
    ConsoleInputHandler consoleInputHandler = new ConsoleInputHandler();
    InnerInputHandler inputHandler = new InnerInputHandler();
    public void input(Context ctx, SystemQueue lines) {
        if(lines.isEmpty())return;
        int times = 5;
        Command cmd;
        while(times-->0&& Objects.nonNull(cmd = lines.poll())){
            switch(cmd.type()){
                case CONSOLE -> consoleInputHandler.handle(ConsoleCommand.class.cast(cmd),ctx,lines);
                case INNER -> inputHandler.handle(InnerCommand.class.cast(cmd),ctx,lines);
                case NET -> throw new RuntimeException();
            }

        }
    }
}
