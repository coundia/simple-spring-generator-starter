package com.pcoundia.service.implementation;

import com.pcoundia.exception.ApiException;
import com.pcoundia.exception.EntityNotFoundException;
import com.pcoundia.helper.amqp.sender.AmqpSenderHelper;
import com.pcoundia.helper.app.SpecificationHelper;
import com.pcoundia.helper.rsql.CustomRsqlVisitor;
import com.pcoundia.jpa.specification.GroupBySpecification;
import com.pcoundia.model.entity.BaseEntity;
import com.pcoundia.repository.ICrudRepository;
import com.pcoundia.security.services.AuthenticationSystem;
import com.pcoundia.service.ICrudMeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @param <E> an Entity extending BaseEntity
 * @param <R> a CRUD Repository
 */
public abstract class CrudMeService<E extends BaseEntity, R extends ICrudRepository<E>> extends CrudService<E, R> implements ICrudMeService<E, R> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected AmqpSenderHelper<E> amqpSenderHelper;

    protected final R repository;

    protected final EntityManager entityManager;

//    protected Class<E> genericType;

    public CrudMeService(R repository, EntityManager entityManager){
        super(repository, entityManager);
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> findAllMe(boolean pageable, int page, int size, boolean isDeleted, String search) {
        if (!AuthenticationSystem.isLogged()) {
            throw new AccessDeniedException("Vous n'avez pas acces à cette ressource");
        }
        Map<String, Object> result = new HashMap<>();
        Session session = entityManager.unwrap(Session.class);
        // Filter filter = session.enableFilter("deletedFilter");
        // filter.setParameter("isDeleted", isDeleted);
        List<E> items = new ArrayList<E>();
        Specification<E> spec = null;
        if (search != null) {
            Set<ComparisonOperator> operators = RSQLOperators.defaultOperators();
            // We add the operators that we want for RSQL
            operators.add(new ComparisonOperator("=kv=", true));
            Node rootNode = new RSQLParser(operators).parse(search);
            spec = rootNode.accept(new CustomRsqlVisitor<E>());
            Specification<E> meSpec = specMe();
            if (meSpec != null)
                spec = spec.and(meSpec);
        } else {
            spec = Specification.where(specMe());
        }
        if (!pageable) {
            items = repository.findAll(spec);
        } else {
            Pageable paging = PageRequest.of(page-1, size);
            Page<E> pageTuts = repository.findAll(spec, paging);
            // pageTuts = itemRepository.findAll(paging);
            items = new ArrayList<>(pageTuts.getContent());
            result.put("totalItems", pageTuts.getTotalElements());
            result.put("totalPages", pageTuts.getTotalPages());
        }
        // session.disableFilter("deletedFilter");
        result.put("data", items);
        return result;
    }

    /**
     * @return The specification with the condition of the object associated with *me*
     */
    public abstract Specification<E> specMe();

    /**
     * We set the property that allow us to know that the object that we save is from *me*
     * @param item The item that we create or update
     */
    public abstract void setMeInItem(E item);

    /**
     *
     * @param item The item that we want to check from
     * @return true if the item is from me, false if it's not from me
     */
    public abstract boolean isItemFromMe(E item);

    @Transactional(readOnly = true)
    public Long countMe(boolean isDeleted, String search) {
        if (!AuthenticationSystem.isLogged()) {
            throw new AccessDeniedException("Vous n'avez pas acces à cette ressource");
        }
        Map<String, Object> result = new HashMap<>();
        Session session = entityManager.unwrap(Session.class);
        // Filter filter = session.enableFilter("deletedFilter");
        // filter.setParameter("isDeleted", isDeleted);
        List<E> items = new ArrayList<E>();
        Specification<E> spec = null;
        if (search != null) {
            Set<ComparisonOperator> operators = RSQLOperators.defaultOperators();
            // We add the operators that we want for RSQL
            operators.add(new ComparisonOperator("=kv=", true));
            Node rootNode = new RSQLParser(operators).parse(search);
            spec = rootNode.accept(new CustomRsqlVisitor<E>());
            Specification<E> meSpec = specCountMe();
            if (meSpec != null)
                spec = spec.and(meSpec);
        } else {
            spec = Specification.where(specCountMe());
        }
        return repository.count(spec);

    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> countMeGroupBy(boolean isDeleted, String search, List<String> groupByFields) {
        List<Map<String, Object>> result = new ArrayList<>();
        Session session = entityManager.unwrap(Session.class);

        Specification<E> spec = null;
        if (search != null) {
            Set<ComparisonOperator> operators = RSQLOperators.defaultOperators();
            operators.add(new ComparisonOperator("=kv=", true));
            Node rootNode = new RSQLParser(operators).parse(search);
            spec = rootNode.accept(new CustomRsqlVisitor<E>());
            Specification<E> meSpec = specCountMe();
            if (meSpec != null)
                spec = spec.and(meSpec);
        } else {
            spec = Specification.where(specCountMe());
        }

        GroupBySpecification<E> groupBySpec = new GroupBySpecification<E>(groupByFields);
        spec = spec.and(groupBySpec);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<E> root = query.from(genericType);
        List<Expression<?>> groupByExpressions = SpecificationHelper.getExpressionsFromFieldsEvenJson(root, cb, groupByFields);

        query.groupBy(groupByExpressions);
        Predicate specPredicate = spec.toPredicate(root, query, cb);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(specPredicate);

        query.where(predicates.toArray(new Predicate[0]));


        List<Selection<?>> selections = groupByExpressions.stream().map(item -> (Selection<?>) item).collect(Collectors.toList());

        selections.add(cb.count(root));
        query.multiselect(selections);

        List<Object[]> groupedResults = entityManager.createQuery(query).getResultList();

        List<String> aliases = SpecificationHelper.getAliasFromGroupByFields(groupByFields);
        for (Object[] row : groupedResults) {

            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < groupByFields.size(); i++) {
                map.put(aliases.get(i), row[i]);
            }
            map.put("count", row[groupByFields.size()]);
            result.add(map);
        }

        return result;
    }

    /**
     * @return The specification count with the condition of the object associated with *me*
     */
    public Specification<E> specCountMe() {
        return specMe();
    }

    @Transactional
    @Override
    public E createMe(E item) {
        if (!AuthenticationSystem.isLogged()) {
            throw new AccessDeniedException("Vous n'avez pas acces à cette ressource");
        }
        setMeInItem(item);
        if (hasExternalRelations)
            validateExternalRelations(item, null);
        return repository.save(item);
    }

    @Transactional
    @Override
    public E updateMe(Long id, E item) {
        if (!AuthenticationSystem.isLogged()) {
            throw new AccessDeniedException("Vous n'avez pas acces à cette ressource");
        }
        E itemBefore = internalFindById(id);
        if (!isItemFromMe(itemBefore))
            throw new AccessDeniedException("Cette ressource ne vous appartient pas");
        item.setId(itemBefore.getId());
        return repository.save(item);
    }

    @Transactional
    @Override
    public E patchMe(Long id, Map<String, Object> patch) {
        E item = internalFindById(id);
        if (!isItemFromMe(item))
            throw new AccessDeniedException("Cette ressource ne vous appartient pas");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        E copy = null;
        try {
            TokenBuffer tb = new TokenBuffer(objectMapper, false);
            objectMapper.writeValue(tb, item);
            copy = objectMapper.readValue(tb.asParser(), genericType);
        } catch (UnrecognizedPropertyException e) {
            log.warn("Error when cloning object {}", item);
            throw new ApiException(e.getMessage());
        } catch (IOException e) {
            log.warn("Error when cloning object {}", item);
            throw new ApiException(e.getMessage());
        }
        ObjectReader objectReader = objectMapper.readerForUpdating(copy);
        JsonNode jsonNode = objectMapper.valueToTree(patch);
        E itemNew;
        try {
            itemNew = objectReader.readValue(jsonNode);
            if (hasExternalRelations)
                validateExternalRelations(itemNew, item);
            repository.save(itemNew);
            // Si l'amqp est supporté par le service, on notifie les autres microservices de la modification a travers le rabbit mq
            if (amqpSenderHelper != null) {
                amqpSenderHelper.sendMessage(itemNew);
                log.info("Object {} sent via RabbitMQ", itemNew);
            }
        }
        catch (UnrecognizedPropertyException e) {
            log.error("Error when converting Map to {} for data {}", getClass(), patch);
            throw new ApiException(e.getMessage());
        } catch (IOException e) {
            log.error("Error when converting Map to {} for data {}", getClass(), patch);
            throw new ApiException(e.getMessage());
        }
        return itemNew;
    }

    @Transactional(readOnly = true)
    @Override
    public E findOneMe(Long id) {
        E item = internalFindById(id);
        if (!isItemFromMe(item))
            throw new AccessDeniedException("Cette ressource ne vous appartient pas");
        return item;
    }

    @Transactional
    @Override
    public void deleteMe(Long id) {
        E item = internalFindById(id);
        if (!isItemFromMe(item))
            throw new AccessDeniedException("Cette ressource ne vous appartient pas");
        repository.deleteById(id);
    }

    // Cette methode nous permet de recupere l'instance a partir d'un id et de pouvoir lancer une exception si l'objet n'exisite pas
    // On a besoin de la class de l'objet non trouvé pour lancer l'exception du le bout de code qui precede l'appel
    protected E internalFindById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(genericType, "id", id.toString()));
    }

    protected String convertFromMap(Map<String, Object> map) {
        try {
            return (new ObjectMapper()).writeValueAsString(map);
        } catch (JsonProcessingException exception) {
            log.info("Erreur lors de la conversion en json du map {}", map);
        }
        return null;
    }
}
