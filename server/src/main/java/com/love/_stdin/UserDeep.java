package com.love._stdin;

import com.love._api.CommandLine;

import java.io.PrintStream;

public class UserDeep implements CommandLine.Command{
    public static final UserDeep INSTANCE = new UserDeep();
    public PrintStream out = System.out;
    private int id;
    @Override
    public CommandLine.Type type() {
        return CommandLine.Type.USER_DEEP;
    }
    private UserDeep(){}
    public int getId(){
        return id;
    }
    public UserDeep reuse(int id){
        this.id = id;
        return this;
    }
}