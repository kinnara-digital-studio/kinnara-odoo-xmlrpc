package com.kinnarastudio.odooxmlrpc.model;

import com.kinnarastudio.odooxmlrpc.service.SearchBuilder;

/**
 * A single filter for search criteria
 */
public class SearchFilter {
    private final Join join;
    private final String field;
    private final Operator operator;
    private final Object value;

    /**
     * Constructs a search filter with default join (AND) and operator (EQUAL)
     * @param field The field name
     * @param value The value to compare
     */
    public SearchFilter(String field, Object value) {
        this(Join.AND, field, Operator.EQUAL, value);
    }

    /**
     * Constructs a search filter with default join (AND)
     * @param field The field name
     * @param operator The operator
     * @param value The value to compare
     */
    public SearchFilter(String field, Operator operator, Object... value) {
        this(Join.AND, field, operator, value);
    }

    /**
     * Constructs a search filter with default operator (EQUAL)
     * @param join The join operator (AND/OR)
     * @param field The field name
     * @param value The value to compare
     */
    public SearchFilter(Join join, String field, Object value) {
        this(join, field, Operator.EQUAL, value);
    }

    /**
     * Constructs a search filter
     * @param join The join operator (AND/OR)
     * @param field The field name
     * @param operator The operator
     * @param value The value to compare
     */
    public SearchFilter(Join join, String field, Operator operator, Object value) {
        this.join = join;
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    /**
     * Gets the field name
     * @return The field name
     */
    public String getField() {
        return field;
    }

    /**
     * Gets the operator
     * @return The operator
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Gets the value
     * @return The value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Gets the join operator
     * @return The join operator
     */
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
        LIKE("=like"),
        NOT_LIKE("not =like"),
        ILIKE("=ilike"),
        NOT_ILIKE("not =ilike");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }

        /**
         * Parses a string to an Operator
         * @param value The string value
         * @return The corresponding Operator
         * @throws IllegalArgumentException if no operator is found
         */
        public static Operator parse(String value) {
            for (Operator op : values()) {
                if (op.symbol.equalsIgnoreCase(value)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("No operator with symbol " + value + " found");
        }
    }

    /**
     * Gets a new SearchBuilder instance
     * @return a new SearchBuilder instance
     */
    public static SearchBuilder getBuilder() {
        return new SearchBuilder();
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

        /**
         * Parses a string to a Join operator
         * @param value The string value
         * @return The corresponding Join operator
         * @throws IllegalArgumentException if no join operator is found
         */
        public static Join parse(String value) {
            for (Join join : values()) {
                if (join.symbol.equalsIgnoreCase(value)) {
                    return join;
                }
            }
            throw new IllegalArgumentException("No join operator with symbol " + value + " found");
        }
    }

    /**
     * Represents an operand in a search filter, used for XML-RPC serialization.
     */
    public final static class Operand {
        private final String field;
        private final SearchFilter.Operator operator;
        private final Object value;

        /**
         * Constructs an Operand from a SearchFilter
         * @param filter The search filter
         */
        public Operand(SearchFilter filter) {
            this(filter.getField(), filter.getOperator(), filter.getValue());
        }

        /**
         * Constructs an Operand
         * @param field The field name
         * @param operator The operator
         * @param value The value
         */
        public Operand(String field, SearchFilter.Operator operator, Object value) {
            this.field = field;
            this.operator = operator;
            this.value = (value == null || value.equals("null")) ? false : value;
        }

        /**
         * Converts the operand to an object array for XML-RPC
         * @return An object array
         */
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
