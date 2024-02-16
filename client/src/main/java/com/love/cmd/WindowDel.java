package com.love.cmd;

public class WindowDel implements ConsoleCommand {
    public final long id;
    public WindowDel(long wid) {
        this.id = wid;
    }

    @Override
    public Type1 type1() {
        return Type1.WIN_DEL;
    }
}
