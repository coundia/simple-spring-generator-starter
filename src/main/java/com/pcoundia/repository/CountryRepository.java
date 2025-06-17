package com.pcoundia.repository;

import com.pcoundia.model.entity.Country;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends ICrudRepository<Country> {
}
