package com.kinnarastudio.odooxmlrpc.model.command;

public class CommandUnlink implements Command {
    private final int id;

    public CommandUnlink(int id) {
        this.id = id;
    }

    @Override
    public Object[] getCommand() {
        return new Object[] {3, id};
    }
}
