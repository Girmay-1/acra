package com.acra.repository;


import com.acra.model.RuleSet;
import com.acra.model.Rule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class RuleSetRepository {

    private final JdbcTemplate jdbcTemplate;

    public RuleSetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public RuleSet save(RuleSet ruleSet) {
        if (ruleSet.getId() == null) {
            // Insert new rule set
            String sql = "INSERT INTO rule_sets (name) VALUES (?) RETURNING id";
            Long id = jdbcTemplate.queryForObject(sql, Long.class, ruleSet.getName());
            ruleSet.setId(id);
        } else {
            // Update existing rule set
            String sql = "UPDATE rule_sets SET name = ? WHERE id = ?";
            jdbcTemplate.update(sql, ruleSet.getName(), ruleSet.getId());
        }

        // Save rules
        for (Rule rule : ruleSet.getRules()) {
            if (rule.getId() == null) {
                String sql = "INSERT INTO rules (rule_set_id, name, description, enabled) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(sql, ruleSet.getId(), rule.getName(), rule.getDescription(), rule.isEnabled());
            } else {
                String sql = "UPDATE rules SET name = ?, description = ?, enabled = ? WHERE id = ?";
                jdbcTemplate.update(sql, rule.getName(), rule.getDescription(), rule.isEnabled(), rule.getId());
            }
        }

        return ruleSet;
    }

    public Optional<RuleSet> findById(Long id) {
        String sql = "SELECT * FROM rule_sets WHERE id = ?";
        List<RuleSet> ruleSets = jdbcTemplate.query(sql, new RuleSetRowMapper(), id);

        if (ruleSets.isEmpty()) {
            return Optional.empty();
        }

        RuleSet ruleSet = ruleSets.get(0);
        ruleSet.setRules(findRulesByRuleSetId(id));
        return Optional.of(ruleSet);
    }

    public List<RuleSet> findAll() {
        String sql = "SELECT * FROM rule_sets";
        List<RuleSet> ruleSets = jdbcTemplate.query(sql, new RuleSetRowMapper());

        for (RuleSet ruleSet : ruleSets) {
            ruleSet.setRules(findRulesByRuleSetId(ruleSet.getId()));
        }

        return ruleSets;
    }

    private List<Rule> findRulesByRuleSetId(Long ruleSetId) {
        String sql = "SELECT * FROM rules WHERE rule_set_id = ?";
        return jdbcTemplate.query(sql, new RuleRowMapper(), ruleSetId);
    }

    private static class RuleSetRowMapper implements RowMapper<RuleSet> {
        @Override
        public RuleSet mapRow(ResultSet rs, int rowNum) throws SQLException {
            RuleSet ruleSet = new RuleSet();
            ruleSet.setId(rs.getLong("id"));
            ruleSet.setName(rs.getString("name"));
            return ruleSet;
        }
    }
    @Transactional
    public void deleteById(Long id) {
        // First, delete all rules associated with this rule set
        String deleteRulesSql = "DELETE FROM rules WHERE rule_set_id = ?";
        jdbcTemplate.update(deleteRulesSql, id);

        // Then, delete the rule set itself
        String deleteRuleSetSql = "DELETE FROM rule_sets WHERE id = ?";
        jdbcTemplate.update(deleteRuleSetSql, id);
    }
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM rule_sets WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private static class RuleRowMapper implements RowMapper<Rule> {
        @Override
        public Rule mapRow(ResultSet rs, int rowNum) throws SQLException {
            Rule rule = new Rule();
            rule.setId(rs.getLong("id"));
            rule.setName(rs.getString("name"));
            rule.setDescription(rs.getString("description"));
            rule.setEnabled(rs.getBoolean("enabled"));
            return rule;
        }
    }
}
