package com.GadgetZone.repository;

import com.GadgetZone.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryRepository {
    private final JdbcTemplate jdbc;

    public List<Category> findAll() {
        String sql = "SELECT * FROM categories";
        return jdbc.query(sql, new CategoryRowMapper());
    }

    private static class CategoryRowMapper implements RowMapper<Category> {
        @Override
        public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Category.builder()
                    .id(rs.getLong("id"))
                    .title(rs.getString("title"))
                    .build();
        }
    }

    public Category save(Category category) {
        if (category.getId() == null) {
            String sql = "INSERT INTO categories (title) VALUES (?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, category.getTitle());
                return ps;
            }, keyHolder);
            category.setId(keyHolder.getKey().longValue());
        } else {
            String sql = "UPDATE categories SET title = ? WHERE id = ?";
            jdbc.update(sql, category.getTitle(), category.getId());
        }
        return category;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        jdbc.update(sql, id);
    }

    public Optional<Category> findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, new CategoryRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}
