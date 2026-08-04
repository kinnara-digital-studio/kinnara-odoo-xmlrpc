package com.kinnarastudio.odooxmlrpc.model.command;

import java.util.Map;

public final class CommandCreate implements Command {
    private final Map<String, Object> record;

    public CommandCreate(Map<String, Object> record) {
        this.record = record;
    }

    @Override
    public Object[] getCommand() {
        return new Object[]{0, 0, record};
    }
}
