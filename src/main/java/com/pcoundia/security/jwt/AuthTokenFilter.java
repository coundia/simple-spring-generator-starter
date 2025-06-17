package com.pcoundia.security.jwt;

import com.pcoundia.exception.ApiException;
import com.pcoundia.security.services.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // String jwt = parseJwt(request);
            Map<String, Object> user = getUserFromRequest(request);
            if (user == null) {
                throw new ApiException("Utilisateur non reconnu");
            }
            Set<GrantedAuthority> authorities = new HashSet<>();
            // Claims claims = jwtUtils.getAllClaimsFromToken(jwt);
            for (String action : (List<String>) user.get("permissions")) {
                authorities.add(new SimpleGrantedAuthority(action));
            }
            for (Map<String, Object> role: (List<Map<String, Object>>)user.get("roles")) {
                authorities.add(new SimpleGrantedAuthority("ROLE_"+role.get("code").toString()));
            }
            UserDetailsImpl userDetails = new UserDetailsImpl(Long.valueOf(user.get("id").toString()), user.get("username").toString(), (user.get("email") != null) ? user.get("email").toString(): null, null, true, authorities);
            if (user.get("firstName") != null)
                userDetails.setFirstName(user.get("firstName").toString());
            if (user.get("lastName") != null)
                userDetails.setLastName(user.get("lastName").toString());

            if (user.get("applicantId") != null)
                userDetails.setApplicantId(Long.valueOf(user.get("applicantId").toString()));
            if (user.get("applicant") != null) {
                Map<String, Object> applicant = (Map<String, Object>) user.get("applicant");
                userDetails.setApplicant(applicant);
                if (applicant.get("branch") != null)
                    userDetails.setBranch((Map<String, Object>) applicant.get("branch"));
                else if (applicant.get("branchId") != null)
                    userDetails.setBranch(Map.of("id", Long.valueOf(applicant.get("branchId").toString())));
                if (applicant.get("office") != null)
                    userDetails.setOffice((Map<String, Object>) applicant.get("office"));
                else if (applicant.get("officeId")  != null) {
                    userDetails.setOffice(Map.of("id", Long.valueOf(applicant.get("officeId").toString())));
                }
            }
            if (user.get("agentId") != null)
                userDetails.setAgentId(Long.valueOf(user.get("agentId").toString()));
            if (user.get("agent") != null) {
                Map<String, Object> agent = (Map<String, Object>) user.get("agent");
                userDetails.setAgent(agent);
                if (agent.get("branch") != null)
                    userDetails.setBranch((Map<String, Object>) agent.get("branch"));
                if (agent.get("office") != null)
                    userDetails.setOffice((Map<String, Object>) agent.get("office"));
                if (agent.get("division") != null)
                    userDetails.setDivision((Map<String, Object>) agent.get("division"));
                if (agent.get("manOfBranches") != null)
                    userDetails.setManOfBranches((List<Map<String, Object>>) agent.get("manOfBranches"));
            }

            if (user.get("partnerContactId") != null)
                userDetails.setPartnerContactId(Long.valueOf(user.get("partnerContactId").toString()));
            if (user.get("partnerContact") != null) {
                Map<String, Object> partnerContact = (Map<String, Object>) user.get("partnerContact");
                userDetails.setPartnerContact(partnerContact);
                if (partnerContact.get("partner") != null)
                    userDetails.setPartner((Map<String, Object>) partnerContact.get("partner"));
            }


            // Map<String,Object> b = restTemplate.getForObject(uri, Map.class);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Request {} {} by user {}", request.getMethod(), request.getRequestURI(), user.get("username"));
        } catch (Exception e) {
            log.warn("Cannot set user authentication: {}", e.getMessage());
            log.info("Request {} {} by anonymous user", request.getMethod(), request.getRequestURI());
            // throw new ApiException(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)
            throws ServletException {
        String path = request.getRequestURI();
        // log.info(request.getRequestURI());
        return path.contains("/actuator") || path.contains("/static") ;
    }


    private Map<String, Object> getUserFromRequest(HttpServletRequest request) throws JsonProcessingException {
        String headerUser = request.getHeader("X-User");
        // log.info("X-User Raw {}", headerUser);
        if (headerUser == null)
            return null;
        headerUser = URLDecoder.decode(headerUser, StandardCharsets.UTF_8);
        // log.info("X-User Decoded {}", headerUser);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        // log.info("User info from request {}", headerUser);
        // Map<String, Object> user = objectMapper.convertValue(headerUser, HashMap.class);
        Map<String, Object> user = objectMapper.readValue(headerUser, HashMap.class);
        // log.info("User info from request after map {}", user);
        return user;
    }
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7, headerAuth.length());
        }
        return null;
    }
}
