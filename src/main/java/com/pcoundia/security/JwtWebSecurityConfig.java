package com.pcoundia.security;

import com.pcoundia.security.jwt.AuthTokenFilter;
import com.pcoundia.security.jwt.UnauthorizedEntryPointJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

@ComponentScan(basePackages = "com.pcoundia")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        // securedEnabled = true,
        // jsr250Enabled = true,
        prePostEnabled = true)
public class JwtWebSecurityConfig {

    //@Value("${eureka.username}")
    private String username= "eureka";

    //@Value("${eureka.password}")
    private String password = "eureka";

    //------------------------------------------------ JWT SECURITY --------------------------------------------------
    @Autowired
    private UnauthorizedEntryPointJwt unauthorizedHandler;

    /**
     * JWT Token Filter
     * @return The Class in charge of filtering all request containing JWT token
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> characterEncodingFilterApp() {
        FilterRegistrationBean<CharacterEncodingFilter> registrationBean = new FilterRegistrationBean<>();
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8"); // Set to ISO-8859-1
        registrationBean.setFilter(characterEncodingFilter);
        registrationBean.addUrlPatterns("/*"); // Apply to all URL patterns
        return registrationBean;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors().and().csrf().disable()
                .antMatcher("/api/**")
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .antMatchers(HttpMethod.POST, "/api/applicants/register").permitAll()
                                .antMatchers("/download/**").permitAll()
                                .antMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**", "/api/subscription-agrijeunes/**/send-to-partner").permitAll()
                                .antMatchers("/api/**/internal", "/api/qrcode/**", "/static/**").permitAll()
                                .antMatchers("/card").permitAll()
                                .antMatchers("/reports/**").permitAll()
                                .antMatchers("/api/projects/published/available").permitAll()
//                                .antMatchers("/api/**").authenticated()
                                .antMatchers("/api/**").permitAll()
                                 .anyRequest().permitAll()
                )
                .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return httpSecurity.build();
    }
}