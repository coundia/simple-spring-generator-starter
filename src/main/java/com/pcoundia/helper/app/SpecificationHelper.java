package com.pcoundia.helper.app;

import com.pcoundia.exception.ApiException;
import com.pcoundia.jpa.function.UnitExpression;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.analysis.function.Exp;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Specification methods which can be used to create a specification based query
 */
@Slf4j
public final class SpecificationHelper {

    private SpecificationHelper() {}

    /**
     * @param root
     * @param criteriaBuilder
     * @param fields It can be nested property or basic property from current Entity
     * @return List of expressions which can be added in a groupBy or in the select of the query builder based on "fields"
     * @param <E> Any entity
     */
    public static <E> List<Expression<?>> getExpressionsFromFieldsEvenJson(Root<E> root, CriteriaBuilder criteriaBuilder, List<String> fields) {
        List<Expression<?>> expressions = new ArrayList<>();

        for (String field : fields) {
            String[] fieldParts = field.split("\\|");
            String fieldOnly = fieldParts[0];
            String basicFunction = null;
            String alias = fieldParts[fieldParts.length-1];
            if (fieldParts.length>1)
                basicFunction = fieldParts[1];

            Map<String, Object> resultGetPath = getPathEvenJson(root, fieldOnly);
            Path<?> path = (Path<?>) resultGetPath.get("path");
            String[] subPart = (String[]) resultGetPath.get("subPart");
//            Path<?> path = getPath(root, field);
//            log.info("SubPart of field {}: {}", field, subPart);
            if (subPart.length>0) {
                String jsonPath = "$." + String.join(".", subPart);
//                log.info("JsonPath of field {}: {}", field, jsonPath);
                Expression<?> jsonExtractExpression = criteriaBuilder.function(
                        "json_unquote",
                        String.class,
                        criteriaBuilder.function(
                                "json_extract",
                                String.class,
                                path,
                                criteriaBuilder.literal(jsonPath)
                        )
                );
                if (basicFunction != null) {
                    if ("age".equals(basicFunction.toLowerCase())) {
                        jsonExtractExpression = getAgeForPath(criteriaBuilder, jsonExtractExpression);
                    } else
                        jsonExtractExpression = basicFunctionForPath(criteriaBuilder, jsonExtractExpression, basicFunction);
                }
//                jsonExtractExpression.alias(alias);
//                Expression<String> jsonExtractExpression = criteriaBuilder.function("JSON_EXTRACT", String.class, path, criteriaBuilder.literal(jsonPath));
                expressions.add(jsonExtractExpression);
            } else {
//                path.alias(alias);
                if (basicFunction != null) {
                    Expression<?> newExpression;
                    if ("age".equals(basicFunction.toLowerCase())) {
                        newExpression = getAgeForPath(criteriaBuilder, path);
                    } else {
                        newExpression = basicFunctionForPath(criteriaBuilder, path, basicFunction);
                    }
//                    newExpression.alias("age");
                    expressions.add(newExpression);
                } else
                    expressions.add(path);
            }

        }
        return expressions;
    }


    /**
     * @param root
     * @param field the field can be any expression allowing us to access an entity property. It can be "name" or "address.name"
     * @return Allowing us to get the access the path of the field in parameter
     */
    public static Map<String, Object> getPathEvenJson(Path<?> root, String field) {
        String[] parts = field.split("\\.");
        for (int i =0; i< parts.length; i++) {
//            String part = parts[i];
            root = root.get(parts[i]);

            if (root instanceof SingularAttributePath) {
                SingularAttribute attr = ((SingularAttributePath) root).getAttribute();

                if(attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC) {
                    // Check if this is a JSON field

                    String[] subParts = Arrays.copyOfRange(parts, i+1, parts.length);
                    if (root.getJavaType().isAssignableFrom(String.class)) {
//                        log.info("Field {} is in json", field);
                        return Map.of("path", root, "subPart", subParts);
                    }
                }
            }
        }
        return Map.of("path", root, "subPart", new String[0]);
    }

    /**
     * An expression allowing us to get the age based on a field Path
     * @param criteriaBuilder
     * @param expression
     * @return The expression based on the row of a field
     */
    private static Expression<?> getAgeForPath(CriteriaBuilder criteriaBuilder, Expression<?> expression) {
        UnitExpression year = new UnitExpression(null, String.class, "YEAR");
        return criteriaBuilder.function(
                "TIMESTAMPDIFF",
                Integer.class,
                year,
                expression,
                criteriaBuilder.currentDate()
        );
    }


    /**
     * We want to call a basic function with only one parameter being the field Path and use it as an expression in our custom query
     * @param criteriaBuilder
     * @param expression
     * @param basicFunction
     * @return An expression based on the result of the call of the basic function on the path
     */
    private static Expression<?> basicFunctionForPath(CriteriaBuilder criteriaBuilder, Expression<?> expression, String basicFunction) {
        return criteriaBuilder.function(
                basicFunction.toUpperCase(),
                String.class,
                expression
        );
    }


    public static List<String> getAliasFromGroupByFields(List<String> groupByFields) {
        List<String> aliases = new ArrayList<>();
        for (String field: groupByFields) {
            String[] fieldParts = field.split("\\|");
            aliases.add(fieldParts[fieldParts.length-1]);
        }
        return aliases;
    }

}
