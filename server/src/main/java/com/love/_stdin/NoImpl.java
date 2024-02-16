package com.love._stdin;

import com.love._api.CommandLine;

public class NoImpl implements CommandLine.Command{
    public final String unknown;
    public NoImpl(String cmd) {
        this.unknown = cmd;
    }

    @Override
    public CommandLine.Type type() {
        return CommandLine.Type.NO_IMPL;
    }
}
