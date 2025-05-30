package com.GadgetZone.service;


import com.GadgetZone.entity.Role;
import com.GadgetZone.entity.User;
import com.GadgetZone.entity.VerificationToken;
import com.GadgetZone.exceptions.EmailExistsException;
import com.GadgetZone.exceptions.ExpiredTokenException;
import com.GadgetZone.exceptions.InvalidTokenException;
import com.GadgetZone.exceptions.UserNotFoundException;
import com.GadgetZone.repository.UserRepository;
import com.GadgetZone.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    @Value("${app.url}")
    private String appUrl;

    @Transactional
    public User register(User user) {
        validateUserRegistration(user);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_BUYER);
        user.setBalance(BigDecimal.valueOf(1000.00));
        user.setEnabled(false);

        User savedUser = userRepository.save(user);
        sendVerificationEmail(savedUser);
        return savedUser;
    }

    public User getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private void validateUserRegistration(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailExistsException(user.getEmail());
        }
    }

    private void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUserId(user.getId());
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(verificationToken);

        String verificationLink = appUrl + "/auth/verify?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Неверный токен подтверждения"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ExpiredTokenException("Срок действия токена истек");
        }

        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (user.isEnabled()) {
            throw new IllegalArgumentException("Аккаунт уже подтверждён");
        }

        VerificationToken oldToken = verificationTokenRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidTokenException("Токен не найден"));

        verificationTokenRepository.delete(oldToken);
        sendVerificationEmail(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public void updateUserBalance(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setBalance(amount);
        userRepository.save(user);
    }
}