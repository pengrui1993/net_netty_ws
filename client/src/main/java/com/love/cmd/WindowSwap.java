package com.love.cmd;

public class WindowSwap implements ConsoleCommand {
    public static final WindowSwap INSTANCE = new WindowSwap();
    private WindowSwap(){}
    @Override
    public Type1 type1() {
        return Type1.WIN_SWAP;
    }
}
