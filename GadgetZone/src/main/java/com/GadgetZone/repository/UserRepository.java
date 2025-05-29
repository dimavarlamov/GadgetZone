package com.GadgetZone.repository;

import com.GadgetZone.entity.User;
import com.GadgetZone.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final JdbcTemplate jdbc;

    public User save(User user) {
        if (user.getId() == null) {
            String sql = "INSERT INTO users (name, email, password, role, balance, enabled) VALUES (?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, user.getName());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getRole().name());
                ps.setBigDecimal(5, user.getBalance());
                ps.setBoolean(6, user.isEnabled());
                return ps;
            }, keyHolder);
            user.setId(keyHolder.getKey().longValue());
        } else {
            String sql = "UPDATE users SET name = ?, email = ?, password = ?, role = ?, balance = ?, enabled = ? WHERE id = ?";
            jdbc.update(sql,
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole().name(),
                    user.getBalance(),
                    user.isEnabled(),
                    user.getId()
            );
        }
        return user;
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, new UserRowMapper(), email));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findById(Long userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, new UserRowMapper(), userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbc.query(sql, new UserRowMapper());
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }


    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return User.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .email(rs.getString("email"))
                    .password(rs.getString("password"))
                    .role(Role.valueOf(rs.getString("role")))
                    .balance(rs.getBigDecimal("balance"))
                    .enabled(rs.getBoolean("enabled"))
                    .build();
        }
    }
}