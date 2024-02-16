package com.love._stdin;

import com.love._api.CommandLine;

public class UidUserList implements CommandLine.Command{
    public static final UidUserList INSTANCE = new UidUserList();
    @Override
    public CommandLine.Type type() {
        return CommandLine.Type.LIST_UID_USER;
    }
    private UidUserList(){}
}