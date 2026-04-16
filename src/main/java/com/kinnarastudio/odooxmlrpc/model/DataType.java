package com.kinnarastudio.odooxmlrpc.model;

public enum DataType {
    STRING,
    INTEGER;

    public static DataType parse(String string) {
        switch (string.toLowerCase()) {
            case "integer":
            case "many2one":
                return DataType.INTEGER;
            default:
                return DataType.STRING;
        }
    }
}
