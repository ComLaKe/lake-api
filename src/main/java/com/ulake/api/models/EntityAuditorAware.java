package com.ulake.api.models;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;


import java.util.Optional;

public class EntityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return Optional.ofNullable(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
    }
}