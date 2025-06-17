package com.pcoundia.security.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.*;

@ToString
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;
    private Long id;

    private String firstName;

    private String lastName;

    private String username;
    private boolean active;
    private String email;

    @ToString.Exclude
    @JsonIgnore
    private String password;

    private Long applicantId;

    private Map<String, Object> applicant;

    private Long agentId;

    private Map<String, Object> agent;

    private Long partnerContactId;

    private Map<String, Object> partnerContact;

    private Map<String, Object> partner;

    private Map<String, Object> office;

    private Map<String, Object> branch;

    private List<Map<String, Object>> manOfBranches = new ArrayList<>();

    private Map<String, Object> division;

    @ToString.Exclude
    private Collection<? extends GrantedAuthority> authorities;
    public UserDetailsImpl(Long id, String username, String email, String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public UserDetailsImpl(Long id, String username, String email, String password, boolean active, Set<GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.active = active;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }
    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public String getUsername() {
        return username;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public Map<String, Object> getApplicant() {
        return applicant;
    }

    public void setApplicant(Map<String, Object> applicant) {
        this.applicant = applicant;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public Map<String, Object> getAgent() {
        return agent;
    }

    public void setAgent(Map<String, Object> agent) {
        this.agent = agent;
    }

    public Long getPartnerContactId() {
        return partnerContactId;
    }

    public void setPartnerContactId(Long partnerContactId) {
        this.partnerContactId = partnerContactId;
    }

    public Map<String, Object> getPartnerContact() {
        return partnerContact;
    }

    public void setPartnerContact(Map<String, Object> partnerContact) {
        this.partnerContact = partnerContact;
    }

    public Map<String, Object> getPartner() {
        return partner;
    }

    public void setPartner(Map<String, Object> partner) {
        this.partner = partner;
    }

    public Map<String, Object> getOffice() {
        return office;
    }

    public void setOffice(Map<String, Object> office) {
        this.office = office;
    }

    public Map<String, Object> getBranch() {
        return branch;
    }

    public void setBranch(Map<String, Object> branch) {
        this.branch = branch;
    }

    public List<Map<String, Object>> getManOfBranches() {
        return manOfBranches;
    }

    public void setManOfBranches(List<Map<String, Object>> manOfBranches) {
        this.manOfBranches = manOfBranches;
    }

    public Map<String, Object> getDivision() {
        return division;
    }

    public void setDivision(Map<String, Object> division) {
        this.division = division;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }
    @Override
    public boolean isAccountNonLocked() {
        return active;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }
    @Override
    public boolean isEnabled() {
        return active;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }

//    @Override
//    public String toString() {
//        return "UserDetailsImpl{" +
//                "id=" + id +
//                ", username='" + username + '\'' +
//                ", active=" + active +
//                ", email='" + email + '\'' +
//                ", authorities=" + authorities +
//                '}';
//    }
}
