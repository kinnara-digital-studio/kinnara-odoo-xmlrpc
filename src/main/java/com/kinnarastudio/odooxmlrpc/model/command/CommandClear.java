package com.kinnarastudio.odooxmlrpc.model.command;

public final class CommandClear implements Command {
    @Override
    public Object[] getCommand() {
        return new Object[] {5, 0, 0};
    }
}
