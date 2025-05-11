package com.mycompany.user.service;

import com.mycompany.user.dto.UserDto;
import com.mycompany.user.model.User;
import com.mycompany.user.repository.UserRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserDto registerUser(UserDto userDto) {
    if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
      throw new IllegalArgumentException("User with this email already exists");
    }

    String hashedPassword = passwordEncoder.encode(userDto.getPassword());

    User user =
        User.builder()
            .email(userDto.getEmail())
            .hashedPassword(hashedPassword)
            .firstName(userDto.getFirstName())
            .lastName(userDto.getLastName())
            .middleName(userDto.getMiddleName())
            .address(userDto.getAddress())
            .build();

    User savedUser = userRepository.save(user);
    return mapToDto(savedUser);
  }

  @Override
  public UserDto updateUser(Long id, UserDto userDto) {
    Optional<User> optionalUser = userRepository.findById(id);
    if (optionalUser.isEmpty()) {
      throw new IllegalArgumentException("User with this id does not exist");
    }
    User user = optionalUser.get();

    user.setFirstName(userDto.getFirstName());
    user.setLastName(userDto.getLastName());
    user.setMiddleName(userDto.getMiddleName());
    user.setAddress(userDto.getAddress());

    if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
      user.setHashedPassword(passwordEncoder.encode(userDto.getPassword()));
    }
    User updatedUser = userRepository.save(user);
    return mapToDto(updatedUser);
  }

  private UserDto mapToDto(User user) {
    return UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .middleName(user.getMiddleName())
        .address(user.getAddress())
        .build();
  }
}
