package com.GadgetZone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Имя обязательно")
    @Pattern(regexp = "^[A-Za-zА-Яа-я]{2,}$", message = "Имя должно содержать минимум 2 буквы")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль должен быть не короче 8 символов")
    @JsonIgnore
    private String password;

    @Transient
    @JsonIgnore
    private String matchingPassword;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email обязателен")
    @Email(message = "Введите корректный email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(columnDefinition = "DECIMAL(10,2) DEFAULT 1000.00")
    private BigDecimal balance;

    @Column(nullable = false)
    private boolean enabled;
}