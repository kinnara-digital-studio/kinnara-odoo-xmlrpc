package com.kinnarastudio.odooxmlrpc.exception;

public class OdooCallMethodException extends Exception {
    public OdooCallMethodException(String message) {
        super(message);
    }
    public OdooCallMethodException(Throwable cause) {
        super(cause);
    }
}
