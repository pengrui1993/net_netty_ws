package com.love.cmd;

import com.love.core.Command;

public interface NetCommand extends Command {
    @Override
    default Type type(){return Type.NET;}

    enum Type1{
        READ,WRITE
    }
    enum Type2{
        GLOBAL,SCOPED
    }
}
