package com.pcoundia.security;


import com.pcoundia.security.basic.MyBasicAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class BasicWebSecurityConfig {

    @Autowired private MyBasicAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser("eureka")
                .password(passwordEncoder.encode("user1Pass"))
                .roles("ACTUATOR")

        ;
                // .authorities("ACTUATOR");
    }

    @Bean
    @Order(2)
    public SecurityFilterChain basicFilterChain(HttpSecurity http) throws Exception {
        http.antMatcher("/actuator/**").authorizeRequests()
                .antMatchers("/actuator/**")
                .hasRole("ACTUATOR")


                // .hasAuthority("ACTUATOR")
                // .authenticated()
                .and()
                .antMatcher("/card/**").authorizeRequests()
                .and()
                .antMatcher("/reports/**").authorizeRequests()

                .and()
                .antMatcher("/static/**").authorizeRequests()
                .and()

                .httpBasic()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);;
        // http.addFilterAfter(new CustomFilter(), BasicAuthenticationFilter.class);
        return http.build();
    }


}
