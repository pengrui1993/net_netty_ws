package com.love.cmd;

public class SystemQuit implements ConsoleCommand {
    public static final SystemQuit INSTANCE = new SystemQuit();
    @Override
    public Type1 type1() {
        return Type1.QUIT;
    }
}
