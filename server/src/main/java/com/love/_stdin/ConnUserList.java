package com.love._stdin;

import com.love._api.CommandLine;

public class ConnUserList implements CommandLine.Command{
    public static final ConnUserList INSTANCE = new ConnUserList();
    @Override
    public CommandLine.Type type() {
        return CommandLine.Type.LIST_CONN_USER;
    }
    private ConnUserList(){}
}