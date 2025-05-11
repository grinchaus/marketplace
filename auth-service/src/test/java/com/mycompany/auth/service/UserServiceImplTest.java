package com.mycompany.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mycompany.auth.dto.SignupDto;
import com.mycompany.auth.exception.ServiceException;
import com.mycompany.auth.mapper.UserMapper;
import com.mycompany.auth.model.ERole;
import com.mycompany.auth.model.Role;
import com.mycompany.auth.model.User;
import com.mycompany.auth.repository.RoleRepository;
import com.mycompany.auth.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class UserServiceImplTest {

  @Mock private UserRepository userRepo;

  @Mock private RoleRepository roleRepo;

  @Mock private UserMapper mapper;

  @InjectMocks private UserServiceImpl service;

  private SignupDto signup;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    signup = SignupDto.builder().username("john").email("john@ex.com").password("plain").build();

    User initialUser = new User(signup.getUsername(), signup.getEmail(), signup.getPassword());
    when(mapper.fromDto(signup)).thenReturn(initialUser);
  }

  @Test
  void addUser_success() throws ServiceException {
    when(userRepo.existsByUsername("john")).thenReturn(false);
    when(userRepo.existsByEmail("john@ex.com")).thenReturn(false);

    Role userRole = Role.builder().name(ERole.USER.getName()).build();
    when(roleRepo.findByName(ERole.USER.getName())).thenReturn(Optional.of(userRole));
    when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    SignupDto result = service.addUser(signup);
    assertEquals("john", result.getUsername());
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepo).save(captor.capture());

    User saved = captor.getValue();
    assertNotEquals("plain", saved.getPassword());
    assertEquals(Set.of(userRole), saved.getRoles());
  }

  @Test
  void addUser_duplicateEmail_throws() {
    when(userRepo.existsByUsername("john")).thenReturn(false);
    when(userRepo.existsByEmail("john@ex.com")).thenReturn(true);

    assertThrows(ServiceException.class, () -> service.addUser(signup));
    verify(userRepo, never()).save(any());
  }
}
