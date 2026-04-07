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

    public Join getJoin() {
        return join;
    }

    /**
     * Operator enumeration
     */
    public enum Operator {
        EQUAL("="),
        NOT_EQUAL("<>"),
        GREATER(">"),
        GREATER_EQUAL(">="),
        LESS("<"),
        LESS_EQUAL("<="),
        IN("in"),
        NOT_IN("not in"),
        LIKE("like"),
        NOT_LIKE("not like"),
        ILIKE("ilike"),
        NOT_ILIKE("not ilike");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    /**
     * Join enumeration
     */
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

    /**
     * Operand class
     */
    public final static class Operand {
        private final String field;
        private final SearchFilter.Operator operator;
        private final Object value;

        public Operand(SearchFilter filter) {
            this(filter.getField(), filter.getOperator(), filter.getValue());
        }

        public Operand(String field, SearchFilter.Operator operator, Object value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }

        public Object[] toObjects() {
            return new Object[]{
                    field,
                    operator.toString(),
                    value
            };
        }

        @Override
        public String toString() {
            return "[" + field + "," + operator + "," + value + "]";
        }
    }
}
