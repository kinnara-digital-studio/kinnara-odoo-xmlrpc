package com.kinnarastudio.odooxmlrpc.service;

import com.kinnarastudio.odooxmlrpc.model.SearchFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating an array of {@link SearchFilter}
 */
public final class SearchBuilder {
    private final List<SearchFilter> filters;

    /**
     * Constructs a new SearchBuilder
     */
    public SearchBuilder() {
        filters = new ArrayList<>();
    }

    /**
     * Adds an "AND" filter with "EQUAL" operator
     * @param field The field name
     * @param value The value
     * @return The SearchBuilder instance
     */
    public SearchBuilder and(String field, Object value) {
        return and(field, SearchFilter.Operator.EQUAL, value);
    }

    /**
     * Adds a "NOT AND" filter by negating the operator
     * @param field The field name
     * @param operator The operator to negate
     * @param value The value
     * @return The SearchBuilder instance
     */
    public SearchBuilder nand(String field, SearchFilter.Operator operator, Object value) {
        SearchFilter.Operator notOperator;
        switch (operator) {
            case EQUAL:
                notOperator = SearchFilter.Operator.NOT_EQUAL;
                break;
            case NOT_EQUAL:
                notOperator = SearchFilter.Operator.EQUAL;
                break;
            case GREATER:
                notOperator = SearchFilter.Operator.LESS_EQUAL;
                break;
            case GREATER_EQUAL:
                notOperator = SearchFilter.Operator.LESS;
                break;
            case LESS:
                notOperator = SearchFilter.Operator.GREATER_EQUAL;
                break;
            case LESS_EQUAL:
                notOperator = SearchFilter.Operator.GREATER;
                break;
            case IN:
                notOperator = SearchFilter.Operator.NOT_IN;
                break;
            case NOT_IN:
                notOperator = SearchFilter.Operator.IN;
                break;
            case LIKE:
                notOperator = SearchFilter.Operator.NOT_LIKE;
                break;
            case NOT_LIKE:
                notOperator = SearchFilter.Operator.LIKE;
                break;
            case ILIKE:
                notOperator = SearchFilter.Operator.NOT_ILIKE;
                break;
            case NOT_ILIKE:
                notOperator = SearchFilter.Operator.ILIKE;
                break;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }

        return and(field, notOperator, value);
    }

    /**
     * Adds an "AND" filter
     * @param field The field name
     * @param operator The operator
     * @param value The value
     * @return The SearchBuilder instance
     */
    public SearchBuilder and(String field, SearchFilter.Operator operator, Object value) {
        filters.add(new SearchFilter(SearchFilter.Join.AND, field, operator, value));
        return this;
    }

    /**
     * Adds an "OR" filter with "EQUAL" operator
     * @param field The field name
     * @param value The value
     * @return The SearchBuilder instance
     */
    public SearchBuilder or(String field, Object value) {
        return or(field, SearchFilter.Operator.EQUAL, value);
    }

    /**
     * Adds a "NOT OR" filter with "EQUAL" operator
     * @param field The field name
     * @param value The value
     * @return The SearchBuilder instance
     */
    public SearchBuilder nor(String field, Object value) {
        return nor(field, SearchFilter.Operator.EQUAL, value);
    }

    /**
     * Adds a "NOT OR" filter by negating the operator
     * @param field The field name
     * @param operator The operator to negate
     * @param value The value
     * @return The SearchBuilder instance
     */
    public SearchBuilder nor(String field, SearchFilter.Operator operator, Object value) {
        SearchFilter.Operator notOperator;
        switch (operator) {
            case EQUAL:
                notOperator = SearchFilter.Operator.NOT_EQUAL;
                break;
            case NOT_EQUAL:
                notOperator = SearchFilter.Operator.EQUAL;
                break;
            case GREATER:
                notOperator = SearchFilter.Operator.LESS_EQUAL;
                break;
            case GREATER_EQUAL:
                notOperator = SearchFilter.Operator.LESS;
                break;
            case LESS:
                notOperator = SearchFilter.Operator.GREATER_EQUAL;
                break;
            case LESS_EQUAL:
                notOperator = SearchFilter.Operator.GREATER;
                break;
            case IN:
                notOperator = SearchFilter.Operator.NOT_IN;
                break;
            case NOT_IN:
                notOperator = SearchFilter.Operator.IN;
                break;
            case LIKE:
                notOperator = SearchFilter.Operator.NOT_LIKE;
                break;
            case NOT_LIKE:
                notOperator = SearchFilter.Operator.LIKE;
                break;
            case ILIKE:
                notOperator = SearchFilter.Operator.NOT_ILIKE;
                break;
            case NOT_ILIKE:
                notOperator = SearchFilter.Operator.ILIKE;
                break;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }

        return or(field, notOperator, value);
    }

    /**
     * Adds an "OR" filter
     * @param field The field name
     * @param operator The operator
     * @param value The value
     * @return The SearchBuilder instance
     */
    public SearchBuilder or(String field, SearchFilter.Operator operator, Object value) {
        filters.add(new SearchFilter(SearchFilter.Join.OR, field, operator, value));
        return this;
    }

    /**
     * Builds the array of {@link SearchFilter}
     * @return The array of {@link SearchFilter}
     */
    public SearchFilter[] build() {
        return filters.toArray(new SearchFilter[0]);
    }

    /**
     * Creates an "equal" search filter
     * @param field The field name
     * @param value The value
     * @return An array containing the search filter
     */
    public static SearchFilter[] eq(String field, Object value) {
        return new SearchFilter[] {new SearchFilter(field, SearchFilter.Operator.EQUAL, value)};
    }

    /**
     * Creates a "not equal" search filter
     * @param field The field name
     * @param value The value
     * @return An array containing the search filter
     */
    public static SearchFilter[] ne(String field, Object value) {
        return new SearchFilter[]{new SearchFilter(field, SearchFilter.Operator.NOT_EQUAL, value)};
    }

    /**
     * Creates an "in" search filter
     * @param field The field name
     * @param values The values
     * @return An array containing the search filter
     */
    public static SearchFilter[] in(String field, Object... values) {
        return new SearchFilter[]{new SearchFilter(field, SearchFilter.Operator.IN, values)};
    }

    /**
     * Creates a "not in" search filter
     * @param field The field name
     * @param values The values
     * @return An array containing the search filter
     */
    public static SearchFilter[] nin(String field, Object... values) {
        return new SearchFilter[]{new SearchFilter(field, SearchFilter.Operator.NOT_IN, values)};
    }
}
