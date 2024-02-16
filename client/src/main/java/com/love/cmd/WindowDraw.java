package com.love.cmd;

public class WindowDraw implements ConsoleCommand {
    public static final WindowDraw INSTANCE = new WindowDraw();
    private WindowDraw(){}
    @Override
    public Type1 type1() {
        return Type1.WIN_DRAW;
    }
}
