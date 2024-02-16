package com.love.util;

import org.lwjgl.opengl.GL11;

public class GlStateMgr {
    private GlStateMgr(){
        throw new UnsupportedOperationException();
    }
    static class ClearColor{
        float r = -1,g = -1,b = -1,a = -1;
        boolean remote(float r,float g,float b,float a){
            return (this.r!=r||this.g!=g||this.b!=b||this.a!=a);
        }
    }
    static ClearColor clearColor = new ClearColor();
    public static void clearColor(float r,float g,float b,float a){
        if(clearColor.remote(r,g,b,a)){
            GL11.glClearColor(r,g,b,a);
        }
    }
}
