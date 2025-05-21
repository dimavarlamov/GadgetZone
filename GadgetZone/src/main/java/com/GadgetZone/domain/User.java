package com.GadgetZone.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;

    @NotBlank(message = "Имя обязательно")
    @Pattern(regexp = "^[A-Za-zА-Яа-я]{2,}$", message = "Имя должно содержать минимум 2 буквы")
    private String name;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен быть не короче 8 символов")
    private String password;

    private transient String matchingPassword;

    @Email(message = "Введите корректный email")
    @NotBlank(message = "Email обязателен")
    private String email;

    private Role role;

    private double balance;
}