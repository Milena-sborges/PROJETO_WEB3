package com.ms.user.security.service;

import com.ms.user.models.UserModel;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter
public class UserDetailsImpl implements UserDetails {
    
    private final UserModel user;
    private List<SimpleGrantedAuthority> authorities;
    
    public UserDetailsImpl(UserModel user) {
        this.user = user;
        // Carrega as authorities no construtor (ainda dentro da sessão do Hibernate)
        this.authorities = loadAuthorities();
    }
    
    private List<SimpleGrantedAuthority> loadAuthorities() {
        List<SimpleGrantedAuthority> auths = new ArrayList<>();
        
        try {
            if (user.getRoles() != null) {
                // Força o carregamento chamando .size()
                user.getRoles().size();
                
                user.getRoles().stream()
                    .filter(Objects::nonNull)
                    .forEach(role -> {
                        if (role.getName() != null) {
                            auths.add(new SimpleGrantedAuthority(role.getName().name()));
                        } else {
                            auths.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
                        }
                    });
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar authorities: " + e.getMessage());
        }
        
        // Se não tiver nenhuma authority, coloca ROLE_CUSTOMER como padrão
        if (auths.isEmpty()) {
            auths.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        }
        
        return auths;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }
    
    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getEmail(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}