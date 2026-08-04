package com.kinnarastudio.odooxmlrpc.model;

import com.kinnarastudio.commons.Try;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Odoo field data types
 */
public enum DataType {
    STRING(String.class),
    INTEGER(Integer.class, int.class),
    FLOAT(Float.class, float.class, Double.class, double.class),
    BOOLEAN(Boolean.class),
    MANY2ONE(Object[].class),
    MANY2MANY(Integer[].class);

    public final Class<?>[] clazz;

    DataType(Class<?>... clazz) {
        this.clazz = clazz;
    }

    /**
     * Get {@link DataType} from string value
     * @param value string value
     * @return {@link DataType}, default {@link #STRING}
     */
    public static DataType parse(String value) {
        for (DataType dataType : DataType.values()) {
            if (dataType.name().equalsIgnoreCase(value)) {
                return dataType;
            }
        }

        return STRING;
    }

    /**
     * Get {@link DataType} from java class
     * @param clazz java class
     * @return {@link DataType}
     */
    public static DataType parse(Class<?> clazz) {
        for (DataType dataType : DataType.values()) {
            for (Class<?> aClass : dataType.clazz) {
                if(aClass.equals(clazz)) {
                    return dataType;
                }
            }
        }

        throw new IllegalArgumentException("Unknown java class: " + clazz.getName());
    }

    /**
     * Parse odoo typed value into java's
     *
     * @param rawValue
     * @return
     * @param <T>
     * @throws NumberFormatException
     */
    public <T> T valueParser(Object rawValue) throws NumberFormatException {
        switch (this) {
            case STRING:
                return (T) String.valueOf(rawValue);
            case MANY2ONE:
            case INTEGER:
                return (T) Integer.valueOf(rawValue.toString());
            case FLOAT:
                return (T) Float.valueOf(rawValue.toString());
            case BOOLEAN:
                return (T) Boolean.valueOf(rawValue.toString().toLowerCase());
            case MANY2MANY:
                return (T) Optional.of(rawValue)
                        .map(String::valueOf)
                        .filter(Predicate.not(String::isEmpty))
                        .map(s -> s.split(";"))
                        .stream()
                        .flatMap(Arrays::stream)
                        .map(String::trim)
                        .filter(Predicate.not(String::isEmpty))
                        .map(Try.onFunction(Integer::valueOf, (NumberFormatException e) -> null))
                        .filter(Objects::nonNull)
                        .toArray(Integer[]::new);

            default:
                return (T) rawValue.toString();
        }
    }
}
