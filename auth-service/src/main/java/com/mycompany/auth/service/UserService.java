package com.mycompany.auth.service;

import com.mycompany.auth.dto.SignupDto;
import com.mycompany.auth.exception.ServiceException;
import com.mycompany.auth.model.User;

public interface UserService {
  SignupDto addUser(SignupDto user) throws ServiceException;

  User findByUsername(String username) throws ServiceException;

  Long getUsersCount();
}
