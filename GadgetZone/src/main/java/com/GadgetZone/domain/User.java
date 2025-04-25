package com.GadgetZone.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private String name;
    private Role role;
    private double balance;
}