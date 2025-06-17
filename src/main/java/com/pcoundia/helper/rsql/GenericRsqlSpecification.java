package com.pcoundia.helper.rsql;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import org.hibernate.query.criteria.internal.path.PluralAttributePath;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GenericRsqlSpecification<T> implements Specification<T> {

    private String property;
    private ComparisonOperator operator;
    private List<String> arguments;

    public GenericRsqlSpecification(final String property, final ComparisonOperator operator, final List<String> arguments) {
        super();
        this.property = property;
        this.operator = operator;
        this.arguments = arguments;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) {
        Path<?> propertyExpression = parseProperty(root);
        final List<Object> args = castArguments(propertyExpression);
        final Object argument = args.get(0);
        // System.out.println(operator);
        // System.out.println("Arguments : "+args);
        // System.out.println("The operator + " + RsqlSearchOperation.getSimpleOperator(operator));
        switch (RsqlSearchOperation.getSimpleOperator(operator)) {

            case EQUAL: {
                if (argument instanceof String) {
                    return builder.like(builder.lower(propertyExpression.as(String.class)), argument.toString().replace('*', '%').toLowerCase());
                    // return builder.like(propertyExpression, argument.toString().replace('*', '%'));
                } else if (argument == null) {
                    if (propertyExpression instanceof PluralAttributePath)
                        return builder.isEmpty(propertyExpression.as(List.class));
                    return builder.isNull(propertyExpression);
                } else {
                    return builder.equal(propertyExpression, argument);
                }
            }
            case NOT_EQUAL: {
                if (argument instanceof String) {
                    return builder.notLike(propertyExpression.as(String.class), argument.toString().replace('*', '%'));
                } else if (argument == null) {
                    if (propertyExpression instanceof PluralAttributePath)
                        return builder.isNotEmpty(propertyExpression.as(List.class));
                    return builder.isNotNull(propertyExpression);
                } else {
                    return builder.notEqual(propertyExpression, argument);
                }
            }
            case GREATER_THAN: {
                if (argument instanceof Date) {
                    Path<Date> propertyDate = (Path<Date>) propertyExpression.as(Date.class);
                    return builder.greaterThan(propertyDate, (Date) argument);
                }
                return builder.greaterThan(propertyExpression.as(String.class), argument.toString());
            }
            case GREATER_THAN_OR_EQUAL: {
                if (argument instanceof Date) {
                    Path<Date> propertyDate = (Path<Date>) propertyExpression.as(Date.class);
                    return builder.greaterThanOrEqualTo(propertyDate, (Date) argument);
                }
                return builder.greaterThanOrEqualTo(propertyExpression.as(String.class), argument.toString());
            }
            case LESS_THAN: {
                if (argument instanceof Date) {
                    Path<Date> propertyDate = (Path<Date>) propertyExpression.as(Date.class);
                    return builder.lessThan(propertyDate, (Date) argument);
                }
                return builder.lessThan(propertyExpression.as(String.class), argument.toString());
            }
            case LESS_THAN_OR_EQUAL: {
                if (argument instanceof Date) {
                    Path<Date> propertyDate = (Path<Date>) propertyExpression.as(Date.class);
                    return builder.lessThanOrEqualTo(propertyDate, (Date) argument);
                }
                return builder.lessThanOrEqualTo(propertyExpression.as(String.class), argument.toString());
            }
            case IN:
                return propertyExpression.in(args);
            case NOT_IN:
                return builder.not(propertyExpression.in(args));
            case KEYVALUE:
                // Construct the JSON_EXTRACT expression
                String jsonPath = "$." + argument; // Construct the JSON path
                if (argument instanceof String) {
                    Class<?> javaType = determineJavaType(args.get(1).toString());
                    if (javaType == String.class)
                        return builder.like(
                            builder.lower(builder.function("JSON_EXTRACT", String.class, propertyExpression.as(String.class), builder.literal(jsonPath))),
                            "%" + args.get(2).toString().toLowerCase() + "%");
                    else
                        return builder.equal(
                                builder.function("JSON_EXTRACT", javaType, propertyExpression.as(String.class), builder.literal(jsonPath)),
                                args.get(2)
                        );

                }
                // return builder.like(builder.lower(propertyExpression.as(String.class)), argument.toString().replace('*', '%').toLowerCase());
                return builder.equal(propertyExpression.get(args.get(0).toString()), args.get(1));
        }

        return null;
    }

    // This method will help us diving deep into nested property using the dot convention
    // The originial tutorial did not have this, so it can only parse the shallow properties.
    private Path<String> parseProperty(Root<T> root) {
        Path<String> path;
        if (property.contains(".")) {
            // Nested properties
            String[] pathSteps = property.split("\\.");
            String step = pathSteps[0];
            path = root.get(step);
            From lastFrom = root;

            for (int i = 1; i <= pathSteps.length - 1; i++) {
                if(path instanceof PluralAttributePath) {
                    PluralAttribute attr = ((PluralAttributePath) path).getAttribute();
                    Join join = getJoin(attr, lastFrom);
                    path = join.get(pathSteps[i]);
                    lastFrom = join;
                } else if(path instanceof SingularAttributePath) {
                    SingularAttribute attr = ((SingularAttributePath) path).getAttribute();
                    if(attr.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC) {
                        Join join = lastFrom.join(attr, JoinType.LEFT);
                        path = join.get(pathSteps[i]);
                        lastFrom = join;
                    } else {
                        // Check if this is a JSON field
                        if (attr.getJavaType().isAssignableFrom(String.class)) {
                            return path;
                        }
                        path = path.get(pathSteps[i]);
                    }
                }  else {
                    path = path.get(pathSteps[i]);
                }
            }
        } else {
            path = root.get(property);
        }
        return path;
    }

    private Class<?> determineJavaType(String returnType) {
        switch (returnType) {
            case "long":
                return Long.class;
            case "string":
                return String.class;
            // Add more cases for other types if needed
            default:
                throw new IllegalArgumentException("Unsupported return type: " + returnType);
        }
    }

    private boolean isJsonField(Path<?> path) {
        Attribute<?, ?> attr = (Attribute<?, ?>) path.getModel();

        // If the persistentAttributeType != BASIC,
        // then we assume it's some kind of association
        return (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC);
    }

    private Join getJoin(PluralAttribute attr, From from) {
        final Set<?> joins = from.getJoins();
        for (Object object : joins) {
            Join<?, ?> join = (Join<?, ?>) object;
            if (join.getAttribute().getName().equals(attr.getName())) {
                return join;
            }
        }
        return createJoin(attr, from);
    }

    private Join createJoin(PluralAttribute attr, From from) {
        switch (attr.getCollectionType()){
            case COLLECTION:
                return from.join((CollectionAttribute) attr);
            case SET:
                return from.join((SetAttribute) attr);
            case LIST:
                return from.join((ListAttribute) attr);
            case MAP:
                return from.join((MapAttribute) attr);
            default:
                return null;
        }
    }

    // === private

    private List<Object> castArguments(Path<?> propertyExpression) {
        Class<?> type = propertyExpression.getJavaType();

        return arguments.stream().map(arg -> {

            if (type.toString().equals("int")) {
                if (arg.equals("null")) {
                    return null;
                }
                return Integer.parseInt(arg);
            } else if (type.equals(Integer.class)) {
                if (arg.equals("null")) {
                    return null;
                }
                return Integer.parseInt(arg);
            } else if (type.toString().equals("long")) {
                if (arg.equals("null")) {
                    return null;
                }
                return Long.parseLong(arg);
            } else if (type.equals(Long.class)) {
                if (arg.equals("null")) {
                    return null;
                }
                return Long.parseLong(arg);
            } else if (type.toString().equals("double")) {
                if (arg.equals("null")) {
                    return null;
                }
                return Double.parseDouble(arg);
            } else if (type.toString().equals("boolean")) {
                if (arg.equals("null")) {
                    return null;
                }
                return Boolean.parseBoolean(arg);
            } else if (type.equals(Boolean.class)) {
                if (arg.equals("null")) {
                    return null;
                }
                return Boolean.parseBoolean(arg);
            } else if (type.equals(Byte.class)) return Byte.parseByte(arg);
            else if (type.equals(Date.class)) {
                System.out.println(arg);
                try {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(arg);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

            } else if (type.equals(List.class)) {
                if (arg.equals("null"))
                    return null;
                return arg;
            }
            else {
                // if (arg.equals("null"))
                    // return null;
                return arg;
            }
        }).collect(Collectors.toList());
    }

}
