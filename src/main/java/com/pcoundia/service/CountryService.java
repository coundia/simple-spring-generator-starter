package com.pcoundia.service;

import com.pcoundia.model.entity.Country;
import com.pcoundia.repository.CountryRepository;
import com.pcoundia.service.implementation.CrudService;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;


@Service
public class CountryService extends CrudService<Country, CountryRepository>  {

	public CountryService(CountryRepository repository, EntityManager entityManager) {
		super(repository, entityManager);
		genericType = Country.class;
	}

}

