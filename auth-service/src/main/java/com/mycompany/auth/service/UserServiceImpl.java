package com.mycompany.auth.service;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;

import com.mycompany.auth.dto.SignupDto;
import com.mycompany.auth.exception.ErrorCode;
import com.mycompany.auth.exception.ServiceException;
import com.mycompany.auth.mapper.UserMapper;
import com.mycompany.auth.model.ERole;
import com.mycompany.auth.model.Role;
import com.mycompany.auth.model.User;
import com.mycompany.auth.repository.RoleRepository;
import com.mycompany.auth.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserServiceImpl implements UserService {

  public static final String EXCEPTION_USERNAME_EXISTS = "exception.usernameExists";
  public static final String EXCEPTION_EMAIL_EXISTS = "exception.emailExists";
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  private final RoleRepository roleRepository;

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public UserServiceImpl(
      final UserRepository userRepository,
      final RoleRepository roleRepository,
      final UserMapper userMapper) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.userMapper = userMapper;
  }

  @Override
  @Transactional(propagation = REQUIRED)
  public SignupDto addUser(final SignupDto signupDto) throws ServiceException {
    if (userRepository.existsByUsername(signupDto.getUsername())) {
      throw new ServiceException(ErrorCode.BAD_REQUEST_PARAMS, EXCEPTION_USERNAME_EXISTS);
    }
    if (userRepository.existsByEmail(signupDto.getEmail())) {
      throw new ServiceException(ErrorCode.BAD_REQUEST_PARAMS, EXCEPTION_EMAIL_EXISTS);
    }

    User user = userMapper.fromDto(signupDto);
    user.setPassword(passwordEncoder.encode(signupDto.getPassword()));

    Set<String> requestedRoles = signupDto.getRoles();
    if (requestedRoles == null || requestedRoles.isEmpty()) {
      requestedRoles = Set.of(ERole.USER.getName());
    }
    Set<Role> roles = new HashSet<>();
    for (String roleName : requestedRoles) {
      roles.add(getOrCreateRole(roleName));
    }
    user.setRoles(roles);

    User saved = userRepository.save(user);
    return SignupDto.builder()
        .username(saved.getUsername())
        .email(saved.getEmail())
        .roles(saved.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
        .build();
  }

  @Override
  public User findByUsername(String username) throws ServiceException {
    return userRepository
        .findByUsername(username)
        .orElseThrow(
            () -> new ServiceException(ErrorCode.BAD_REQUEST_PARAMS, "exception.user.notFound"));
  }

  @Override
  public Long getUsersCount() {
    return userRepository.count();
  }

  private Role getOrCreateRole(String roleName) {
    return roleRepository
        .findByName(roleName)
        .orElseGet(
            () ->
                roleRepository.save(
                    Role.builder().name(roleName).active(true).system(false).build()));
  }
}
