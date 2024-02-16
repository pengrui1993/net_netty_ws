package com.love.cmd;

public class WindowInfo implements ConsoleCommand {
    public final long id;
    public WindowInfo(long wid) {
        this.id = wid;
    }
    @Override
    public Type1 type1() {
        return Type1.WIN_INFO;
    }
}
