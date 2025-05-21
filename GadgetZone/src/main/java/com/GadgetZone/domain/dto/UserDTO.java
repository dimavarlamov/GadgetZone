package com.GadgetZone.domain.dto;

import com.GadgetZone.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, message = "Имя должно содержать минимум 2 символа")
    private String name;

    @NotBlank
    @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
    private String password;

    @NotBlank
    private String matchingPassword;

    @Email
    @NotBlank
    private String email;

    private Role role;
}