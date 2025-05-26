package com.GadgetZone.service;

import com.GadgetZone.dao.UserRepository;
import com.GadgetZone.dao.VerificationTokenRepository;
import com.GadgetZone.domain.Role;
import com.GadgetZone.domain.User;
import com.GadgetZone.domain.VerificationToken;
import com.GadgetZone.domain.dto.UserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           VerificationTokenRepository tokenRepository,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @Override
    public boolean save(UserDTO userDTO) {
        if (userDTO.getPassword().length() < 8) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 8 символов");
        }
        if (!userDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Введите корректный email");
        }

        if (userRepository.findByEmail(userDTO.getEmail()) != null) {
            throw new IllegalArgumentException("Email уже используется");
        }
        if (userRepository.findByName(userDTO.getName()) != null) {
            throw new IllegalArgumentException("Имя пользователя уже занято");
        }

        User user = User.builder()
                .name(userDTO.getName())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .email(userDTO.getEmail())
                .role(Role.BUYER)
                .balance(1000.00)
                .enabled(false) // аккаунт неактивен до подтверждения
                .build();
        boolean saved = userRepository.save(user);
        if (saved) {
            User createdUser  = userRepository.findByEmail(userDTO.getEmail());
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setUserId(createdUser .getId());
            verificationToken.setToken(token);
            verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
            tokenRepository.saveToken(verificationToken);
            String verificationLink = "http://localhost:8080/auth/verify?token=" + token;
            emailService.sendVerificationEmail(userDTO.getEmail(), verificationLink);
        }

        return saved;
    }

        @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByName(username);
        if (user == null) {
            throw new UsernameNotFoundException("Пользователь не найден с именем: " + username);
        }

        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("Аккаунт не подтвержден.");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().name()));

        return new org.springframework.security.core.userdetails.User(
                user.getName(),
                user.getPassword(),
                authorities
        );
    }

    public boolean verifyAccount(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }
        User user = userRepository.findById(verificationToken.getUserId());
        if (user != null) {
            user.setEnabled(true);
            userRepository.update(user);
            tokenRepository.deleteByUserId(user.getId().intValue());
            return true;
        }
        return false;
    }


}