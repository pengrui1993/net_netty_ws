package com.love._core;

public interface Listener {
    default int order(){return Integer.MAX_VALUE/2;}
    void onEvent(Event evt);
}
