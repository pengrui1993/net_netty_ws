package com.love.cmd;

public class WindowList implements ConsoleCommand {
    public static final WindowList INSTANCE = new WindowList();
    @Override
    public Type1 type1() {
        return Type1.WIN_LIST;
    }
}
