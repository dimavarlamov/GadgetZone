package com.GadgetZone.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.GadgetZone.domain.dto.UserDTO;

public interface UserService extends UserDetailsService {
    boolean save(UserDTO userDTO);
}   