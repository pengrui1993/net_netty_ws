package com.love.graphic;

public interface WindowCallback {
    default void onWindowClose(){
    }
    default void onWindowContentScale(float x,float y){
    }// float xscale, float yscale
    default void onWindowFocus(boolean focused){
    }
    default void onWindowIconify(boolean iconified){
    }
    default void onWindowMaximize(boolean maximized){
    }
    default void onWindowPos(int x,int y){
    }
    default void onWindowRefresh(){
    }
    default void onWindowSize(int width, int height){
    }
}
