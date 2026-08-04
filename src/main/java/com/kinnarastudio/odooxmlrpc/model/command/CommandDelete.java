package com.kinnarastudio.odooxmlrpc.model.command;

public final class CommandDelete implements Command {
    private final int id;

    public CommandDelete(int id) {
        this.id = id;
    }

    @Override
    public Object[] getCommand() {
        return new Object[] {2, id};
    }
}
