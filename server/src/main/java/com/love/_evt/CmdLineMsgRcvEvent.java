package com.love._evt;

import com.love._api.CommandLine;
import com.love._core.Event;

public class CmdLineMsgRcvEvent implements Event {
    public final CommandLine.Command cmd;

    public CmdLineMsgRcvEvent(CommandLine.Command cmd) {
        this.cmd = cmd;
    }

    @Override
    public Type type() {
        return Type.CMD_LINE_MSG;
    }

}
