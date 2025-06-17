package com.pcoundia.security.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        /*
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        Set<GrantedAuthority> authorities = new HashSet<>();
        System.out.println("OKOKKO");
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            for (RoleToAction userRoleToAction : role.getRoleToActions()) {
                authorities.add(new SimpleGrantedAuthority(userRoleToAction.getAction().getCode()));
            }
        }

        System.out.println(authorities);
        return new UserDetailsImpl(user.getId(), user.getUsername(), user.getEmail(), user.getPassword(), user.isActive(), authorities);
        // return UserDetailsImpl.build(user);
        */
        return null;
    }
}