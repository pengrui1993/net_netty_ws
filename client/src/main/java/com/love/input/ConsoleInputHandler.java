package com.love.input;

import com.love.cmd.ConsoleCommand;
import com.love.cmd.WindowDel;
import com.love.cmd.WindowList;
import com.love.cmd.WindowNew;
import com.love.core.Context;
import com.love.core.SystemQueue;

public class ConsoleInputHandler {
    public void handle(ConsoleCommand cmd, Context ctx, SystemQueue lines){
        switch(cmd.type1()){
            case QUIT -> {ctx.stop();}
            case WIN_LIST -> {
                System.out.println(ctx.listWindow());
            }
            case WIN_NEW -> {
                WindowNew c = WindowNew.class.cast(cmd);
                ctx.newWindow(c.name);
                lines.offer(WindowList.INSTANCE);
            }
            case WIN_DEL -> {
                WindowDel c = WindowDel.class.cast(cmd);
                ctx.closeWindow(c.id);
                lines.offer(WindowList.INSTANCE);
            }
            case WIN_DRAW -> ctx.requestWindowDraw();
            case WIN_SWAP -> ctx.requestWindowSwap();
            case WIN_INFO ->{
                System.out.println("CommandLine.input");
            }
        }
    }
}
