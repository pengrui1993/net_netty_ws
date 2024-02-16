package com.love.util;

import org.lwjgl.opengl.GL11;

public class OpenglUtil {
    public static void detect(){
        int err;
        if(GL11.GL_NO_ERROR!=(err=GL11.glGetError()))throw new RuntimeException("err code:"+err);
    }
    public static void clear(){
        while(GL11.GL_NO_ERROR!=GL11.glGetError()){
            final StackTraceElement se = Thread.currentThread().getStackTrace()[2];
            System.out.println("clear err:"+se);
        }
    }
}
