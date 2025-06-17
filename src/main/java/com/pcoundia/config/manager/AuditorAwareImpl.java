package com.pcoundia.config.manager;

import com.pcoundia.security.services.AuthenticationSystem;
import com.pcoundia.security.services.UserDetailsImpl;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        if (!AuthenticationSystem.isLogged()) {
            return Optional.empty();
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Optional.of(userDetails.getId());
    }
}
