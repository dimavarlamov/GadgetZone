package com.GadgetZone.dao;

import com.GadgetZone.domain.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
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
        String sql = "SELECT * FROM users WHERE email = ?";
        List<User> users = jdbc.query(sql, new BeanPropertyRowMapper<>(User.class), email);
        return users.isEmpty() ? null : users.get(0);
    }

    public User findByName(String name) {
        String sql = "SELECT * FROM users WHERE name = ?";
        List<User> users = jdbc.query(sql, new BeanPropertyRowMapper<>(User.class), name);
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