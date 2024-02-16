package com.love.input;

import com.love.cmd.InnerCommand;
import com.love.cmd.PostRunner;
import com.love.core.Context;
import com.love.core.SystemQueue;

public class InnerInputHandler {
    public void handle(InnerCommand cmd, Context ctx, SystemQueue lines){
        switch(cmd.type1()){
            case RUN -> ctx.run(PostRunner.class.cast(cmd).runner);
        }
    }
}
