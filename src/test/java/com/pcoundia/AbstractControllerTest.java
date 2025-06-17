package com.pcoundia;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Abstract base class for controller tests.
 * Provides common setup for MockMvc, ObjectMapper, and mocked service bean.
 * Extend this class in your controller test classes to reduce code duplication.
 *
 * @param <S> Service type to mock (e.g., ICrudService)
 * @param <R> Repository type (for future extension)
 *
 * Usage:
 *     class MyEntityControllerTest extends AbstractControllerTest<MyService, MyRepository> { ... }
 */

public abstract class AbstractControllerTest<S, R> {

	@Autowired
	protected MockMvc mockMvc;

	@MockBean
	protected S service;

	@Autowired
	protected ObjectMapper objectMapper;
}
