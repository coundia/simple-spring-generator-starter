package com.pcoundia.service;


import com.pcoundia.repository.ICrudRepository;

import java.util.Map;

public interface ICrudMeService<E,  R extends ICrudRepository<E>> {

    Map<String, Object> findAllMe(boolean pageable, int page, int size, boolean isDeleted, String search);

    E createMe(E item);

    E updateMe(Long id, E item);

    E patchMe(Long id, Map<String, Object> patch);

    E findOneMe(Long id);

    void deleteMe(Long id);

}
