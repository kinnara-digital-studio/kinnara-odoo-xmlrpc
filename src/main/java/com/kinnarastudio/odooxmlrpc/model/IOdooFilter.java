package com.kinnarastudio.odooxmlrpc.model;

public interface IOdooFilter {
    String getField();

    String getOperator();

    String getValue();

    DataType getDataType();


}
