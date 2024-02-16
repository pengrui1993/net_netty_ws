package com.love.cmd;

import com.love.core.Command;

public interface ConsoleCommand extends Command {
    @Override
    default Type type(){return Type.CONSOLE;}
    enum Type1{
        QUIT,WIN_NEW,WIN_DEL,WIN_LIST,WIN_INFO,WIN_DRAW,WIN_SWAP
    }
    Type1 type1();
}
