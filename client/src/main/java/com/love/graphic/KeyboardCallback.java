package com.love.graphic;

public interface KeyboardCallback {
    default void onChar(int codepoint){
    };
    default void onCharMods(int codepoint,int mods){}
    default void onKey(int key, int scancode, int action, int mods){}
}
