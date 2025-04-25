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

    public User findFirstByName(String name) {
        String sql = "SELECT * FROM users WHERE name = ? LIMIT 1";
        List<User> users = jdbc.query(sql, new BeanPropertyRowMapper<>(User.class), name);
        return users.isEmpty() ? null : users.get(0);
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        List<User> users = jdbc.query(sql, new BeanPropertyRowMapper<>(User.class), username);
        return users.isEmpty() ? null : users.get(0);
    }

    public boolean save(User user) {
        String sql = "INSERT INTO users (username, password, name, role) VALUES (?, ?, ?, ?)";
        int updated = jdbc.update(sql, user.getUsername(), user.getPassword(), user.getName(), user.getRole());
        return updated == 1;
    }
}