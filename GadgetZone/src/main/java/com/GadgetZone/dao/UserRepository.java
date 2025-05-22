package com.GadgetZone.dao;

import com.GadgetZone.domain.User;
import com.GadgetZone.domain.Role;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public User findByEmail(String email) {
        String sql = "SELECT id, name, password_hash, email, role, balance FROM users WHERE email = ?";
        List<User> users = jdbc.query(sql, (rs, rowNum) -> User.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .password(rs.getString("password_hash"))
                .email(rs.getString("email"))
                .role(Role.valueOf(rs.getString("role")))
                .balance(rs.getDouble("balance"))
                .build(), email);
        return users.isEmpty() ? null : users.get(0);
    }

    public User findByName(String name) {
        String sql = "SELECT id, name, password_hash, email, role, balance FROM users WHERE name = ?";
        List<User> users = jdbc.query(sql, (rs, rowNum) -> User.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .password(rs.getString("password_hash"))
                .email(rs.getString("email"))
                .role(Role.valueOf(rs.getString("role")))
                .balance(rs.getDouble("balance"))
                .build(), name);
        return users.isEmpty() ? null : users.get(0);
    }

    public boolean save(User user) {
        String sql = "INSERT INTO users (name, email, password_hash, role, balance) VALUES (?, ?, ?, ?, ?)";
        int updated = jdbc.update(sql,
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().name(),
                user.getBalance()
        );
        return updated == 1;
    }
}