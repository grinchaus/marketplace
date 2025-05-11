package com.mycompany.auth.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mycompany.auth.model.User;
import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class UserDetailsImpl implements UserDetails {
  @Serial private static final long serialVersionUID = 1L;
  private final Long id;
  private final String username;
  private final String email;
  @JsonIgnore private final String password;
  private final Collection<? extends GrantedAuthority> authorities;

  public UserDetailsImpl(
      final User user, final Collection<? extends GrantedAuthority> authorities) {
    this.id = user.getId();
    this.username = user.getUsername();
    this.email = user.getEmail();
    this.password = user.getPassword();
    this.authorities = authorities;
  }

  public static UserDetailsImpl build(User user) {
    return new UserDetailsImpl(user, buildGrantedAuthorities(user));
  }

  private static List<GrantedAuthority> buildGrantedAuthorities(final User user) {
    return user.getRoles()
        .stream()
        .flatMap(role -> role.getPrivileges().stream())
        .map(privilege -> new SimpleGrantedAuthority(privilege.getName()))
        .collect(Collectors.toList());
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserDetailsImpl user = (UserDetailsImpl) o;
    return Objects.equals(id, user.id);
  }
}
