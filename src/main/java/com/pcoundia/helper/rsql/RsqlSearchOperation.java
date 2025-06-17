package com.pcoundia.helper.rsql;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.RSQLOperators;

import java.util.Arrays;

public enum RsqlSearchOperation {
    EQUAL(RSQLOperators.EQUAL),
    NOT_EQUAL(RSQLOperators.NOT_EQUAL),
    GREATER_THAN(RSQLOperators.GREATER_THAN),
    GREATER_THAN_OR_EQUAL(RSQLOperators.GREATER_THAN_OR_EQUAL),
    LESS_THAN(RSQLOperators.LESS_THAN),
    LESS_THAN_OR_EQUAL(RSQLOperators.LESS_THAN_OR_EQUAL),
    IN(RSQLOperators.IN),
    NOT_IN(RSQLOperators.NOT_IN),

    LIKE(new ComparisonOperator("=like=")),

    KEYVALUE(new ComparisonOperator("=kv="));

    private ComparisonOperator operator;

    RsqlSearchOperation(final ComparisonOperator operator) {
        this.operator = operator;
    }

    public static RsqlSearchOperation getSimpleOperator(final ComparisonOperator operator) {
        // System.out.println(operator);
        // System.out.println(Arrays.stream(values()).map(RsqlSearchOperation::getOperator).collect(Collectors.toList()));
        return Arrays.stream(values())
                .filter(operation -> {
                    return operation.getOperator() == operator || operation.getOperator().equals(operator);
                })
                .findAny().orElse(null);
    }

    public ComparisonOperator getOperator() {
        return operator;
    }
}