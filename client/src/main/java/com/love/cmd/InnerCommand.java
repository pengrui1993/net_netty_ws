package com.love.cmd;

import com.love.core.Command;

public interface InnerCommand extends Command {
    @Override
    default Type type(){return Type.INNER;}
    Type1 type1();
    enum Type1{
        RUN
    }

}
