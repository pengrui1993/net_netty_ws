package com.love.cmd;

public class WindowNew implements ConsoleCommand {
    public final String name;
    public WindowNew(String name) {
        this.name = name;
    }
    @Override
    public Type1 type1() {
        return Type1.WIN_NEW;
    }
}
