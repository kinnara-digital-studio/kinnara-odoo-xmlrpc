package com.kinnarastudio.odooxmlrpc.model.command;

public final class CommandSet implements Command {
    private final int[] ids;

    public CommandSet(int[] ids) {
        this.ids = ids;
    }

    @Override
    public Object[] getCommand() {
        return new Object[]{6, 0, ids};
    }
}
