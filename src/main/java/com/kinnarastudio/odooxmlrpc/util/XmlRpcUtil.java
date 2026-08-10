package com.kinnarastudio.odooxmlrpc.util;

import com.kinnarastudio.odooxmlrpc.model.SearchFilter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class XmlRpcUtil {
    private XmlRpcUtil() {}

    @Nonnull
    public static Object[] prefixation(SearchFilter[] filters) {
        if (filters == null || filters.length == 0) {
            return new Object[0];
        }

        List<Object> result = new ArrayList<>();

        for (SearchFilter filter : filters) {
            if (result.isEmpty()) {
                result.add(new SearchFilter.Operand(filter));
            } else {
                SearchFilter.Join join = filter.getJoin();
                result.add(0, join);
                result.add(new SearchFilter.Operand(filter));
            }
        }

        return result.stream()
                .map(o -> {
                    if (o instanceof SearchFilter.Operand) {
                        return ((SearchFilter.Operand) o).toObjects();
                    } else if (o instanceof SearchFilter.Join) {
                        return o.toString();
                    } else {
                        return o;
                    }
                }).toArray();
    }

    @Nonnull
    public static Object[] mathematicPrefixation(SearchFilter[] filters) {
        if (filters == null || filters.length == 0) {
            return new Object[0];
        }

        Stack<List<Object>> operandStack = new Stack<>();
        Stack<SearchFilter.Join> operatorStack = new Stack<>();

        for (SearchFilter filter : filters) {
            if (operandStack.isEmpty()) {
                operandStack.push(new ArrayList<>() {{
                    add(new SearchFilter.Operand(filter));
                }});
            } else {
                SearchFilter.Join operator = filter.getJoin();
                if (operator == SearchFilter.Join.OR) {
                    operatorStack.push(operator);
                    operandStack.push(new ArrayList<>() {{
                        add(new SearchFilter.Operand(filter));
                    }});
                } else {
                    List<Object> pop = operandStack.pop();
                    pop.add(0, operator);
                    pop.add(0, new SearchFilter.Operand(filter));
                    operandStack.push(pop);
                }
            }
        }

        List<Object> result = new ArrayList<>();
        while (!operatorStack.isEmpty()) {
            SearchFilter.Join operator = operatorStack.pop();
            List<Object> right = operandStack.pop();
            List<Object> left = operandStack.pop();

            result.add(operator);
            result.add(left);
            result.add(right);
        }

        return result.toArray();
    }
}
