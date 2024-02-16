package com.love.graphic;

public interface Callbacks
    extends KeyboardCallback,MouseCallback,WindowCallback
{

    default void onJoystick(int joyId,int event){

    }
    default void onFrameBufferSize(int width,int height){}

    default void onMonitor(long monitor, int event){
        System.out.printf("onMonitor,monitor:%s,event:%d\n",monitor,event);
    }

}
