package com.GadgetZone.repository;

import com.GadgetZone.entity.VerificationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VerificationTokenRepository {
    private final JdbcTemplate jdbc;

    public void save(VerificationToken token) {
        String sql = "INSERT INTO verification_tokens (token, user_id, expiry_date) " +
                "VALUES (?, ?, ?)";
        jdbc.update(sql,
                token.getToken(),
                token.getUserId(),
                token.getExpiryDate());
    }

    public Optional<VerificationToken> findByToken(String token) {
        String sql = "SELECT * FROM verification_tokens WHERE token = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, new VerificationTokenRowMapper(), token));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void delete(VerificationToken token) {
        String sql = "DELETE FROM verification_tokens WHERE id = ?";
        jdbc.update(sql, token.getId());
    }

    private static class VerificationTokenRowMapper implements RowMapper<VerificationToken> {
        @Override
        public VerificationToken mapRow(ResultSet rs, int rowNum) throws SQLException {
            return VerificationToken.builder()
                    .id(rs.getLong("id"))
                    .token(rs.getString("token"))
                    .userId(rs.getLong("user_id"))
                    .expiryDate(rs.getTimestamp("expiry_date").toLocalDateTime())
                    .build();
        }
    }

    public Optional<VerificationToken> findByUserId(Long userId) {
        String sql = "SELECT * FROM verification_tokens WHERE user_id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, new VerificationTokenRowMapper(), userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}