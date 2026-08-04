package com.kinnarastudio.odooxmlrpc.model.command;

import java.util.Map;

public final class CommandUpdate implements Command {
    private final int id;
    private final Map<String, Object> record;

    public CommandUpdate(int id, Map<String, Object> record) {
        this.id = id;
        this.record = record;
    }

    @Override
    public Object[] getCommand() {
        return new Object[]{1, id, record};
    }
}
