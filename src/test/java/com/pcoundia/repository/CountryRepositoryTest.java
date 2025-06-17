package com.pcoundia.repository;

import com.pcoundia.model.entity.Country;
import com.pcoundia.repository.CountryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CountryRepositoryTest {

    @Autowired
    private CountryRepository repository;

    @Test
    void it_should_save_and_find_entity() {
        Country entity = new Country();

        String codeValue = UUID.randomUUID().toString();
        entity.setCode(codeValue);
        String nameValue = UUID.randomUUID().toString();
        entity.setName(nameValue);
        String alpha2Value = UUID.randomUUID().toString();
        entity.setAlpha2(alpha2Value);

        Country saved = repository.save(entity);
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).contains(saved);
        assertThat(saved.getCode()).isEqualTo(codeValue);
        assertThat(saved.getName()).isEqualTo(nameValue);
        assertThat(saved.getAlpha2()).isEqualTo(alpha2Value);


    }
}
