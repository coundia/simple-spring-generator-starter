package com.pcoundia.jpa.specification;

import com.pcoundia.helper.app.SpecificationHelper;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.List;

public class GroupBySpecification<E> implements Specification<E> {
    private List<String> groupByFields;


    public GroupBySpecification(List<String> groupByFields) {
        this.groupByFields = groupByFields;
    }

    @Override
    public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

        List<Expression<?>> groupByExpressions = SpecificationHelper.getExpressionsFromFieldsEvenJson(root, criteriaBuilder, groupByFields);

        query.multiselect(groupByExpressions.toArray(new Expression[0]))
                .multiselect(criteriaBuilder.count(root));

        query.groupBy(groupByExpressions);

        return criteriaBuilder.conjunction();

    }

}
