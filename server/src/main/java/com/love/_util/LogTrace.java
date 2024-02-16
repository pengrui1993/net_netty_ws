package com.love._util;

import org.slf4j.MDC;

import java.util.Objects;

/**
 * mapped diagnostic context
 */
public class LogTrace {
    static final String k = "tk_key";

    static boolean isBlank(String str){
        if(Objects.isNull(str))
            return true;
        return str.trim().isBlank();
    }
    public static void enable(String value){
        enable(value,false);
    }
    public static void enable(String value,boolean force){
        String ex;
        if(isBlank(ex=MDC.get(k))){
            if(!force)System.out.printf("log trace config:%s,thread:%s\n",value,Thread.currentThread().getId());
            MDC.put(k,value);
        }else{
            if(!force)System.out.printf("warning mdc exists,pre:%s,now:%s,thread:%s\n",ex,value,Thread.currentThread().getId());
        }
    }
    public static void disable(){
        MDC.remove(k);
    }
}
