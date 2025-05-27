package com.GadgetZone.service;

import com.GadgetZone.entity.Role;
import com.GadgetZone.entity.User;
import com.GadgetZone.exceptions.UserNotFoundException;
import com.GadgetZone.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminService {
    private final UserRepository userRepository;

    @Transactional
    public void updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.setRole(newRole);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}

