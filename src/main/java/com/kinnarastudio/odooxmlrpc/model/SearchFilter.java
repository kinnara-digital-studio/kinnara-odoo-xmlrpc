package com.kinnarastudio.odooxmlrpc.model;

public class SearchFilter {
    private final Join join;
    private final String field;
    private final Operator operator;
    private final Object value;

    public SearchFilter(String field, Operator operator, Object value) {
        this(Join.AND, field, operator, value);
    }

    public SearchFilter(String field, Object value) {
        this(Join.AND, field, Operator.EQUAL, value);
    }

    public SearchFilter(Join join, String field, Object value) {
        this(join, field, Operator.EQUAL, value);
    }

    public SearchFilter(String field, Object... values) {
        this(Join.AND, field, Operator.IN, values);
    }

    public SearchFilter(Join join, String field, Operator operator, Object value) {
        this.join = join;
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public Operator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

    public static SearchFilter[] single(String field, Object value) {
        return new SearchFilter[]{new SearchFilter(field, value)};
    }

    public Join getJoin() {
        return join;
    }

//    public final static String EQUAL = "=";
//    public final static String NOT_EQUAL = "<>";
//    public final static String GREATER = ">";
//    public final static String GREATER_EQUAL = ">=";
//    public final static String LESS = "<";
//    public final static String LESS_EQUAL = "<=";
//    public final static String IN = "in";

    public enum Operator {
        EQUAL("="),
        NOT_EQUAL("<>"),
        GREATER(">"),
        GREATER_EQUAL(">="),
        LESS("<"),
        LESS_EQUAL("<="),
        IN("in");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    public enum Join {
        AND("&"),
        OR("|");

        private final String symbol;

        Join(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }
}
