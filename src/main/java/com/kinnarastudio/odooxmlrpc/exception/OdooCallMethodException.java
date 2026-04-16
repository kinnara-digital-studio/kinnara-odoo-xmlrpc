package com.kinnarastudio.odooxmlrpc.exception;

/**
 * Exception when calling any xmlrpc method
 */
public class OdooCallMethodException extends Exception {
    public OdooCallMethodException(String message) {
        super(message);
    }
    public OdooCallMethodException(Throwable cause) {
        super(cause);
    }
}
