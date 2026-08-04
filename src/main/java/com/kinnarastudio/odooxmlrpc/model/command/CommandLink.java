package com.kinnarastudio.odooxmlrpc.model.command;

public class CommandLink implements Command {
    private final int id;

    public CommandLink(int id) {
        this.id = id;
    }

    @Override
    public Object[] getCommand() {
        return new Object[] {4, id};
    }
}
