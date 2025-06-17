package com.pcoundia.service;


import com.pcoundia.repository.ICrudRepository;

import java.util.Map;

public interface ICrudService<E,  R extends ICrudRepository<E>> {

    Map<String, Object> findAll(boolean pageable, int page, int size, boolean isDeleted, String search);

    E create(E item);

    E update(Long id, E item);

    E patch(Long id, Map<String, Object> patch);

    E findOne(Long id);

    void delete(Long id);

}
