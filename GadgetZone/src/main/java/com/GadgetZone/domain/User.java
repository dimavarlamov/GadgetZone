package com.GadgetZone.domain;

import lombok.Builder;
import lombok.Data;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Data
@Builder
@Entity 
@Table(name = "users") 
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Имя обязательно")
    @Pattern(regexp = "^[A-Za-zА-Яа-я]{2,}$", message = "Имя должно содержать минимум 2 буквы")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен быть не короче 8 символов")
    private String password;

    @Transient 
    @NotBlank(message = "Подтверждение пароля обязательно")
    private String matchingPassword;

    @Email(message = "Введите корректный email")
    @NotBlank(message = "Email обязателен")
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    private double balance;
    private String name;
}